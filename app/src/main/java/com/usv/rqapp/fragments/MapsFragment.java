package com.usv.rqapp.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.databinding.FragmentMapsBinding;

public class MapsFragment extends Fragment {

    private static FirebaseAuth auth;


    private GoogleMap map;
    private View rootView;
    private FragmentMapsBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();

        initFirebaseAuth();
        loadMap(savedInstanceState);
        signOutHandler();
        binding.map.setClipToOutline(true);


        return rootView;
    }

    private void initFirebaseAuth() {
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // todo:  setTextToUI();
        }
    }

    private void updateUI() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animationExit[0], CustomAnimation.animationExit[1]).
                replace(R.id.fragment_frame, LoginFragment.newInstance()).commit();
    }

    private void signOutHandler() {
        binding.imgReport.setOnClickListener(v -> {
            signOut();
        });
    }


    public void signOut() {
        // [START auth_sign_out]
        auth.signOut();
        AuthUI.getInstance().signOut(getContext()).addOnCompleteListener(task -> {
            try {
                finalize();
                Toast.makeText(getContext(), "Sign out", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                updateUI();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        // [END auth_sign_out]
    }

    private void loadMap(Bundle savedInstanceState) {

        binding.map.onCreate(savedInstanceState);

        binding.map.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                map = mMap;

                // For showing a move to my location button
                //  map.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng sydney = new LatLng(-34, 151);
                map.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.map.onDestroy();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.map.onLowMemory();
    }
}
