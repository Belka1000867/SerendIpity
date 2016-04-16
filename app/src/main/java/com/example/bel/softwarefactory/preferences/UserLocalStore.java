package com.example.bel.softwarefactory.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.bel.softwarefactory.entities.UserEntity;
import com.google.android.gms.maps.model.LatLng;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class UserLocalStore {
    private static final String LOCAL_STORE_NAME = "userData";
    private SharedPreferences userLocalDatabase;
    private SharedPreferences.Editor spEditor;

    @RootContext
    void setContext(Context context){
        userLocalDatabase = context.getSharedPreferences(LOCAL_STORE_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(UserEntity user){
        spEditor = userLocalDatabase.edit();
        spEditor.putString("username", user.getUsername());
        spEditor.putString("email", user.getEmail());
        spEditor.putString("password", user.getPassword());
        spEditor.apply();
    }

    public UserEntity getLoggedInUserData(){
        String password = userLocalDatabase.getString("password","");

        return new UserEntity(getUsername(),getEmail(),password);
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


    public void setFacebookLoggedIn(boolean facebookLogin){
        spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("facebookLogin", facebookLogin);
        spEditor.apply();
    }

    public boolean isFacebookLoggedIn(){
        return userLocalDatabase.getBoolean("facebookLogin", false);
    }

    public void setFacebookId(long id){
        spEditor = userLocalDatabase.edit();
        spEditor.putLong("facebookId", id);
        spEditor.apply();
    }

    public long getFacebookId(){
        return userLocalDatabase.getLong("facebookId", -1);
    }

    public void setProfilePictureUrl(String url){
        spEditor = userLocalDatabase.edit();
        spEditor.putString("profilePictureUrl", url);
        spEditor.apply();
    }

    public String getProfilePictureUrl(){
        return userLocalDatabase.getString("profilePictureUrl", null);
    }

    public void setLastLatitude(LatLng latLng){
        spEditor = userLocalDatabase.edit();
        spEditor.putString("Latitude", Double.toString(latLng.latitude));
        spEditor.putString("Longitude", Double.toString(latLng.longitude));
        spEditor.apply();
    }

    public String getLastLatitude(){
        return userLocalDatabase.getString("Latitude", null);
    }

    public String getLastLongitude(){
        return userLocalDatabase.getString("Longitude", null);
    }

}

