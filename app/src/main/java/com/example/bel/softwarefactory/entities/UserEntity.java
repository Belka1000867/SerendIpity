package com.example.bel.softwarefactory.entities;

public class UserEntity {

    private String username;
    private String email;
    private String password;
    //private Date birthday;
    //private String city;
    //private String country;

    public UserEntity(String username, String email) {
        setUsername(username);
        setEmail(email);
    }

    public UserEntity(String username, String email, String password) {
        setUsername(username);
        setEmail(email);
        setPassword(password);
//        this.birthday = null;
//        this.city = null;
//        this.country = null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

}

