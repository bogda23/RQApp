package com.usv.rqapp.data;

import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.databinding.FragmentRegisterBinding;

import java.util.Objects;

public class User {
    private String userId;
    private String userLastName;
    private String userFirstName;
    private String userEmail;
    private String userPassword;

    public User(String userFirstName, String userLastName, String userEmail, String userPassword) {
        this.userLastName = userLastName;
        this.userFirstName = userFirstName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public User(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getUserId(), user.getUserId()) &&
                Objects.equals(getUserLastName(), user.getUserLastName()) &&
                Objects.equals(getUserFirstName(), user.getUserFirstName()) &&
                Objects.equals(getUserEmail(), user.getUserEmail()) &&
                Objects.equals(getUserPassword(), user.getUserPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getUserLastName(), getUserFirstName(), getUserEmail(), getUserPassword());
    }
}
