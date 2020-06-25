package com.usv.rqapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.databinding.FragmentAddItemInNewsFeedBinding;
import com.usv.rqapp.models.firestoredb.NewsFeed;

import java.util.HashMap;
import java.util.Map;

public class NewsFeedEventFragment extends Fragment {
    private String TAG = "NewsFeedEventFragment";
    private final int REQUEST_CODE_START_ACTIVITY = 51;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;

    private FragmentAddItemInNewsFeedBinding binding;
    private View newEventView;
    private FragmentManager manager;
    private FirebaseUser firebaseUser;
    private FirestoreController firestoreController;


    @Override
    public void onStart() {
        super.onStart();
        manager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentAddItemInNewsFeedBinding.inflate(inflater, container, false);
        newEventView = binding.getRoot();

        initCurrentLocation();
        iniFirestore();

        getCurrentUser();

        handleBackButton();
        handleGetCurrentLocationButton();
        handleSendEventButton();

        return newEventView;
    }

    private void iniFirestore() {
        firestoreController = new FirestoreController();
    }

    private void handleGetCurrentLocationButton() {
        binding.tvCurrentLocation.setOnClickListener(click -> {
            if (mLastKnownLocation == null) {
                createDeviceGpsLocationRequest();
            } else {
                binding.edtEventLocation.setText(mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
            }
        });
    }


    private void initCurrentLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }


    private void getCurrentUser() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void handleSendEventButton() {
        String currentUser = firebaseUser.getDisplayName();
        binding.edtEventUtilizator.setText(currentUser);

        binding.btnAddEvent.setOnClickListener(click -> {
            String titlul = binding.edtEventTitle.getText().toString();
            String descriere = binding.edtEventDescription.getText().toString();
            String location = binding.edtEventLocation.getText().toString();

            if (TextUtils.isEmpty(titlul)) {
                binding.edtEventTitle.setError(CONSTANTS.INVALID_EVENT_TITLE);
                binding.edtEventTitle.requestFocus();
            } else if (TextUtils.isEmpty(descriere)) {
                binding.edtEventDescription.setError(CONSTANTS.INVALID_EVENT_DESCRIPTION);
                binding.edtEventDescription.requestFocus();
            } else if (TextUtils.isEmpty(location)) {
                binding.edtEventLocation.setError(CONSTANTS.INVALID_EVENT_DESCRIPTION);
                binding.edtEventLocation.requestFocus();
            } else {
                Log.e(TAG, currentUser);
                NewsFeed feed = new NewsFeed(titlul, descriere, currentUser, DateHandler.getCurrentFirestoreTimestamp(), DateHandler.getCurrentFirestoreTimestamp(), 0, location, true);
                sendEventToFirebase(feed.convertUsereToMap(feed));
            }

        });
    }

    private void sendEventToFirebase(Map<String, Object> map) {
        firestoreController.addNewsFeedToFireStore(map, manager);
    }


    private void handleBackButton() {
        binding.btnBack.setOnClickListener(click -> {
            manager.popBackStackImmediate();
        });
    }

    private void createDeviceGpsLocationRequest() {
        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Location Request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());


        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();                /** GPS Enabled*/

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {   /** Location is enabled by user or not*/
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;

                    try {
                        resolvable.startResolutionForResult(getActivity(), REQUEST_CODE_START_ACTIVITY);

                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    } catch (Exception e2) {
                        e2.getMessage();
                    }
                }
            }
        });
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();
                    if (mLastKnownLocation == null) {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult == null) {
                                    return;
                                }
                                mLastKnownLocation = locationResult.getLastLocation();
                                mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };

                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Toast.makeText(getActivity(), "unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static NewsFeedEventFragment newInstance() {
        NewsFeedEventFragment fragment = new NewsFeedEventFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }


}
