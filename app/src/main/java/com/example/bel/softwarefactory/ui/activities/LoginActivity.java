package com.example.bel.softwarefactory.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.ServerRequests;
import com.example.bel.softwarefactory.preferences.UserLocalStore;
import com.example.bel.softwarefactory.entities.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {
    private final String TAG = this.getClass().getSimpleName();

    @ViewById
    protected TextView forgetPassword_textView;
    @ViewById
    protected EditText login_editText;
    @ViewById
    protected EditText password_editText;
    @ViewById
    protected ImageButton login_imageButton;
    @ViewById
    protected TextView registration_textView;
    @ViewById
    protected CheckBox rememberMe_checkBox;

    //facebook button and callback manager
    private CallbackManager facebookCallbackManager ;

    @Bean
    protected UserLocalStore userLocalStore;

    @AfterViews
    protected void afterViews() {

        //facebook sdk callback instance
        facebookCallbackManager = CallbackManager.Factory.create();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //facebook button initialization
        LoginButton loginFacebook_button = (LoginButton) findViewById(R.id.loginFacebook_button);
        if (loginFacebook_button != null) {
            loginFacebook_button.setReadPermissions("email", "public_profile");
            loginFacebook_button.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "Facebook_OnSuccess()");
                    getFacebookUserData();
                    userLocalStore.setFacebookLoggedIn(true);
                    MenuActivity_.intent(LoginActivity.this).start();
                }

                @Override
                public void onCancel() {
                    showAlert("Login attempt canceled.");
                }

                @Override
                public void onError(FacebookException error) {
                    showAlert("Facebook login failed.");
                }
            });
        }
    }

    @Click(R.id.login_imageButton)
    protected void login_imageButton_click() {
        Log.d(TAG, "LoginOnClick()");
        String email = login_editText.getText().toString();
        String password = password_editText.getText().toString();
        String username = "";

        User user = new User(username, email, password);
        authenticate(user);
        userLocalStore.setRememberUser(rememberMe_checkBox.isChecked());
    }

    @Click(R.id.registration_textView)
    protected void registration_textView_click() {
        RegisterActivity_.intent(LoginActivity.this).start();
    }

    @Click(R.id.forgetPassword_textView)
    protected void forgetPassword_textView_click() {
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

        alertDialog.setPositiveButton("Reset password", (dialog, which) -> {
            String email = input.getText().toString();
            if (isEmailValid(email)) {
                Log.d("DEBUG", "requesting password reset");
                ServerRequests serverRequests = new ServerRequests(LoginActivity.this);
                serverRequests.requestPassword(email);
                showAlert("Password Reset Request sent to email");
            } else {
                showAlert("Incorrect input");
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
            // Close dialog
            dialog.cancel();
        });

        alertDialog.show();
    }

    private void authenticate(User user) {
        Log.d(TAG, "authenticate()");
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.fetchUserDataInBackground(user, returnedUser -> {
            if (returnedUser == null) {
                showAlert("Incorrect Email/Password Combination");
            } else {
                logUserIn(returnedUser);
                MenuActivity_.intent(LoginActivity.this).start();
            }
        });
    }

    private void logUserIn(User returnedUser) {
        Log.d(TAG, "logUserIn()");
        //store loggedIn user data in the class file
        userLocalStore.saveUser(returnedUser);
        userLocalStore.setUserLoggedIn(true);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //facebook for login start
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getFacebookUserData() {
        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
            User userLogging = null;
            try {
                userLogging = new User(object.getString("name"), object.getString("email"), "");
                userLocalStore.setFacebookId(object.getLong("id"));

                if (object.has("picture")) {
                    String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    userLocalStore.setProfilePictureUrl(profilePicUrl);
                    Log.d(TAG, "Profile picture url :  " + userLocalStore.getProfilePictureUrl());
                }
                Log.d(TAG, "facebook id " + userLocalStore.getFacebookId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (userLogging != null) {
                logUserIn(userLogging);
            }

            Log.d(TAG, "getFacebookUserData()" + userLocalStore.isUserLoggedIn());
        });
        /*
        * 1. Put the string of variables into request
        * 2. Execute request
        * */
        Bundle bundle = new Bundle();
        bundle.putString("fields", "id, first_name, last_name, name, name_format, email, picture");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

}

