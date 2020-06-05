package com.usv.rqapp.fragments;

import android.content.Context;
import android.content.Intent;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.controller.FragmentOpener;
import com.usv.rqapp.data.User;
import com.usv.rqapp.databinding.FragmentLoginBinding;


import java.util.Arrays;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private FragmentManager manager;

    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1;
    private static final int FB_SIGN_IN = 2;
    private static FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private View loginView;
    private FragmentLoginBinding binding;
    private AdView adView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        loginView = binding.getRoot();

        /***/
        initFirebase();
        testAds();
        configurateNormalLoginToFirebase();
        configurateGoogleLogin();
        configurateFacebookLogin();
        uiHandler();
        onTogglePasswordPressed();


        return loginView;
    }

    private void setInputTypePAssword() {
        binding.edtPasswordLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void onTogglePasswordPressed() {
        setInputTypePAssword();
        binding.tvTogglePasswordLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.tvTogglePasswordLogin.getText() == CONSTANTS.SHOW) {
                    binding.tvTogglePasswordLogin.setText(CONSTANTS.HIDE);
                    binding.edtPasswordLogin.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    binding.edtPasswordLogin.setSelection(binding.edtPasswordLogin.length());
                } else {
                    binding.tvTogglePasswordLogin.setText(CONSTANTS.SHOW);
                    binding.edtPasswordLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.edtPasswordLogin.setSelection(binding.edtPasswordLogin.length());
                }
            }
        });
    }

    private void configurateNormalLoginToFirebase() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    //  Toast.makeText(getContext(), "Logare normala cu succes", Toast.LENGTH_LONG).show();

                    manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                            CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, MapsFragment.newInstance()).commit();
                }
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleResultTask(task);
                break;
        }
    }

    private void handleGoogleResultTask(Task<GoogleSignInAccount> task) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account);
            }

        } catch (ApiException e) {
            Log.e(TAG, "Google sign in failed", e);                                             //todo: Google Sign In failed, update UI appropriately

        } finally {
            binding.imgGoogleLogin.setEnabled(true);
        }
    }


    private void uiHandler() {
        registerHandler();
        forgotPasswordHandler();
        loginHandler();
    }


    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
    }

    private void configurateGoogleLogin() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this.getActivity(), gso);
        binding.imgGoogleLogin.setOnClickListener(v -> signInGoogle());
    }

    private void signInGoogle() {
        binding.imgGoogleLogin.setEnabled(false);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void singOutGoogle() {
        FirebaseAuth.getInstance().signOut();
        googleSignInClient.signOut().addOnCompleteListener(getActivity(), task -> {
            updateUI(null);
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //  Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());                                   //todo: get the id of the user

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in:success");
                        FirebaseUser user = auth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "Sign in:failure", task.getException());
                        updateUI(null);
                    }

                });
    }

    private void facebookLoginButtonHandler() {

        binding.imgFacebookLogin.setOnClickListener(v -> {

            binding.imgFacebookLogin.setClickable(false);

            LoginManager.getInstance().logInWithReadPermissions(LoginFragment.this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken());
                    binding.imgFacebookLogin.setClickable(true);
                }

                @Override
                public void onCancel() {
                    binding.imgFacebookLogin.setClickable(true);
                }

                @Override
                public void onError(FacebookException error) {
                    error.printStackTrace();
                    binding.imgFacebookLogin.setClickable(true);
                }
            });
        });
    }

    private void configurateFacebookLogin() {
        //  FacebookSdk.sdkInitialize(getView().getContext().getApplicationContext());  --------------> todo: Commented due to the corona virus
        //  AppEventsLogger.activateApp(getView().getContext().getApplicationContext());
        // logger.logPurchase(BigDecimal.valueOf(4.32), Currency.getInstance("USD"));
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        facebookLoginButtonHandler();

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.e(TAG, "HandleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "SignInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            binding.imgFacebookLogin.setClickable(true);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "SignInWithCredential:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            binding.imgFacebookLogin.setClickable(true);
                            updateUI(null);

                        }
                    }

                });
        binding.imgFacebookLogin.setEnabled(true);
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) { //todo: Aici setam user, parola, email,imagini
            Toast.makeText(getContext(), "You are logged in as : " + user.getEmail(), Toast.LENGTH_LONG).show();
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                    CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, MapsFragment.newInstance()).commit();

            // Get data from google account
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
            if (acct != null) {
                String nickName = acct.getDisplayName();
                String lastName = acct.getGivenName();
                String firstName = acct.getFamilyName();

                Toast.makeText(getContext(), nickName + " " + lastName + " " + firstName, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void testAds() {
        MobileAds.initialize(getContext(), CONSTANTS.BANNER_ID_SAMPLE);
        MobileAds.initialize(getContext(), initializationStatus -> {
            Toast.makeText(getContext(), initializationStatus.toString(), Toast.LENGTH_LONG).show();
        });

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(CONSTANTS.HUAWEI_ID).build();
        binding.adView.loadAd(adRequest);


    }

    private void loginHandler() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmailLogin.getText().toString();
            String password = binding.edtPasswordLogin.getText().toString();
            User user = new User(email, password);
            if (fieldsFromUserAreValid(user)) {
                signInWithEmailAndPassword(auth, user);
            }


        });
    }

    private boolean fieldsFromUserAreValid(User user) {
        boolean validForSignIn = false;
        if (user.getUserEmail().isEmpty()) {
            binding.edtEmailLogin.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailLogin.requestFocus();
        } else if (user.getUserPassword().isEmpty()) {
            binding.edtPasswordLogin.setError(CONSTANTS.INVALIDE_PASSWORD);
            binding.edtPasswordLogin.requestFocus();
        } else if (user.getUserEmail().isEmpty() && user.getUserPassword().isEmpty()) {
            binding.edtEmailLogin.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailLogin.requestFocus();
            binding.edtPasswordLogin.setError(CONSTANTS.INVALIDE_PASSWORD);
            binding.edtPasswordLogin.requestFocus();
        } else if (!user.getUserEmail().isEmpty() && !user.getUserPassword().isEmpty()) {
            validForSignIn = true;
        } else {
            Toast.makeText(getContext(), "Eroare la inregistrare", Toast.LENGTH_LONG).show();
            validForSignIn = false;
        }
        return validForSignIn;
    }

    private void signInWithEmailAndPassword(FirebaseAuth auth, User user) {
        auth.signInWithEmailAndPassword(user.getUserEmail(), user.getUserPassword()).addOnCompleteListener(getActivity(), task -> {
            if (task.isSuccessful()) {
                FragmentOpener.loadNextFragment(MapsFragment.newInstance(), getFragmentManager());
            } else {
                Toast.makeText(getContext(), "Logare normala nereusita", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void forgotPasswordHandler() {
        binding.tvForgotPassword.setOnClickListener(v -> {
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_frame, RecoveryFragment.newInstance()).addToBackStack(null).commit();

        });
    }

    private void registerHandler() {
        binding.tvRegister.setOnClickListener(v -> {
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                    CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, RegisterFragment.newInstance()).addToBackStack(null).commit();

        });
    }


    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }


}
