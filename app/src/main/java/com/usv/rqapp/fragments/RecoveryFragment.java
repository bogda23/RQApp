package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usv.rqapp.R;
import com.usv.rqapp.databinding.FragmentRecoveryBinding;

public class RecoveryFragment extends Fragment {
    private View recoveryView;
    private FragmentRecoveryBinding binding;

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecoveryBinding.inflate(inflater, container, false);
        recoveryView = binding.getRoot();

        backPRessHandler();

        return recoveryView;
    }

    private void backPRessHandler() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();

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
