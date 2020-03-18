package com.usv.rqapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.usv.rqapp.fragments.LoginFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manageFragments();
    }

    private void manageFragments() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new LoginFragment()).commit();

    }


}

