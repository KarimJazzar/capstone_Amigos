package com.amigos.myapplication.helpers;

import com.amigos.myapplication.models.User;

public class UserHelper {
    public static User user = new User();

    public static String getUserFullname() {
        return user.getFirstName() + " " + user.getLastName();
    }
}
