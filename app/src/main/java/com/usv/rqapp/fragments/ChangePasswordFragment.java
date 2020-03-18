package com.usv.rqapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usv.rqapp.R;

public class ChangePasswordFragment extends Fragment {

    private View changePasswordView;
    private TextView tvBackPress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        changePasswordView = inflater.inflate(R.layout.fragment_change_password, container, false);
        initUI();
        backPressHandler();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initUI() {
        tvBackPress = changePasswordView.findViewById(R.id.btn_back);
    }

    private void backPressHandler() {
        tvBackPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();

            }
        });
    }


}
