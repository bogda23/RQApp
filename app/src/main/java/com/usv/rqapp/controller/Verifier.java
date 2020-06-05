package com.usv.rqapp.controller;

import android.util.Patterns;

import java.util.regex.Pattern;

public class Verifier {

    public static boolean validEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}
