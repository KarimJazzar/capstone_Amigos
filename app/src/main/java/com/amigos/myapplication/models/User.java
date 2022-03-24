package com.amigos.myapplication.models;

import android.graphics.Bitmap;

public class User {
    private String firstName;
    private String lastName;
    private String phoneNuber;
    private String email;
    private Bitmap profilePicture;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNuber() {
        return phoneNuber;
    }

    public void setPhoneNuber(String phoneNuber) {
        this.phoneNuber = phoneNuber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }
}
