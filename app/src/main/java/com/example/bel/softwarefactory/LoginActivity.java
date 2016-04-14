package com.example.bel.softwarefactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bel on 18.02.2016.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etLogin, etPassword;
    private ImageButton ibLogin;
    private TextView tvForgetPassword, tvRegistration;
    private CheckBox cbRememberMe;

    //facebook button and callback manager
    private CallbackManager facebookCallbackManager;

    //user data
    private UserLocalStore userLocalStore;

    private static final String DEBUG_TAG = "Debug_Login";

    @Override
    protected void onStart() {
        super.onStart();

//        if(serendipityUser){
//            displayUserDetails();
//            cbRememberMe.setChecked(userLocalStore.isRememberMe());
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //facebook sdk initialization and callback instance
        FacebookSdk.sdkInitialize(getApplicationContext());
        facebookCallbackManager = new CallbackManager.Factory().create();
        getSupportActionBar().hide();
        setContentView(R.layout.login);

        //facebook button initialization
        LoginButton facebookLoginButton = (LoginButton)findViewById(R.id.login_button);
        facebookLoginButton.setReadPermissions("email","public_profile");


        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(DEBUG_TAG, "Facebook_OnSuccess()");
                getFacebookUserData();
                userLocalStore.setFacebookLoggedIn(true);

                goToMapActvity();
            }

            @Override
            public void onCancel() {
                showErrorMessage("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException error) {
                showErrorMessage("Facebook login failed.");
            }
        });

        //set edittext fields from to variables
        etLogin = (EditText)findViewById(R.id.etLogin);
        etPassword = (EditText)findViewById(R.id.etPassword);

        //set ImageButton and CheckBox to variables
        ibLogin = (ImageButton) findViewById(R.id.ibLogin);
        cbRememberMe = (CheckBox)findViewById(R.id.cbRememberMe);

        //bRegister = (Button) findViewById(R.id.bRegister);
        tvForgetPassword = (TextView)findViewById(R.id.tvForgetPassword);
        tvRegistration = (TextView) findViewById(R.id.tvRegistration);

        //centralize text in edittexts
        etLogin.setGravity(Gravity.CENTER);
        etPassword.setGravity(Gravity.CENTER);

        //get reference to local store
        userLocalStore = new UserLocalStore(this);

        //set listener to LOGIN button
        ibLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(DEBUG_TAG, "LoginOnClick()");
                String email = etLogin.getText().toString();
                String password = etPassword.getText().toString();
                String username = "";

                User user = new User(username, email, password);

                authenticate(user);

                userLocalStore.setRememberUser(cbRememberMe.isChecked());
            }
        });

        tvRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordDialog();
            }
        });
    }

    private void authenticate(User user){
        Log.d(DEBUG_TAG, "authenticate()");
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null) {
                    showErrorMessage("Incorrect Email/Password Combination");
                } else {
                    logUserIn(returnedUser);
                    goToMapActvity();
                }
            }
        });
    }

    private void logUserIn(User returnedUser){
        Log.d(DEBUG_TAG, "logUserIn()");
        //store loggedIn user data in the class file
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
    }

    private void goToMapActvity(){
        Log.d(DEBUG_TAG, "goToMapActvity()");
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        startActivity(intent);
    }

    private void showErrorMessage(String text){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        //create textview with centralized text
        TextView message = new TextView(this);
        message.setText(text);
        message.setTextSize(14);
        message.setGravity(Gravity.CENTER);

        dialogBuilder.setView(message);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void showResetPasswordDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Reset Password");

        final EditText input = new EditText(LoginActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setHint("Enter your E-mail");
        input.setSingleLine();
        input.setGravity(Gravity.CENTER);
        alertDialog.setView(input);

        // Setting Icon to Dialog
        alertDialog.setIcon(R.mipmap.ic_key);

        alertDialog.setPositiveButton("Reset password", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString();
                if (isEmailValid(email)) {
                    Log.d("DEBUG", "requesting password reset");
                    ServerRequests serverRequests = new ServerRequests(LoginActivity.this);
                    serverRequests.requestPassword(email);
                    showErrorMessage("Password Reset Request sent to email");
                } else{
                    showErrorMessage("Incorrect input");
                }
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Close dialog
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //facebook for login start

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getFacebookUserData(){
        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                User userLogging = null;
                try {
                    userLogging = new User(object.getString("name"), object.getString("email"), "");
                    userLocalStore.setFacebookId(object.getLong("id"));

                    if(object.has("picture")) {
                        String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                        userLocalStore.setProfilePictureUrl(profilePicUrl);
                        Log.d(DEBUG_TAG, "Profile picture url :  " + userLocalStore.getProfilePictureUrl());
                    }
                    Log.d(DEBUG_TAG, "facebook id " + userLocalStore.getFaceboookId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(userLogging!=null)
                    logUserIn(userLogging);

                Log.d(DEBUG_TAG, "getFacebookUserData()" + userLocalStore.isUserLoggedIn());
            }
        });
        /*
        * 1. Put the string of variables into request
        * 2. Execute request
        * */
        Bundle bundle = new Bundle();
        bundle.putString("fields","id, first_name, last_name, name, name_format, email, picture");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //measure installs on your mobile app ads
        //log an app activation event for Facebook
        //Logs 'install' and 'app activate' App Events
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //logs 'app deactivate' app event for facebook
        AppEventsLogger.deactivateApp(this);
    }

}

