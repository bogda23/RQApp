package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.firestore.GeoPoint;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.databinding.FragmentAddFavoriteItemBinding;
import com.usv.rqapp.models.firestoredb.FavoriteLocation;

public class FavoriteEventFragment extends Fragment {

    private static final String TAG = "FavoriteEventFragment";
    private static final String ARG_GEOPOINT = "argGeoPoint";

    private FragmentAddFavoriteItemBinding binding;
    private View favItemView;
    private FragmentManager manager;

    private FirestoreController db;
    private GeoPoint geoPointReceivedFromMap;


    @Override
    public void onStart() {
        super.onStart();
        manager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddFavoriteItemBinding.inflate(inflater, container, false);
        favItemView = binding.getRoot();

        iniFirestore();

        getLocationGeoPointFromMapBox();
        handleBackButton();
        handleSendEventButton();


        return favItemView;
    }

    /**
     * Inițializează controlerul bazei de date
     */
    private void iniFirestore() {
        db = new FirestoreController();
    }

    /**
     * Primește locația de la fragmentul Mapbox pentru a salva locația favorită
     */
    private void getLocationGeoPointFromMapBox() {
        if (getArguments() != null) {
            double[] arr = getArguments().getDoubleArray(ARG_GEOPOINT);
            geoPointReceivedFromMap = new GeoPoint(arr[0], arr[1]);
        }
    }

    /**
     *  Înapoi la fragmentul anterior
     */
    private void handleBackButton() {
        binding.btnBack.setOnClickListener(click -> {
            manager.popBackStackImmediate();
        });
    }

    /**
     * Adaugă locătia curentă  în baza de date și verifică titlul
     */
    private void handleSendEventButton() {

        binding.btnAddFavLocation.setOnClickListener(click -> {
            String titlul = binding.edtLocationTitle.getText().toString();

            if (TextUtils.isEmpty(titlul)) {
                binding.edtLocationTitle.setError(CONSTANTS.INVALID_EVENT_TITLE);
                binding.edtLocationTitle.requestFocus();
            } else if (geoPointReceivedFromMap == null) {
                Toast.makeText(getContext(), CONSTANTS.LOCATION_NOT_RECEIVED, Toast.LENGTH_LONG).show();
            } else {
                binding.btnAddFavLocation.setClickable(false);
                binding.progressBarHolder.setVisibility(View.VISIBLE);

                addFavoriteLocationToFirestore(titlul);

            }

        });
    }

    /**
     *  Adaugă în baza de date locația favorită
     * @param titlul Titlul pus pentru locația adăugată
     */
    private void addFavoriteLocationToFirestore(String titlul) {
        FavoriteLocation favLocation = new FavoriteLocation(titlul, DateHandler.getCurrentFirestoreTimestamp(), geoPointReceivedFromMap);
        db.addFavoriteLocationToFirestore(favLocation, manager);

    }

    /**
     * @return
     */
    public static FavoriteEventFragment newInstance(GeoPoint geoPoint) {
        FavoriteEventFragment fragment = new FavoriteEventFragment();
        Bundle args = new Bundle();
        args.putDoubleArray(ARG_GEOPOINT, new double[]{geoPoint.getLatitude(), geoPoint.getLongitude()});
        fragment.setArguments(args);
        return fragment;
    }
}
