package com.usv.rqapp.fragments;

import android.content.Context;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.controllers.Verifier;
import com.usv.rqapp.databinding.FragmentRecoveryBinding;

public class RecoveryFragment extends Fragment {
    private static final String TAG = "RecoveryFragment";
    private View recoveryView;
    private FragmentRecoveryBinding binding;
    private FragmentManager manager;

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

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecoveryBinding.inflate(inflater, container, false);
        recoveryView = binding.getRoot();

        backPRessHandler();
        passwordRecoverHandler();

        return recoveryView;
    }


    private void passwordRecoverHandler() {
        binding.btnRecovery.setOnClickListener(click -> recoverPassword());
    }

    private boolean validateEmailField() {
        boolean isValid = false;
        if (TextUtils.isEmpty(binding.edtEmailRecovery.getText().toString())) {
            binding.edtEmailRecovery.setError(CONSTANTS.INVALIDE_EMAIL);
            binding.edtEmailRecovery.requestFocus();
        } else if (!Verifier.validEmail(binding.edtEmailRecovery.getText().toString())) {
            binding.edtEmailRecovery.setError(CONSTANTS.INCORECT_EMAIL);
            binding.edtEmailRecovery.requestFocus();
        } else {

            isValid = true;
        }

        return isValid;
    }

    private void recoverPassword() {
        if (validateEmailField()) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(binding.edtEmailRecovery.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            binding.tvEmailTrimis.setVisibility(View.VISIBLE);
                            binding.tvEmailTrimis.setTextColor(getResources().getColor(R.color.colorGreen));
                            binding.tvEmailTrimis.setText(CONSTANTS.EMAIL_SENT_SUCCESS);
                            Toast.makeText(getActivity(), CONSTANTS.EMAIL_SENT_SUCCESS,Toast.LENGTH_LONG).show();
                            manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                                    CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, LoginFragment.newInstance()).commit();
                        } else {
                            binding.tvEmailTrimis.setVisibility(View.VISIBLE);
                            binding.tvEmailTrimis.setTextColor(getResources().getColor(R.color.colorRed));
                            binding.tvEmailTrimis.setText(CONSTANTS.EMAIL_SENT_ERROR);
                        }

                    });
        }
    }

    private void backPRessHandler() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // manager.popBackStackImmediate();
                FragmentOpener.loadNextFragment(LoginFragment.newInstance(),manager);
            }
        });
    }


    public static RecoveryFragment newInstance() {
        RecoveryFragment fragment = new RecoveryFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
