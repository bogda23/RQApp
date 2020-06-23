package com.usv.rqapp.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.databinding.FragmentUserAccountBinding;
import com.usv.rqapp.models.firestoredb.User;

import java.util.Calendar;

public class UserAccountFragment extends Fragment {

    private static final String TAG = "UserAccountFragment";
    private View accountView;
    private FragmentUserAccountBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private FirestoreController firestoreController;

    private FragmentManager manager;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private String userID;

    @Override
    public void onStart() {
        super.onStart();
        manager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentUserAccountBinding.inflate(inflater, container, false);
        accountView = binding.getRoot();
        initFirebase();
        initFirestoreDatabase();
        loadAccountData();
        loadDateOfBirthFromFirebase();

        handleDatePicker();
        handleChangePassword();
        handleDeleteAccount();
        signOutHandler();

        return accountView;
    }

    public void loadDateOfBirthFromFirebase() {
        if (firebaseUser.getUid() != null) {
            DocumentReference documentReference = db.collection(User.UTILIZATORI).document(firebaseUser.getUid());
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists() && documentSnapshot.getString(User.DATA_NASTERE) != null) {
                        Log.e(TAG + " Data nastere exista", documentSnapshot.getId());
                        binding.btnDatePicker.setText(documentSnapshot.getString(User.DATA_NASTERE));

                    } else {
                        Log.e(TAG + " Data nastere nu exista", "Data Empty");

                    }
                }
            })
                    .addOnFailureListener(e -> {
                        Log.e(TAG + "Data nastere eroare: ", e.getMessage());

                    });
        }
    }

    private void handleDatePicker() {
        binding.btnDatePicker.setOnClickListener(click -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener, year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                binding.btnDatePicker.setText(date);
                User user = new User(firebaseUser.getUid(), date, true);
                firestoreController.updateUserDateOdBirthInFirestore(user);

            }
        };
    }


    private void handleChangePassword() {
        binding.tvChangePassword.setOnClickListener(v -> {
            binding.progressBarHolder.setVisibility(View.VISIBLE);
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle(CONSTANTS.CHANGE_PASSWORD);
            dialog.setMessage(CONSTANTS.CHANGE_PASSWORD_MESSAGE);
            dialog.setPositiveButton(getString(R.string.schimba_parola), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AuthUI.getInstance().signOut(getActivity()).addOnCompleteListener(task -> {
                        try {
                            finalize();
                            Toast.makeText(getContext(), "Sign out", Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            goToRecoveryFragment();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
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

    private void goToRecoveryFragment() {
        manager.beginTransaction().replace(R.id.fragment_frame, RecoveryFragment.newInstance()).commit();
    }

    private void loadAccountData() {
        binding.txtAccountName.setText(firebaseUser.getDisplayName());
        binding.edtAccountEmail.setText(firebaseUser.getEmail());
    }

    private void initFirestoreDatabase() {
        db = FirebaseFirestore.getInstance();
        firestoreController = new FirestoreController();
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
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
                    firebaseUser.delete().addOnCompleteListener(task -> {
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
        db.collection(User.UTILIZATORI).document(firebaseUser.getUid()).delete().addOnCompleteListener(task -> {
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
