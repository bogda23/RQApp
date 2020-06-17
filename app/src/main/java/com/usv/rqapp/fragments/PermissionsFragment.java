package com.usv.rqapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.R;
import com.usv.rqapp.databinding.FragmentPermissionsBinding;

public class PermissionsFragment extends Fragment {
    private static final String TAG = "PermissionsFragment";
    private View permissionsView;
    private FragmentPermissionsBinding binding;
    private FragmentManager manager;
    private Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = getFragmentManager();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false);
        permissionsView = binding.getRoot();
        activity = getActivity();

        showContent(true);
        handlePermissions();
        return permissionsView;
    }

    private void showContent(boolean show) {
        if (!show) {
            binding.permissionContent.setVisibility(View.GONE);
            binding.btnGrant.setVisibility(View.GONE);
        } else {
            binding.permissionContent.setVisibility(View.VISIBLE);
            binding.btnGrant.setVisibility(View.VISIBLE);
        }
    }

    private void loadNavigatorFragment(){
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, NavigatorFragment.newInstance()).commit();
    }


    private void countDownEvent() {
        new CountDownTimer(1000, 100) {

            public void onTick(long millisUntilFinished) {
                showContent(false);
                binding.progressBarHolder.setVisibility(View.VISIBLE);
            }

            public void onFinish() {
                binding.progressBarHolder.setVisibility(View.GONE);
                loadNavigatorFragment();
            }
        }.start();
    }

    public void handlePermissions() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            countDownEvent();
            return;
        }

        binding.btnGrant.setOnClickListener(click -> {
            Dexter.withContext(getActivity().getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                    // TODO: 6/15/2020 SHOW BUTTON CUREENT LOCATION ON MAP
                    Log.e(TAG, "Merg permisiuneile");
                    binding.progressBarHolder.setVisibility(View.GONE);
                    loadNavigatorFragment();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    if (response.isPermanentlyDenied()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(CONSTANTS.PERMISSION_DENIED).setMessage(CONSTANTS.PERMISSION_DENIED_MESSAGE).setNegativeButton("Cancel", null).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getContext().getPackageName(), PermissionsFragment.TAG));
                                Log.e(TAG, "E ok");
                            }
                        }).show();
                    } else {
                        Toast.makeText(getContext(), CONSTANTS.PERMISSION_DENIED, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Permisiuni respinse");
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken token) {
                    token.continuePermissionRequest();

                }
            }).check();
        });


    }


    public static PermissionsFragment newInstance() {
        PermissionsFragment fragment = new PermissionsFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
