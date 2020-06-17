package com.usv.rqapp.fragments;

import android.content.Context;
import android.content.Intent;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.ads.AdRequest;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.DbController;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.controllers.Verifier;
import com.usv.rqapp.models.db.User;
import com.usv.rqapp.databinding.FragmentLoginBinding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private FragmentManager manager;

    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;
    private DbController dbController;
    private FirebaseFirestore db;
    private static final int RC_SIGN_IN = 1;
    private static final int FB_SIGN_IN = 2;
    private static FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private View loginView;
    private FragmentLoginBinding binding;
    private String userID;


    @Override
    public void onStart() {
        super.onStart();

        auth.addAuthStateListener(authStateListener);
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                binding.edtEmailLogin.setError(null);
                updateUI(currentUser);
                Log.e(TAG, "Userul are email-ul validat ");
                return;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        loginView = binding.getRoot();

        /***/
        initFirestoreDatabase();
        initFirebase();
        testAds();
        handleAlreadyLogedUserToFirebase();
        configurateGoogleLogin();
        configurateFacebookLogin();
        uiHandler();
        onTogglePasswordPressed();


        return loginView;
    }

    private void initFirestoreDatabase() {
        dbController = new DbController();
        db = FirebaseFirestore.getInstance();
    }

    private void setInputTypePAssword() {
        binding.edtPasswordLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void onTogglePasswordPressed() {
        setInputTypeEmail();
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

    private void setInputTypeEmail() {
        binding.edtEmailLogin.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    private void handleAlreadyLogedUserToFirebase() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    //  Toast.makeText(getContext(), "Logare normala cu succes", Toast.LENGTH_LONG).show();
                    if (user.isEmailVerified()) {
                        manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                                CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, PermissionsFragment.newInstance()).commit();

                    } else {
                        Log.e(TAG, "Userul nu a validat email-ul");
                        binding.edtEmailLogin.setText(user.getEmail());
                        binding.edtEmailLogin.setError(CONSTANTS.UNVERIFIED_EMAIL);
                        binding.edtEmailLogin.requestFocus();
                    }

                }
            }
        };
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = getFragmentManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       // FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                binding.progressBarHolder.setVisibility(View.VISIBLE);
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
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        // Get data from google account
                        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
                        if (googleAccount != null) {
                            User user = new User(googleAccount.getFamilyName(), googleAccount.getGivenName(), googleAccount.getEmail(), DateHandler.getCurrentDate(), DateHandler.getCurrentTimestamp(), false);
                            user.setId_utilizator(firebaseUser.getUid());
                            updateUserCredetialsInFirestore(user);
                        }
                        updateUI(firebaseUser);

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
                            Log.e(TAG, "SignInWithCredential:success");
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            User user = new User(Profile.getCurrentProfile().getLastName(), Profile.getCurrentProfile().getFirstName(),
                                    task.getResult().getUser().getEmail(), DateHandler.getCurrentDate(), DateHandler.getCurrentTimestamp(), false);
                            user.setId_utilizator(firebaseUser.getUid());
                            updateUserCredetialsInFirestore(user);
                            binding.imgFacebookLogin.setClickable(true);
                            updateUI(firebaseUser);
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
                    CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, PermissionsFragment.newInstance()).commit();

        }
    }

    private void testAds() {
        /*List<String> testDeviceIds = Arrays.asList("0EC3F86BF34E8ACF4D2FE0905F86C4B8");
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);*/
        MobileAds.initialize(getContext(), CONSTANTS.BANNER_ID_SAMPLE);
        MobileAds.initialize(getContext(), initializationStatus -> {
            Toast.makeText(getContext(), initializationStatus.toString(), Toast.LENGTH_LONG).show();
        });

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(CONSTANTS.HUAWEI_ID).build();
        binding.adView.loadAd(adRequest);


    }

    private void loginHandler() {
        binding.btnLogin.setOnClickListener(v -> {
            binding.progressBarHolder.setVisibility(View.VISIBLE);
            String email = binding.edtEmailLogin.getText().toString();
            String password = binding.edtPasswordLogin.getText().toString();
            User user = new User(email, password);
            if (fieldsFromUserAreValid(user)) {
                signInWithEmailAndPassword(auth, user);
            } else {
                binding.progressBarHolder.setVisibility(View.GONE);
            }
        });
    }

    private boolean fieldsFromUserAreValid(User user) {
        boolean validForSignIn = false;
        if (TextUtils.isEmpty(user.getEmail()) || !Verifier.validEmail(user.getEmail())) {
            binding.edtEmailLogin.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailLogin.requestFocus();
        } else if (TextUtils.isEmpty(user.getParola())) {
            binding.edtPasswordLogin.setError(CONSTANTS.INVALIDE_PASSWORD);
            binding.edtPasswordLogin.requestFocus();
        } else if (TextUtils.isEmpty(user.getEmail()) && TextUtils.isEmpty(user.getParola())) {
            binding.edtEmailLogin.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailLogin.requestFocus();
            binding.edtPasswordLogin.setError(CONSTANTS.INVALIDE_PASSWORD);
            binding.edtPasswordLogin.requestFocus();
        } else if (!TextUtils.isEmpty(user.getEmail()) && !TextUtils.isEmpty(user.getParola())) {
            validForSignIn = true;
        } else {
            Toast.makeText(getContext(), "Eroare la inregistrare", Toast.LENGTH_LONG).show();
            validForSignIn = false;
        }
        return validForSignIn;
    }

    private void signInWithEmailAndPassword(FirebaseAuth auth, User user) {
        auth.signInWithEmailAndPassword(user.getEmail(), user.getParola()).addOnCompleteListener(getActivity(), task -> {
            if (task.isSuccessful()) {
                user.setId_utilizator(auth.getUid());
                updateUserCredetialsInFirestore(user);
                binding.progressBarHolder.setVisibility(View.GONE);
                if (auth.getCurrentUser().isEmailVerified()) {
                    FragmentOpener.loadNextFragment(PermissionsFragment.newInstance(), getFragmentManager());
                } else {
                    binding.edtEmailLogin.setError(CONSTANTS.UNVERIFIED_EMAIL);
                    binding.edtEmailLogin.requestFocus();
                }

            } else {
                Verifier.showErrorsAtSignInFail(binding, getContext(), task);
                binding.progressBarHolder.setVisibility(View.GONE);
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


    public void updateUserCredetialsInFirestore(User user) {
        if (user.getId_utilizator() != null) {
            DocumentReference documentReference = db.collection(User.UTILIZATORI).document(user.getId_utilizator());
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.e(TAG + " User_exists", documentSnapshot.getId());
                        userID = documentSnapshot.getId();
                        user.setFirstTime(false);
                        Map<String, Object> map = new HashMap<>();
                        map.put(User.ULTIMA_LOGARE, DateHandler.getCurrentTimestamp());
                        db.collection(User.UTILIZATORI).document(user.getId_utilizator()).update(map);

                    } else {
                        Log.e(TAG + " User_exists", "Data Empty");
                        user.setFirstTime(true);
                        if (dbController.addUserToFireStore(User.UTILIZATORI, user.convertUsereToMap(user))) {
                            binding.progressBarHolder.setVisibility(View.GONE);
                            Log.e(TAG, "Logare cu Google ---> Date salvate in DB cu succes");
                        }


                    }
                }
            })
                    .addOnFailureListener(e -> {
                        Log.e(TAG + "_", e.getMessage());
                        user.setFirstTime(true);
                        if (dbController.addUserToFireStore(User.UTILIZATORI, user.convertUsereToMap(user))) {
                            binding.progressBarHolder.setVisibility(View.GONE);
                            Log.e(TAG, "Logare cu Google ---> Date salvate in DB cu succes");
                        }
                    });
        }


    }
}
