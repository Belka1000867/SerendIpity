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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bel on 18.02.2016.
 */
public class Login extends AppCompatActivity {

    EditText etLogin;
    EditText etPassword;
    ImageButton ibLogin;
    TextView tvForgetPassword;
    TextView tvRegistration;
    CheckBox cbRememberMe;

    private LoginButton facebookLoginButton;
    private CallbackManager facebookCallbackManager;

    //user data
    UserLocalStore userLocalStore;

    @Override
    protected void onStart() {
        super.onStart();

        if(userLocalStore.isUserLoggedIn() && userLocalStore.isRememberMe() ){
            displayUserDetails();
            cbRememberMe.setChecked(userLocalStore.isRememberMe());
        }
    }

    private void displayUserDetails(){
        User user = userLocalStore.getLoggedInUserData();

        etLogin.setText(user.getEmail());
        etPassword.setText(user.getPassword());
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
        facebookLoginButton = (LoginButton)findViewById(R.id.login_button);
        facebookLoginButton.setReadPermissions("email","public_profile");


        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getFacebookUserData();
                userLocalStore.setFacebookLogin(true);
                //logUserIn(returnedUser);
                //goToMapActvity();
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
                Intent intent = new Intent(getApplicationContext(), Register.class);
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
        //store loggedIn user data in the class file
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
    }

    private void goToMapActvity(){
        Intent intent = new Intent(getApplicationContext(), Menu.class);
        startActivity(intent);
    }

    private void showErrorMessage(String text){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Login.this);
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

        final EditText input = new EditText(Login.this);
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
                    ServerRequests serverRequests = new ServerRequests(Login.this);
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
                //Log.d("DEBUG", response.toString());
                //Log.d("DEBUG", object.toString());

                Bundle bundle = logInAsFacebookUser(object);
                User user = new User(bundle.getString("name", ""), bundle.getString("email", ""));
                logUserIn(user);
                goToMapActvity();
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("fields","first_name, last_name, name, name_format, email");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

    private Bundle logInAsFacebookUser(JSONObject jsonObject){
        Bundle bundle = new Bundle();

        try {
            bundle.putString("name", jsonObject.getString("name"));
            bundle.putString("email", jsonObject.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bundle;
    }
}

