package com.usv.rqapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.usv.rqapp.databinding.ActivityMainBinding;
import com.usv.rqapp.fragments.LoginFragment;
import com.usv.rqapp.fragments.WelcomeFragment;
import com.usv.rqapp.interfaces.DataInterface;
import com.usv.rqapp.network.MyReceiver;

public class MainActivity extends AppCompatActivity implements DataInterface {
    private BroadcastReceiver MyReceiver = null;
    private String networkStatus;
    private ActivityMainBinding binding;


    private TextView tvError;
    private ConstraintLayout clRoot;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);

        broadcastIntent();
        //  getWindow().setStatusBarColor(getColor(R.color.colorGreyLight));
        manageFragments();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(MyReceiver);
    }

    public void broadcastIntent() {
        MyReceiver = new MyReceiver();
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void manageFragments() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new WelcomeFragment()).commit();
       //  getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, new LoginFragment()).commit();

    }


    @Override
    public void networkStatusEvent(String status) {
        networkStatus = status;
        if (status == CONSTANTS.NO_INTERNET) {
            CustomAnimation.applyAnimationTo(binding.tvInternetStatus, R.anim.top_to_middle);
            binding.tvInternetStatus.setVisibility(View.VISIBLE);


        } else {
            CustomAnimation.applyAnimationTo(binding.tvInternetStatus, R.anim.middle_to_top);
            binding.tvInternetStatus.setVisibility(View.GONE);
        }

    }


}

