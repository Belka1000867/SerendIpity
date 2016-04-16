package com.example.bel.softwarefactory.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.example.bel.softwarefactory.entities.UserEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

public class ServerRequests {

    private ProgressDialog progressDialog;

    public static final String ENCODING_FORMAT = "UTF-8";

    public byte[] buffer;
    public int bytesAvailable;
    public int bytesRead;
    public int maxBufferSize = 10 * 1024 * 1024;

    public ServerRequests(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    public void storeUserDataInBackground(UserEntity user, GetUserCallback userCallback) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallback).execute();
    }

    public void fetchUserDataInBackground(UserEntity user, GetUserCallback userCallback) {
        progressDialog.show();
        new FetchUserDataAsyncTask(user, userCallback).execute();
    }

    public void uploadRecordingInBackground(String filePath, String fileName, String username) {
        progressDialog.show();
        new UploadRecordingAsyncTask(filePath, fileName, username).execute();
    }

    public void requestPassword(String email) {
        progressDialog.show();
        new RequestPassword(email).execute();
    }

    public void changeUserData(String username, String email, String prevEmail, GetUserCallback userCallback) {
        progressDialog.show();
        new ChangeUserDataAsyncTask(username, email, prevEmail, userCallback).execute();
    }

    public void changePassword(String email, String password) {
        progressDialog.show();
        new ChangePasswordAsyncTask(email, password).execute();
    }

    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, Void> {

        UserEntity user;
        GetUserCallback userCallback;

        public StoreUserDataAsyncTask(UserEntity user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {

            //HttpParams CHANGE
            //from https://www.youtube.com/watch?v=cOsZHuu8Qog

            try

            {
                URL url = new URL(AppConstants.SERVER_ADDRESS + "Register.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                //we past information
                httpURLConnection.setDoOutput(true);
                //get outputstreamwrite from http connection
                OutputStream outputStream = httpURLConnection.getOutputStream();

                //write down information
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING_FORMAT));
                //encode data before sending
                String data = URLEncoder.encode("username", ENCODING_FORMAT) + "=" + URLEncoder.encode(user.getUsername(), ENCODING_FORMAT) + "&" +
                        URLEncoder.encode("email", ENCODING_FORMAT) + "=" + URLEncoder.encode(user.getEmail(), ENCODING_FORMAT) + "&" +
                        URLEncoder.encode("password", ENCODING_FORMAT) + "=" + URLEncoder.encode(user.getPassword(), ENCODING_FORMAT) + "&";
                //write data into buffer writer
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                //input stream to get response from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                inputStream.close();

                httpURLConnection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            userCallback.done(null);
            super.onPostExecute(aVoid);
        }
    }

    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, UserEntity> {

        private UserEntity user;
        private GetUserCallback userCallback;

        public FetchUserDataAsyncTask(UserEntity user, GetUserCallback userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }

        @Override
        protected UserEntity doInBackground(Void... params) {

            try {
                URL url = new URL(AppConstants.SERVER_ADDRESS + "FetchUserData.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                //we past information
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                //get outputstreamwrite from http connection
                OutputStream outputStream = httpURLConnection.getOutputStream();
                //write down information
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING_FORMAT));
                //encode data before sending
                String data = URLEncoder.encode("email", ENCODING_FORMAT) + "=" + URLEncoder.encode(user.getEmail(), ENCODING_FORMAT) + "&" +
                        URLEncoder.encode("password", ENCODING_FORMAT) + "=" + URLEncoder.encode(user.getPassword(), ENCODING_FORMAT) + "&";
                //write data into buffer writer
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                //input stream to get response from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                String response = "";
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }

                //decode JsonArray got from response
                //JSONArray jsonArray = new JSONArray(response);
                UserEntity returnedUser;
                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.length() == 0) {
                    returnedUser = null;
                } else {
                    String username = jsonObject.getString("username");
                    String email = jsonObject.getString("email");
                    String password = jsonObject.getString("password");
                    //user.photo = jsonObject.getString("photo");
                    //user.birthday = jsonObject.get("birthday");
                    //String city = jsonObject.getString("city");
                    //String country = jsonObject.getString("country");

                    returnedUser = new UserEntity(username, email, password);
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return returnedUser;

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserEntity user) {
            progressDialog.dismiss();
            userCallback.done(user);
            super.onPostExecute(user);
        }

    }

    public class UploadRecordingAsyncTask extends AsyncTask<Void, Void, Void> {
        String fileName;
        String filePath;
        String username;

        public UploadRecordingAsyncTask(String filePath, String fileName, String username) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.username = username;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("Record", "do in background");
            try {
                Log.d("Record", "try loop");
                URL url = new URL(AppConstants.SERVER_ADDRESS + "upload_file.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                //we past information
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                Log.d("Record", "httpURLConnection.setDoOutput");
                //get outputstreamwrite from http connection
                httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpURLConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "******");
                httpURLConnection.setRequestProperty("uploaded_file", fileName);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                //write down information
                DataOutputStream dos = new DataOutputStream(outputStream);


                //Encoding file and than sending it to the database folder
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                Log.d("DEBUG", "File path " + filePath);

                String filePathCheck = Environment.getExternalStorageDirectory() + "/audio.3gp";

                Log.d("DEBUG", "File 2 path " + filePath);
                //CHECKING FROM HERE
                FileInputStream fileInputStream = new FileInputStream(new File(filePathCheck));


                InputStream inputStreamFile = new BufferedInputStream(fileInputStream);
                int numOfBytes = inputStreamFile.available();
                Log.d("DEBUG", "Size " + numOfBytes);


                bytesAvailable = inputStreamFile.available();
                Log.d("DEBUG", "bytesAvailable:" + bytesAvailable);
                int buffersize = Math.min(bytesAvailable, maxBufferSize);
                Log.d("DEBUG", "BUffersize  " + buffersize);
                buffer = new byte[buffersize];
                Log.d("DEBUG", "buffer " + Arrays.toString(buffer));
                bytesRead = inputStreamFile.read(buffer, 0, buffersize);
                Log.d("DEBUG", "bytesRead " + bytesRead);


                while (bytesRead > 0) {
                    Log.d("DEBUG", "Reading bytesRead " + bytesRead);
                    Log.d("DEBUG", "buffer 1 " + Arrays.toString(buffer));
                    outputStream.write(buffer, 0, buffersize);
                    Log.d("DEBUG", "Reading1 " + Arrays.toString(buffer));
                    Log.d("DEBUG", "Reading bytesRead 2" + bytesRead);
                    Log.d("DEBUG", "bytesAvailable 2 " + bytesAvailable);
                    bytesAvailable = inputStreamFile.available();
                    Log.d("DEBUG", "Reading2 " + bytesAvailable);
                    buffersize = Math.min(bytesAvailable, maxBufferSize);
                    Log.d("DEBUG", "Reading3 " + buffersize);
                    bytesRead = inputStreamFile.read(buffer, 0, buffersize);
                    Log.d("DEBUG", "Finishing");
                }


                byte[] audioBytesFile = new byte[numOfBytes];
                int i = inputStreamFile.read(audioBytesFile, 0, numOfBytes);


                //String audioString = Base64.encodeToString(audioBytesFile, 0);
                String audioString = Base64.encodeToString(audioBytesFile, 0);

                inputStreamFile.close();

//                byte[] bytes = new byte[1024];
//                int n;
//                while (-1 != (n = fileInputStream.read(bytes)))
//                    byteArrayOutputStream.write(bytes, 0, n);

                //byte[] audioBytes = byteArrayOutputStream.toByteArray();

                //String audioString = Base64.encodeToString(audioBytes, 0);

                //encode data before sending
//                String data = URLEncoder.encode("filename", ENCODING_FORMAT) + "=" + URLEncoder.encode(fileName, ENCODING_FORMAT) + "&" +
//                        URLEncoder.encode("owner", ENCODING_FORMAT) + "=" + URLEncoder.encode(username, ENCODING_FORMAT) + "&" +
//                        URLEncoder.encode("encodedfile", ENCODING_FORMAT) + "=" + URLEncoder.encode(audioString, ENCODING_FORMAT) + "&";


                //write data into buffer writer
                //bufferedWriter.write(data);
                dos.flush();
                dos.close();
                outputStream.flush();
                outputStream.close();


                httpURLConnection.disconnect();
                Log.d("Record", "disconnect httpURL connection");
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

    public class RequestPassword extends AsyncTask<Void, Void, Void> {
        String email;

        public RequestPassword(String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL(AppConstants.SERVER_ADDRESS + "RequestPassword.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                //we past information
                httpURLConnection.setDoOutput(true);

                //get outputstreamwrite from http connection
                OutputStream outputStream = httpURLConnection.getOutputStream();
                //write down information
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING_FORMAT));

                Log.d("DEBUG", "Request is sent to the server:" + email);

                //encode data before sending
                String data = URLEncoder.encode("email", ENCODING_FORMAT) + "=" + URLEncoder.encode(email, ENCODING_FORMAT) + "&";

                //write data into buffer writer
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                //input stream to get response from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                inputStream.close();

                httpURLConnection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

    public class ChangeUserDataAsyncTask extends AsyncTask<Void, Void, UserEntity> {
        String username, email, prevEmail;
        GetUserCallback userCallback;

        public ChangeUserDataAsyncTask(String username, String email, String prevEmail, GetUserCallback userCallback) {
            this.username = username;
            this.email = email;
            this.prevEmail = prevEmail;
            this.userCallback = userCallback;
        }

        @Override
        protected UserEntity doInBackground(Void... params) {

            Log.d("DEBUG:", "doInBackground is running");

            try {
                URL url = new URL(AppConstants.SERVER_ADDRESS + "ChangeUserData.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                //we past information
                httpURLConnection.setDoOutput(true);
                //get outputstreamwrite from http connection
                OutputStream outputStream = httpURLConnection.getOutputStream();

                Log.d("DEBUG:", "Sending data to server : username = " + username + " email = " + email + "previous email = " + prevEmail);

                //write down information
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING_FORMAT));
                //encode data before sending
                String data = URLEncoder.encode("username", ENCODING_FORMAT) + "=" + URLEncoder.encode(username, ENCODING_FORMAT) + "&" +
                        URLEncoder.encode("email", ENCODING_FORMAT) + "=" + URLEncoder.encode(email, ENCODING_FORMAT) + "&" +
                        URLEncoder.encode("prevEmail", ENCODING_FORMAT) + "=" + URLEncoder.encode(prevEmail, ENCODING_FORMAT) + "&";
                //write data into buffer writer
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                //input stream to get response from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

                String response = "";
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    response += line;
                }

                Log.d("DEBUG:", "Getting data from server : response = " + response);

                //decode JsonArray got from response
                UserEntity returnedUser;
                //JSONArray jsonArray = new JSONArray(response);

                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.length() == 0) {
                    returnedUser = null;
                } else {
                    String username = jsonObject.getString("username");
                    String email = jsonObject.getString("email");

                    Log.d("DEBUG", "username " + username + " email " + email);

                    returnedUser = new UserEntity(username, email);
                    Log.d("DEBUG", "username " + returnedUser.getUsername() + " email " + returnedUser.getEmail());
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return returnedUser;

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            Log.d("DEBUG:", "doInBackground is giving null");

            return null;
        }

        @Override
        protected void onPostExecute(UserEntity user) {
            progressDialog.dismiss();
            userCallback.done(user);
            super.onPostExecute(user);
        }
    }

    public class ChangePasswordAsyncTask extends AsyncTask<Void, Void, Void> {
        String email, password;

        public ChangePasswordAsyncTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(AppConstants.SERVER_ADDRESS + "ChangePassword.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                //we past information
                httpURLConnection.setDoOutput(true);
                //get outputstreamwrite from http connection
                OutputStream outputStream = httpURLConnection.getOutputStream();

                //write down information
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, ENCODING_FORMAT));

                Log.d("DEBUG", "email " + email + "password" + password);
                //encode data before sending
                String data = URLEncoder.encode("email", ENCODING_FORMAT) + "=" + URLEncoder.encode(email, ENCODING_FORMAT) + "&" +
                        URLEncoder.encode("password", ENCODING_FORMAT) + "=" + URLEncoder.encode(password, ENCODING_FORMAT) + "&";
                //write data into buffer writer
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                //input stream to get response from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                inputStream.close();
                httpURLConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

}

