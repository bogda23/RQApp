package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.controller.Verifier;
import com.usv.rqapp.data.User;
import com.usv.rqapp.databinding.FragmentRegisterBinding;

import java.util.concurrent.Executor;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private String userResponseToken;
    private View registerView;
    private FragmentRegisterBinding binding;
    private FirebaseAuth auth;

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

        initFirebase();
        buttonHandler();
        configurateNormalRegister();
        onTogglePasswordPressed();

        return registerView;
    }

    private void setInputTypePAssword() {
        binding.edtPasswordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void setInputTypeEmail() {
        binding.edtEmailRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    private void onTogglePasswordPressed() {
        setInputTypeEmail();
        setInputTypePAssword();
        binding.tvTogglePasswordRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.tvTogglePasswordRegister.getText() == CONSTANTS.SHOW) {
                    binding.tvTogglePasswordRegister.setText(CONSTANTS.HIDE);
                    binding.edtPasswordRegister.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    binding.edtPasswordRegister.setSelection(binding.edtPasswordRegister.length());
                } else {
                    binding.tvTogglePasswordRegister.setText(CONSTANTS.SHOW);
                    binding.edtPasswordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.edtPasswordRegister.setSelection(binding.edtPasswordRegister.length());
                }
            }
        });
    }


    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
    }

    private void configurateNormalRegister() {
        binding.btnRegister.setOnClickListener(click -> {
            //   String firstName = binding.edtNumeRegister.getText().toString();
            //  String lastName = binding.edtPrenumeRegister.getText().toString();
            String email = binding.edtEmailRegister.getText().toString();
            String password = binding.edtPasswordRegister.getText().toString();
            User user = new User(email, password);
            if (validateFieldsFromUser(user)) {
                createFirebaseUser(user);
            }
            ;
        });
    }

    private boolean validateFieldsFromUser(User user) {
        boolean isValid = false;
        if (user.getUserEmail().isEmpty()) {
            binding.edtEmailRegister.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailRegister.requestFocus();
        } else if (user.getUserPassword().isEmpty()) {
            binding.edtPasswordRegister.setError(CONSTANTS.MIN_SIX_CHARS_PASSWORD);
            binding.edtPasswordRegister.requestFocus();
        } else if (user.getUserEmail().isEmpty() && user.getUserPassword().isEmpty()) {
            binding.edtEmailRegister.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailRegister.requestFocus();
            binding.edtPasswordRegister.setError(CONSTANTS.MIN_SIX_CHARS_PASSWORD);
            binding.edtPasswordRegister.requestFocus();
        } else {
            Toast.makeText(getContext(), "Eroare la inregistrare", Toast.LENGTH_LONG).show();
            isValid = true;
        }
        return isValid;
    }

    private void createFirebaseUser(User user) {
        auth.createUserWithEmailAndPassword(user.getUserEmail(), user.getUserPassword())
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in:success");
                        Toast.makeText(getContext(), "Inregistrare cu succes", Toast.LENGTH_LONG).show();
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        System.out.println("---------------------------------------------------..................................>>>>>>>>>>>>>" + firebaseUser.getEmail());
                        updateUI();

                    } else {
                        try {
                            throw task.getException();
                        }
                        // if user enters wrong email.
                        catch (FirebaseAuthWeakPasswordException weakPassword) {
                            Log.d(TAG, "onComplete: weak_password");
                            binding.edtPasswordRegister.setError(CONSTANTS.MIN_SIX_CHARS_PASSWORD);
                            binding.edtPasswordRegister.requestFocus();
                        }
                        // if user enters wrong password.
                        catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                            Log.d(TAG, "onComplete: malformed_email");
                            binding.edtEmailRegister.setError(CONSTANTS.VERIFY_EMAIL);
                            binding.edtEmailRegister.requestFocus();
                        } catch (FirebaseAuthUserCollisionException existEmail) {
                            Log.d(TAG, "onComplete: exist_email");
                            binding.edtEmailRegister.setError(CONSTANTS.EMAIL_ALREADY_EXISTS);
                            binding.edtEmailRegister.requestFocus();
                        } catch (Exception e) {
                            Log.d(TAG, "onComplete: " + e.getMessage());
                        }
                    }
                });
    }

    private void updateUI() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, MapsFragment.newInstance()).commit();
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
        binding.tvIHaveAccount.setOnClickListener(v -> {
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
