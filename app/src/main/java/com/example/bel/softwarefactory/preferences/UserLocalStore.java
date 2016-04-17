package com.example.bel.softwarefactory.preferences;

import com.example.bel.softwarefactory.entities.AudioRecordEntity;
import com.example.bel.softwarefactory.entities.UserEntity;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.lang.reflect.Type;
import java.util.List;

@EBean
public class UserLocalStore {

    @Pref
    protected Preferences_ preferences;

    public void saveUser(UserEntity user) {
        Gson gson = new Gson();
        String userString = gson.toJson(user);

        preferences.edit()
                .user()
                .put(userString)
                .apply();
    }

    public UserEntity getUser() {
        Gson gson = new Gson();
        return gson.fromJson(preferences.user().get(), UserEntity.class);
    }

    public void setUsername(String username) {
        Gson gson = new Gson();
        UserEntity userEntity = gson.fromJson(preferences.user().get(), UserEntity.class);
        userEntity.setUsername(username);
        saveUser(userEntity);
    }

    public void setEmail(String email) {
        Gson gson = new Gson();
        UserEntity userEntity = gson.fromJson(preferences.user().get(), UserEntity.class);
        userEntity.setEmail(email);
        saveUser(userEntity);
    }

    public void setPassword(String password) {
        Gson gson = new Gson();
        UserEntity userEntity = gson.fromJson(preferences.user().get(), UserEntity.class);
        userEntity.setPassword(password);
        saveUser(userEntity);
    }

    public void setUserLoggedIn(boolean loggedIn) {
        preferences.edit()
                .loggedIn()
                .put(loggedIn)
                .apply();
    }


    public boolean isUserLoggedIn() {
        return preferences.loggedIn().getOr(false);
    }

    public void clearUserData() {
        preferences.clear();
    }

    // set variable to remember user details if he want or don't want to be remembered (depends on the check box)
    public void setRememberUser(boolean rememberUser) {
        preferences.edit()
                .rememberUser()
                .put(rememberUser)
                .apply();
    }

    public boolean isRememberMe() {
        return preferences.rememberUser().getOr(false);
    }

    public void setFacebookLoggedIn(boolean facebookLogin) {
        preferences.edit()
                .facebookLogin()
                .put(facebookLogin)
                .apply();
    }

    public boolean isFacebookLoggedIn() {
        return preferences.facebookLogin().getOr(false);
    }

    public void setFacebookId(long id) {
        preferences.edit()
                .facebookId()
                .put(id)
                .apply();
    }

    public long getFacebookId() {
        return preferences.facebookId().getOr(-1L);
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        preferences.edit()
                .profilePictureUrl()
                .put(profilePictureUrl)
                .apply();
    }

    public String getProfilePictureUrl() {
        return preferences.profilePictureUrl().getOr(null);
    }

    public void setLastPosition(LatLng latLng) {
        Gson gson = new Gson();
        String latLonString = gson.toJson(latLng);

        preferences.edit()
                .lastPosition()
                .put(latLonString)
                .apply();
    }

    public double getLastLatitude() {
        Gson gson = new Gson();
        LatLng latLng = gson.fromJson(preferences.lastPosition().get(), LatLng.class);
        return latLng.latitude;
    }

    public double getLastLongitude() {
        Gson gson = new Gson();
        LatLng latLng = gson.fromJson(preferences.lastPosition().get(), LatLng.class);
        return latLng.longitude;
    }

    public void saveAudioRecordsList(List<AudioRecordEntity> audioRecordEntities) {
        Gson gson = new Gson();
        String saveAudioRecordsListString = gson.toJson(audioRecordEntities);

        preferences.edit()
                .audioRecordsList()
                .put(saveAudioRecordsListString)
                .apply();
    }

    public List<AudioRecordEntity> getAudioRecordsList() {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<AudioRecordEntity>>() {
        }.getType();
        return gson.fromJson(preferences.audioRecordsList().get(), listType);
    }

}

