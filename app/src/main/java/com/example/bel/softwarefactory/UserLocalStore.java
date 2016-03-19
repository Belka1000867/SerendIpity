package com.example.bel.softwarefactory;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Bel on 21.02.2016.
 */
public class UserLocalStore {

    // sharedp references file 'userData' for user information where we save all settings about the account of loggedIn user
    public static final String LOCAL_STORE_NAME = "userData";
    SharedPreferences userLocalDatabase;
    SharedPreferences.Editor spEditor;

    public UserLocalStore(Context context){
        userLocalDatabase = context.getSharedPreferences(LOCAL_STORE_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserData(User user){
        spEditor = userLocalDatabase.edit();
        spEditor.putString("username", user.getUsername());
        spEditor.putString("email", user.getEmail());
        spEditor.putString("password", user.getPassword());
        spEditor.apply();
    }

    public User getLoggedInUserData(){
        String password = userLocalDatabase.getString("password","");

        return new User(getUsername(),getEmail(),password);
    }

    //GET user data separately
    public String getUsername(){
        return userLocalDatabase.getString("username", "");
    }

    public void setUsername(String username){
        spEditor = userLocalDatabase.edit();
        spEditor.putString("username", username);
        spEditor.apply();
    }

    public String getEmail(){
        return userLocalDatabase.getString("email", "");
    }

    public void setEmail(String email){
        spEditor = userLocalDatabase.edit();
        spEditor.putString("email", email);
        spEditor.apply();
    }

    public void setUserLoggedIn(boolean loggedIn){
        spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn", loggedIn);
        spEditor.apply();
    }

    public String getPassword(){
        return userLocalDatabase.getString("password", "");
    }

    public boolean isUserLoggedIn(){
        return userLocalDatabase.getBoolean("loggedIn", false);
    }

    public void clearUserData(){
        spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.apply();
    }

    // set variable to remember user details if he want or don't want to be remembered (depends on the check box)
    public void setRememberUser(boolean rememberUser){
        spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("rememberUser", rememberUser);
        spEditor.apply();
    }

    public boolean isRememberMe(){
        return userLocalDatabase.getBoolean("rememberUser", false);
    }


    public void setFacebookLogin(boolean facebookLogin){
        spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("facebookLogin", facebookLogin);
        spEditor.apply();
    }

    public boolean isFacebookLogin(){
        return userLocalDatabase.getBoolean("facebookLogin", false);
    }
}

