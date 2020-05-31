package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.databinding.FragmentRegisterBinding;

import java.util.concurrent.Executor;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private String userResponseToken;
    private View registerView;
    private FragmentRegisterBinding binding;

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        registerView = binding.getRoot();

        buttonHandler();

        return registerView;
    }

    private void loadReCaptcha() {
        SafetyNet.getClient(getContext()).verifyWithRecaptcha(CONSTANTS.SITE_KEY)
                .addOnSuccessListener((Executor) this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Validate the user response token using the
                                    // reCAPTCHA siteverify API.
                                    //todo: We need retrofit
                                }
                            }
                        })
                .addOnFailureListener((Executor) this, new OnFailureListener() {


                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d(TAG, "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    }
                });

    }


    private void buttonHandler() {
        binding.tvRegister.setOnClickListener(v -> {
            getFragmentManager().popBackStackImmediate();
        });
    }


    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }

}
