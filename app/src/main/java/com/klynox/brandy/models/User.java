package com.klynox.brandy.models;

/**
 * Created by YOBO on 1/31/2018.
 */

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    public String fname;
    public String state;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getFirstname() {
        return fname;
    }
    public void setFirstname(String fname) {
        this.fname = fname;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}
// [END blog_user_class]