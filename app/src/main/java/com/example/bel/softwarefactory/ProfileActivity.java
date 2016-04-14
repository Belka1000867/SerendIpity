package com.example.bel.softwarefactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by bel on 09.03.16.
 */
public class ProfileActivity extends AppCompatActivity {

    TextView tvUserName;
    EditText etUser, etEmail, etPass, etPassConf;
    Button bSaveChanges, bChangePass;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        userLocalStore = new UserLocalStore(this);

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        etUser = (EditText) findViewById(R.id.etUsername);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPassword);
        etPassConf = (EditText) findViewById(R.id.etPasswordConf);
        bSaveChanges = (Button) findViewById(R.id.bSaveChanges);
        bChangePass = (Button) findViewById(R.id.bChangePass);

        //centralize text in edittexts
        etUser.setGravity(Gravity.CENTER);
        etEmail.setGravity(Gravity.CENTER);
        etPass.setGravity(Gravity.CENTER);
        etPassConf.setGravity(Gravity.CENTER);

        tvUserName.setText(userLocalStore.getUsername());
        etUser.setText(userLocalStore.getUsername());
        etEmail.setText(userLocalStore.getEmail());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeData();
            }
        });

        bChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });


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

    private void showErrorMessage(String text){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
        //create textview with centralized text
        TextView message = new TextView(this);
        message.setText(text);
        message.setTextSize(14);
        message.setGravity(Gravity.CENTER);

        dialogBuilder.setView(message);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void changeData(){

        String userName = etUser.getText().toString();
        String email = etEmail.getText().toString();

        final String previousEmail = userLocalStore.getEmail();
        boolean isNameChanging = true;
        boolean isEmailChanging = true;

        if (userName.isEmpty() || userName.equals(userLocalStore.getUsername())) {
            userName = userLocalStore.getUsername();
            isNameChanging = false;
        }

        if (email.isEmpty() || email.equals(previousEmail)) {
            email = userLocalStore.getEmail();
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

                dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().equals(userLocalStore.getPassword())) {

                            ServerRequests serverRequests = new ServerRequests(ProfileActivity.this);
                            serverRequests.changeUserData(usernameToChange, emailToChange, previousEmail, new GetUserCallback() {
                                @Override
                                public void done(User returnedUser) {
                                    if (returnedUser.getEmail().equals(previousEmail) && !emailToChange.equals(previousEmail)) {
                                        showErrorMessage("Such Email already exist");
                                    } else {
                                        showErrorMessage("Data was successfully changed!");
                                        userLocalStore.setUsername(returnedUser.getUsername());
                                        userLocalStore.setEmail(returnedUser.getEmail());
                                        Log.d("DEBUG", "username" + usernameToChange + " email " + emailToChange);
                                        Log.d("DEBUG", "username" + userLocalStore.getUsername() + " email " + userLocalStore.getEmail());
                                    }
                                }
                            });
                        }
                        else
                            showErrorMessage("Incorrect password");
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", null);
                dialogBuilder.show();
            } else
                showErrorMessage("Email incorrect");
        } else
            showErrorMessage("Nothing to change");
    }

    public void changePassword(){
        final String password = etPass.getText().toString();
        String passwordConf = etPassConf.getText().toString();

        if(password.equals(passwordConf)){
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

            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (input.getText().toString().equals(userLocalStore.getPassword())) {

                        ServerRequests serverRequests = new ServerRequests(ProfileActivity.this);
                        serverRequests.changePassword(userLocalStore.getEmail(), password);
                    }
                    else
                        showErrorMessage("Incorrect password");
                }
            });
            dialogBuilder.setNegativeButton("Cancel", null);
            dialogBuilder.show();
        }
        else {
            showErrorMessage("Passwords does not match. Please, try again.");
        }


    }
}
