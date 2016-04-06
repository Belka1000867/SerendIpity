package com.example.bel.softwarefactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.appevents.AppEventsLogger;

/**
 * Created by bel on 09.03.16.
 */
public class FirstActivity extends AppCompatActivity {

    UserLocalStore userLocalStore;
    Button bLogin, bRegister, bStartVisitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.first_activity);

        bLogin = (Button) findViewById(R.id.bLogin);
        bRegister = (Button) findViewById(R.id.bSignUp);
        bStartVisitor = (Button) findViewById(R.id.bStartAsVisitor);

        //get reference to local store
        userLocalStore = new UserLocalStore(this);

        isLoggedInUser();

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = (userLocalStore.isFacebookLoggedIn() || userLocalStore.isUserLoggedIn()) ? new Intent(getBaseContext(), Menu.class) : new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

        bStartVisitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLocalStore.setUserLoggedIn(false);
                Intent intent = new Intent(getApplicationContext(), Menu.class);
                startActivity(intent);
            }
        });
    }

    public void isLoggedInUser(){
        //if the user logged in as serendipity user and want to be remembered in the application
        boolean serendipityUser = userLocalStore.isUserLoggedIn() && userLocalStore.isRememberMe();
        //skip this page if user is logged in as facebook user or serendipity
        if(serendipityUser || userLocalStore.isFacebookLoggedIn()){
            goToMapActvity();
        }
        Log.d("DEBUG", "Serendipity : " + serendipityUser);
        Log.d("DEBUG", "Facebook: " + userLocalStore.isFacebookLoggedIn());
    }

    public void goToMapActvity() {
        Intent intent = new Intent(getApplicationContext(), Menu.class);
        startActivity(intent);
    }
}
