package com.usv.rqapp.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.models.captcha.CaptchaResponse;
import com.usv.rqapp.models.db.User;
import com.usv.rqapp.databinding.FragmentRegisterBinding;
import com.usv.rqapp.reCaptcha.IreCaptcha;
import com.usv.rqapp.reCaptcha.ReCaptcha;

import java.sql.Timestamp;
import java.util.Date;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private FragmentManager manager;

    private String userResponseToken;
    private View registerView;
    private FragmentRegisterBinding binding;
    private FirebaseAuth auth;
    private FirestoreController db;
    private IreCaptcha captcha;

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = getFragmentManager();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        registerView = binding.getRoot();

        initCaptcha();
        initFirebase();
        initFirestoreDatabase();
        iHaveAccountHandler();
        configurateNormalRegister();
        onTogglePasswordPressed();


        return registerView;
    }

    private void initFirestoreDatabase() {
        db = new FirestoreController();
    }

    private void emailVerification() {
        final FirebaseUser user = auth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(),
                                "Email de verificare trimis la adresa  " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                        FragmentOpener.loadNextFragment(LoginFragment.newInstance(),manager);
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(getActivity(),
                                "Nu am reusit sa trimitem email de verificare. Reincercati înregistrarea",
                                Toast.LENGTH_LONG).show();
                    }

                });
    }

    private void initCaptcha() {
        captcha = ReCaptcha.getCaptchaApi();
    }

    private void handleCaptchaValidation() {
        SafetyNet.getClient(getActivity().getApplicationContext()).verifyWithRecaptcha(CONSTANTS.RECAPTCHA_SITE_KEY)
                .addOnSuccessListener(response -> {
                    if (!response.getTokenResult().isEmpty()) {
                        verifyCaptchaTokenOnServer(response.getTokenResult());
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        int statusCode = apiException.getStatusCode();
                        Log.e(TAG, "ERROR_SERVER: " + CommonStatusCodes.getStatusCodeString(statusCode));
                    } else {
                        // A different, unknown type of error occurred.
                        Log.e(TAG, "ERROR_UNKNOWN: " + e.getMessage());
                    }
                });

    }

    private void verifyCaptchaTokenOnServer(String tokenResult) {
        AlertDialog dialog = new SpotsDialog(getContext());
        dialog.show();
        dialog.setMessage("Vă rog așteptați...");
        captcha.captchaValidate(tokenResult).enqueue(new Callback<CaptchaResponse>() {
            @Override
            public void onResponse(Call<CaptchaResponse> call, Response<CaptchaResponse> response) {
                dialog.dismiss();
                if (response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Nu sunteți robot", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getContext(), "Sunteți robot", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CaptchaResponse> call, Throwable t) {
                dialog.dismiss();
                Log.e(TAG + "-Captcha", "Eroare la verificare token");
            }
        });
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
            binding.progressBarHolder.setVisibility(View.VISIBLE);
            String firstName = binding.edtNumeRegister.getText().toString();
            String lastName = binding.edtPrenumeRegister.getText().toString();
            String email = binding.edtEmailRegister.getText().toString();
            String password = binding.edtPasswordRegister.getText().toString();
            Date signUpDate = DateHandler.getCurrentDate();
            Timestamp lastLogin = DateHandler.getCurrentTimestamp();
            System.out.println("SignUpDate--------------------->>>>>>>>>>>>>>>" + signUpDate);
            if (validateFieldsFromUser()) {
                User user = new User(firstName, lastName, email, password, signUpDate, lastLogin, false);
                //todo: RESOLVE THIS PROBLEM --> handleCaptchaValidation();
                createFirebaseUser(user);
            } else {
                binding.progressBarHolder.setVisibility(View.GONE);
            }
        });
    }

    private boolean validateFieldsFromUser() {
        boolean isValid = false;
        if (TextUtils.isEmpty(binding.edtNumeRegister.getText().toString()) || binding.edtNumeRegister.getText().length() < CONSTANTS.MINIMUM_LENGTH_FOR_NAME) {
            binding.edtNumeRegister.setError(CONSTANTS.INVALID_LASTNAME);
            binding.edtNumeRegister.requestFocus();
        } else if (TextUtils.isEmpty(binding.edtPrenumeRegister.getText().toString()) || binding.edtPrenumeRegister.getText().length() < CONSTANTS.MINIMUM_LENGTH_FOR_NAME) {
            binding.edtPrenumeRegister.setError(CONSTANTS.INVALID_FIRSTNAME);
            binding.edtPrenumeRegister.requestFocus();
        } else if (binding.edtEmailRegister.getText().toString().isEmpty()) {
            binding.edtEmailRegister.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailRegister.requestFocus();
        } else if (binding.edtPasswordRegister.getText().toString().isEmpty()) {
            binding.edtPasswordRegister.setError(CONSTANTS.MIN_SIX_CHARS_PASSWORD);
            binding.edtPasswordRegister.requestFocus();
        } else if (binding.edtEmailRegister.getText().toString().isEmpty() && binding.edtPasswordRegister.getText().toString().isEmpty()) {
            binding.edtEmailRegister.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailRegister.requestFocus();
            binding.edtPasswordRegister.setError(CONSTANTS.MIN_SIX_CHARS_PASSWORD);
            binding.edtPasswordRegister.requestFocus();
        } else {
            isValid = true;
        }
        return isValid;
    }

    private void createFirebaseUser(User user) {
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getParola())
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in:success");
                        user.setId_utilizator(auth.getUid());
                        emailVerification();
                        createNodeInFirebaseDatabase(user);
                        binding.progressBarHolder.setVisibility(View.GONE);

                    } else {
                        binding.progressBarHolder.setVisibility(View.GONE);
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
                            binding.edtEmailRegister.setError(CONSTANTS.INVALIDE_EMAIL);
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

    private void createNodeInFirebaseDatabase(User user) {
        user.setFirstTime(true);
        if (db.addUserToFireStore(User.UTILIZATORI, user.convertUsereToMap(user))) {
            binding.progressBarHolder.setVisibility(View.GONE);
            signInWithEmailAndPassword(auth, user);
        }
    }

    private void updateUI() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, MapsFragment.newInstance()).commit();
    }

    private void iHaveAccountHandler() {
        binding.tvIHaveAccount.setOnClickListener(v -> {
            getFragmentManager().popBackStackImmediate();
        });
    }

    private void signInWithEmailAndPassword(FirebaseAuth auth, User user) {
        auth.signInWithEmailAndPassword(user.getEmail(), user.getParola()).addOnCompleteListener(getActivity(), task -> {
            if (task.isSuccessful()) {
                FragmentOpener.loadNextFragment(MapsFragment.newInstance(), getFragmentManager());
            }
        }).addOnFailureListener(e->{
            FragmentOpener.loadNextFragment(LoginFragment.newInstance(), getFragmentManager());
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
