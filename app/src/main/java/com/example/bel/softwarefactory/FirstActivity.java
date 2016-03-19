package com.example.bel.softwarefactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
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
