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

public class RecoveryFragment extends Fragment {
    private View recoveryView;
    private TextView tvBackPress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recoveryView = inflater.inflate(R.layout.fragment_recovery, container, false);

        initUI();
        backPRessHandler();

        return recoveryView;
    }

    private void backPRessHandler() {
        tvBackPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();

            }
        });
    }

    private void initUI() {
        tvBackPress = recoveryView.findViewById(R.id.btn_back);
    }

    public static RecoveryFragment newInstance() {
        RecoveryFragment fragment = new RecoveryFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
