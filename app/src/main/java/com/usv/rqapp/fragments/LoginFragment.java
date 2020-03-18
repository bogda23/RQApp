package com.usv.rqapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.MapFragment;
import com.usv.rqapp.R;

public class LoginFragment extends Fragment {

    private TextView tvRegister;
    private TextView tvForgotPassword;
    private View loginView;
    private Button btnLogin;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        loginView = inflater.inflate(R.layout.fragment_login, container, false);

        /***/
        initUI();
        uiHandler();

        return loginView;
    }

    private void uiHandler() {
        registerHandler();
        forgotPasswordHandler();
        loginHandler();
    }

    private void loginHandler() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.fragment_frame, MapsFragment.newInstance()).commit();
            }
        });
    }

    private void forgotPasswordHandler() {
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.fragment_frame, RecoveryFragment.newInstance()).addToBackStack(null).commit();
            }
        });
    }

    private void registerHandler() {
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.beginTransaction().replace(R.id.fragment_frame, RegisterFragment.newInstance()).addToBackStack(null).commit();
            }
        });
    }


    private void initUI() {
        tvRegister = loginView.findViewById(R.id.tv_register);
        tvForgotPassword = loginView.findViewById(R.id.tv_forgot_password);
        btnLogin = loginView.findViewById(R.id.btn_login);
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
