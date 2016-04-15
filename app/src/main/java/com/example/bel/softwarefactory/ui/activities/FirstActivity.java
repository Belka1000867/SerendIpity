package com.example.bel.softwarefactory.ui.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.preferences.UserLocalStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.first_activity)
public class FirstActivity extends BaseActivity {

    private UserLocalStore userLocalStore;

    @AfterViews
    protected void afterViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        //get reference to local store
        userLocalStore = new UserLocalStore(this);

        isLoggedInUser();
    }

    @Click(R.id.login_button)
    protected void login_button_click() {
        Intent intent;
        intent = (userLocalStore.isFacebookLoggedIn() || userLocalStore.isUserLoggedIn()) ? new Intent(getBaseContext(), MenuActivity.class) : new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Click(R.id.register_button)
    protected void register_button_click() {
        RegisterActivity_.intent(FirstActivity.this).start();
    }

    @Click(R.id.startAsVisitor_button)
    protected void startAsVisitor_button_click() {
        userLocalStore.setUserLoggedIn(false);
        MenuActivity_.intent(FirstActivity.this).start();
        finish();
    }

    public void isLoggedInUser() {
        //if the user logged in as serendipity user and want to be remembered in the application
        boolean serendipityUser = userLocalStore.isUserLoggedIn() && userLocalStore.isRememberMe();
        //skip this page if user is logged in as facebook user or serendipity
        if (serendipityUser || userLocalStore.isFacebookLoggedIn()) {
            goToMapActivity();
        }
        Log.d("DEBUG", "Serendipity : " + serendipityUser);
        Log.d("DEBUG", "Facebook: " + userLocalStore.isFacebookLoggedIn());
    }

    public void goToMapActivity() {
        MenuActivity_.intent(FirstActivity.this).start();
    }

}
