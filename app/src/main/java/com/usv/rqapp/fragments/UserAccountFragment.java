package com.usv.rqapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.databinding.FragmentUserAccountBinding;
import com.usv.rqapp.models.db.User;

public class UserAccountFragment extends Fragment {

    private static final String TAG = "UserAccountFragment";
    private View accountView;
    private FragmentUserAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentUserAccountBinding.inflate(inflater, container, false);
        accountView = binding.getRoot();
        initFirebase();
        initFirestoreDatabase();

        handleDeleteAccount();
        signOutHandler();

        return accountView;
    }

    private void initFirestoreDatabase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void handleDeleteAccount() {
        binding.deleteAccount.setOnClickListener(click -> {
            binding.progressBarHolder.setVisibility(View.VISIBLE);
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle(CONSTANTS.DELETE_ACCOUNT);
            dialog.setMessage(CONSTANTS.DELETE_ACCOUNT_MESSAGE);
            dialog.setPositiveButton(getString(R.string.sterge_contul), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    user.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            deleteAccount();
                        } else {
                            binding.progressBarHolder.setVisibility(View.GONE);
                            Log.e(TAG, task.getException().getMessage());
                            // Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Toast.makeText(getContext(), "Din motive de securitate trebuie sa te autentifici din nou!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            dialog.setNegativeButton(getString(R.string.anuleaza), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    binding.progressBarHolder.setVisibility(View.GONE);
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        });
    }

    private void deleteAccount() {
        db.collection(User.UTILIZATORI).document(user.getUid()).delete().addOnCompleteListener(task -> {
            binding.progressBarHolder.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), CONSTANTS.ACCOUNT_DELETED, Toast.LENGTH_LONG).show();
                goToLoginFragment();
            } else {
                binding.progressBarHolder.setVisibility(View.GONE);
                Log.e(TAG, task.getException().getMessage());

            }
        });
    }


    private void goToLoginFragment() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animationExit[0], CustomAnimation.animationExit[1]).
                replace(R.id.fragment_frame, LoginFragment.newInstance()).commit();
    }

    private void signOutHandler() {
        binding.signOut.setOnClickListener(v -> {
            signOut();
        });
    }

    public void signOut() {
        AuthUI.getInstance().signOut(getActivity()).addOnCompleteListener(task -> {
            try {
                finalize();
                Toast.makeText(getContext(), "Sign out", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                goToLoginFragment();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        // [END auth_sign_out]
    }

    public static UserAccountFragment newInstance() {
        UserAccountFragment fragment = new UserAccountFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
