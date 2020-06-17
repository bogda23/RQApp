package com.usv.rqapp.controllers;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.databinding.FragmentLoginBinding;

import java.util.regex.Pattern;

public class Verifier {

    public static boolean validEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static void showErrorsAtSignInFail(FragmentLoginBinding binding, Context context, Task<AuthResult> task)     {
        final String TAG = "FragmentLoginBinding";
        try {
            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_CUSTOM_TOKEN":
                    Toast.makeText(context, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                    Toast.makeText(context, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_INVALID_CREDENTIAL":
                    Toast.makeText(context, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(context, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                    binding.edtEmailLogin.setError("The email address is badly formatted.");
                    binding.edtEmailLogin.requestFocus();
                    break;

                case "ERROR_WRONG_PASSWORD":
                    Toast.makeText(context, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                    binding.edtPasswordLogin.setError(CONSTANTS.INVALIDE_PASSWORD);
                    binding.edtPasswordLogin.requestFocus();
                    binding.edtPasswordLogin.setText("");
                    break;

                case "ERROR_USER_MISMATCH":
                    Toast.makeText(context, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_REQUIRES_RECENT_LOGIN":
                    Toast.makeText(context, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                    Toast.makeText(context, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_EMAIL_ALREADY_IN_USE":
                    Toast.makeText(context, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                    Toast.makeText(context, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_USER_DISABLED":
                    Toast.makeText(context, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_USER_TOKEN_EXPIRED":
                    Toast.makeText(context, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(context, "There is no user record corresponding to this identifier. The user may have been deleted.", Toast.LENGTH_LONG).show();
                    binding.edtEmailLogin.setError(CONSTANTS.YOU_DONT_HAVE_ACCOUNT);
                    binding.edtEmailLogin.requestFocus();
                    break;

                case "ERROR_INVALID_USER_TOKEN":
                    Toast.makeText(context, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_OPERATION_NOT_ALLOWED":
                    Toast.makeText(context, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                    break;

                case "ERROR_WEAK_PASSWORD":
                    Toast.makeText(context, "The given password is invalid.", Toast.LENGTH_LONG).show();
                    binding.edtPasswordLogin.setError(CONSTANTS.INVALIDE_PASSWORD);
                    binding.edtPasswordLogin.requestFocus();
                    break;
            }
        } catch (Exception e) {
            binding.edtPasswordLogin.setError(CONSTANTS.TO_MANY_REQUESTS);
            binding.edtPasswordLogin.requestFocus();
            Log.e(TAG + " Exeption", e.getMessage());
            e.printStackTrace();
        }
    }
}
