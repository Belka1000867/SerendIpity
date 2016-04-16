package com.example.bel.softwarefactory.ui.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.utils.ServerRequests;
import com.example.bel.softwarefactory.entities.UserEntity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_register)
public class RegisterActivity extends BaseActivity {

    @ViewById
    protected EditText username_editText;

    @ViewById
    protected EditText email_editText;

    @ViewById
    protected EditText password_editText;

    @ViewById
    protected EditText passwordConfirmation_editText;

    @Click(R.id.signIn_textView)
    protected void signIn_textView_click() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Click(R.id.confirmRegistration_imageButton)
    protected void confirmRegistration_imageButton_click() {
        String username = username_editText.getText().toString();
        String email = email_editText.getText().toString();
        String password = password_editText.getText().toString();
        String passwordConf = passwordConfirmation_editText.getText().toString();

        if (isEmailValid(email)) {
            // Check Password and Confirmed Password
            // if not matched show toast
            if (password.equals(passwordConf)) {
                UserEntity newUser = new UserEntity(username, email, password);
                signUpUser(newUser);
            } else {
                showToast("Passwords does not match. Please, try again.");
            }
        } else {
            showToast("Email is incorrect. Please, try again.");
        }
    }

    @AfterViews
    protected void afterViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //get reference to local store
//        UserLocalStore userLocalStore = new UserLocalStore(this);
    }

    //code from http://stackoverflow.com/questions/6119722/how-to-check-edittexts-text-is-email-address-or-not
    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showToast(String errorText) {
        Toast toast = Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void signUpUser(UserEntity user) {
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeUserDataInBackground(user, returnedUser -> {
            LoginActivity_.intent(RegisterActivity.this).start();
            showToast("Registration successful. Please, Login.");
        });

    }
}
