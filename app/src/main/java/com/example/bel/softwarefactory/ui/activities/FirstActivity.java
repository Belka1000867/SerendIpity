package com.example.bel.softwarefactory.ui.activities;

import android.support.v7.app.ActionBar;
import android.util.Log;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.preferences.UserLocalStore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.first_activity)
public class FirstActivity extends BaseActivity {

    @Bean
    protected UserLocalStore userLocalStore;

    @AfterViews
    protected void afterViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        boolean serendipityUser = userLocalStore.isUserLoggedIn() && userLocalStore.isRememberMe();
        //skip this page if user is logged in as facebook user or serendipity
        if (serendipityUser || userLocalStore.isFacebookLoggedIn()) {
            MenuActivity_.intent(FirstActivity.this).start();
            finish();
        }
        Log.d("DEBUG", "Serendipity : " + serendipityUser);
        Log.d("DEBUG", "Facebook: " + userLocalStore.isFacebookLoggedIn());
    }

    @Click(R.id.login_button)
    protected void login_button_click() {
        if (userLocalStore.isFacebookLoggedIn() || userLocalStore.isUserLoggedIn()) {
            MenuActivity_.intent(FirstActivity.this).start();
            finish();
        } else {
            LoginActivity_.intent(FirstActivity.this).start();
        }
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

}
