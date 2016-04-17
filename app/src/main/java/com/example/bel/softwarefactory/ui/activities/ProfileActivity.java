package com.example.bel.softwarefactory.ui.activities;

import android.app.AlertDialog;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.utils.ServerRequests;
import com.example.bel.softwarefactory.entities.UserEntity;
import com.example.bel.softwarefactory.preferences.UserLocalStore;
import com.example.bel.softwarefactory.utils.AlertDialogHelper_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_profile)
public class ProfileActivity extends BaseActivity {

    @ViewById
    protected TextView userName_textView;
    @ViewById
    protected EditText username_editText;
    @ViewById
    protected EditText email_editText;
    @ViewById
    protected EditText password_editText;
    @ViewById
    protected EditText passwordConfirmation_editText;

    @Bean
    protected UserLocalStore userLocalStore;

    @AfterViews
    protected void afterViews() {
        userName_textView.setText(userLocalStore.getUser().getUsername());
        username_editText.setText(userLocalStore.getUser().getUsername());
        email_editText.setText(userLocalStore.getUser().getEmail());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Click(R.id.saveChanges_button)
    protected void saveChanges_button_click() {
        String userName = username_editText.getText().toString();
        String email = email_editText.getText().toString();

        final String previousEmail = userLocalStore.getUser().getEmail();
        boolean isNameChanging = true;
        boolean isEmailChanging = true;

        if (userName.isEmpty() || userName.equals(userLocalStore.getUser().getUsername())) {
            userName = userLocalStore.getUser().getUsername();
            isNameChanging = false;
        }

        if (email.isEmpty() || email.equals(previousEmail)) {
            email = userLocalStore.getUser().getEmail();
            isEmailChanging = false;
        }

        if (isNameChanging || isEmailChanging) {
            if (isEmailValid(email)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
                //create EditText inside of the Alert
                final EditText input = new EditText(ProfileActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                //code taken from
                //http://stackoverflow.com/questions/2586301/set-inputtype-for-an-edittext
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input.setGravity(Gravity.CENTER);
                dialogBuilder.setView(input);
                dialogBuilder.setMessage("Verify password");

                final String usernameToChange = userName;
                final String emailToChange = email;

                dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                    if (input.getText().toString().equals(userLocalStore.getUser().getPassword())) {

                        ServerRequests serverRequests = new ServerRequests(ProfileActivity.this);
                        serverRequests.changeUserData(usernameToChange, emailToChange, previousEmail, returnedUser -> {
                            if (returnedUser.getEmail().equals(previousEmail) && !emailToChange.equals(previousEmail)) {
                                showErrorMessage("Such Email already exist");
                            } else {
                                showErrorMessage("Data was successfully changed!");
                                userLocalStore.saveUser(new UserEntity(returnedUser.getUsername(), returnedUser.getEmail()));
                                Log.d("DEBUG", "username" + usernameToChange + " email " + emailToChange);
                                Log.d("DEBUG", "username" + userLocalStore.getUser().getUsername() + " email " + userLocalStore.getUser().getEmail());
                            }
                        });
                    } else
                        showErrorMessage("Incorrect password");
                });
                dialogBuilder.setNegativeButton("Cancel", null);
                dialogBuilder.show();
            } else
                showErrorMessage("Email incorrect");
        } else
            showErrorMessage("Nothing to change");
    }

    @Click(R.id.changePass_button)
    protected void changePass_button_click() {
        final String password = password_editText.getText().toString();
        String passwordConf = passwordConfirmation_editText.getText().toString();

        if (password.equals(passwordConf)) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
            //create EditText inside of the Alert
            final EditText input = new EditText(ProfileActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);

            //code taken from
            //http://stackoverflow.com/questions/2586301/set-inputtype-for-an-edittext
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
            input.setGravity(Gravity.CENTER);
            input.setSingleLine();
            dialogBuilder.setView(input);
            dialogBuilder.setMessage("Verify password");

            dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                if (input.getText().toString().equals(userLocalStore.getUser().getPassword())) {

                    ServerRequests serverRequests = new ServerRequests(ProfileActivity.this);
                    serverRequests.changePassword(userLocalStore.getUser().getEmail(), password);
                } else
                    showErrorMessage("Incorrect password");
            });
            dialogBuilder.setNegativeButton("Cancel", null);
            dialogBuilder.show();
        } else {
            showErrorMessage("Passwords does not match. Please, try again.");
        }
    }


    private void showErrorMessage(String error) {
        AlertDialogHelper_.getInstance_(ProfileActivity.this).showError(error);
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
