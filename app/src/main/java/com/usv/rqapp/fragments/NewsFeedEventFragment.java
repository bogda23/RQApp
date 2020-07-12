package com.usv.rqapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.squareup.picasso.Picasso;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.controllers.ImageController;
import com.usv.rqapp.databinding.FragmentAddItemInNewsFeedBinding;
import com.usv.rqapp.models.firestoredb.NewsFeed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class NewsFeedEventFragment extends Fragment {
    private String TAG = "NewsFeedEventFragment";
    private final int REQUEST_CODE_START_ACTIVITY = 51;
    private final int REQUEST_CODE_START_READ_GALLERY = 10;


    private final int PICK_IMAGE_REQUEST = 1;
    private final String IMAGE_NAME = "1";
    private ImageController imageController;
    private ProgressDialog progressDialog;
    private Bitmap eventImage;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;

    private FragmentAddItemInNewsFeedBinding binding;
    private View newEventView;
    private FragmentManager manager;
    private FirebaseUser firebaseUser;
    private FirestoreController firestoreController;

    private PlacesClient placesClient;
    private AutocompleteSessionToken token;
    private List<AutocompletePrediction> predictionList;
    private GeoPoint eventLocation;
    private String placeName;

    private StorageReference storageRef;

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

        progressDialog = new ProgressDialog(getContext());
        iniFirestore();
        initFirestoreStorage();
        initImageController();

        handleChooseImageButton();


        initPlacesAPi();
        executeSearchComponents();

        getCurrentUser();

        handleBackButton();
        handleGetCurrentLocationButton();
        handleSendEventButton();

        return newEventView;
    }

    private void initImageController() {
        imageController = new ImageController(getContext());
    }

    private void handleChooseImageButton() {
        binding.btnChooseImage.setOnClickListener(click -> {
            checkStoragePermmissions();

        });
    }

    private void checkStoragePermmissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_START_READ_GALLERY);
            } else {
                openFileChosser();
            }
        } else {
            openFileChosser();
        }
    }


    private void openFileChosser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_START_READ_GALLERY);
            } else {

            }
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                eventImage = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            binding.imgImageUploadedValidation.setImageResource(R.drawable.icon_images);
            binding.imgImageUploadedValidation.setVisibility(View.VISIBLE);

        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode != RESULT_OK) {
            binding.imgImageUploadedValidation.setImageResource(R.drawable.report_light);
            binding.imgImageUploadedValidation.setVisibility(View.VISIBLE);
        }
    }

    private void initFirestoreStorage() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    /**
     *
     */
    private void initPlacesAPi() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity(), getResources().getString(R.string.google_api_key));
        placesClient = Places.createClient(getActivity().getApplicationContext());
        token = AutocompleteSessionToken.newInstance();

    }

    /**
     *
     */
    private void executeSearchComponents() {
        handleSearchAction();
        handleTextChangedOnSearch();
        getSuggestionsOnSearch();
    }

    /**
     *
     */
    private void handleSearchAction() {
        binding.materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                getActivity().startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    binding.materialSearchBar.disableSearch();
                    binding.materialSearchBar.clearSuggestions();
                    binding.materialSearchBar.setPlaceHolder(getString(R.string.introduceti_locatie));

                }
            }
        });
    }

    /**
     *
     */
    private void handleTextChangedOnSearch() {
        binding.materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();

                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                binding.materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!binding.materialSearchBar.isSuggestionsVisible()) {
                                    binding.materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.e("mytag", "prediction fetching task unsuccessful" + task.getException().getMessage());
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     *
     */
    private void getSuggestionsOnSearch() {
        binding.materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = binding.materialSearchBar.getLastSuggestions().get(position).toString();
                binding.materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.materialSearchBar.clearSuggestions();
                        //binding.materialSearchBar.disableSearch();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(binding.materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();

                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            eventLocation = new GeoPoint(latLngOfPlace.latitude, latLngOfPlace.longitude);
                            if (place.getName() != null) {
                                placeName = place.getName();
                            } else {
                                placeName = binding.materialSearchBar.getText();
                            }
                            Log.e("mytag", "Place found: " + place.getLatLng());
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.e("mytag", "place not found: " + e.getMessage());
                            Log.e("mytag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    private void iniFirestore() {
        firestoreController = new FirestoreController();
    }

    private void handleGetCurrentLocationButton() {
        binding.tvCurrentLocation.setOnClickListener(click -> {
            if (mLastKnownLocation == null) {
                createDeviceGpsLocationRequest();
            } else {
                binding.materialSearchBar.setText(mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                binding.materialSearchBar.setPlaceHolder(mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
            }
        });
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

            if (TextUtils.isEmpty(titlul)) {
                binding.edtEventTitle.setError(CONSTANTS.INVALID_EVENT_TITLE);
                binding.edtEventTitle.requestFocus();
            } else if (TextUtils.isEmpty(descriere)) {
                binding.edtEventDescription.setError(CONSTANTS.INVALID_EVENT_DESCRIPTION);
                binding.edtEventDescription.requestFocus();
            } else if (TextUtils.isEmpty(placeName)) {
                binding.materialSearchBar.setTextHintColor(getResources().getColor(R.color.colorRedOpacity_50));
                binding.materialSearchBar.setPlaceHolderColor(getResources().getColor(R.color.colorRedOpacity_50));
            } else if (placeName == null) {
                binding.materialSearchBar.setPlaceHolder(CONSTANTS.INEXISTENT_LOCATION);
                binding.materialSearchBar.setPlaceHolderColor(getResources().getColor(R.color.colorRedOpacity_50));
            } else if (eventImage == null) {
                Toast.makeText(getContext(), CONSTANTS.PUT_AN_IMAGE, Toast.LENGTH_LONG).show();
            } else {
                binding.btnAddEvent.setClickable(false);
                progressDialog.setMessage(CONSTANTS.ADDING_FEED_EVENT);
                progressDialog.show();
                Log.e(TAG, currentUser);

                NewsFeed feed = new NewsFeed(titlul, descriere, currentUser, DateHandler.getCurrentFirestoreTimestamp(), DateHandler.getCurrentFirestoreTimestamp(), 0, placeName, eventLocation, true);

                byte[] imageToStore = imageController.compressImage(eventImage);
                putImageInToFirebaseStorage(imageToStore, feed);

            }

        });
    }

    private void putImageInToFirebaseStorage(byte[] imageToStore, NewsFeed feed) {
        UploadTask imagePath = storageRef.child(NewsFeed.POSTARI).child(feed.id_postare + ".jpg").putBytes(imageToStore);
        imagePath.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendEventToFirebase(task, feed);
            } else {
                Log.e(TAG, task.getException().getMessage());
                Toast.makeText(getContext(), CONSTANTS.CAN_T_SAVE_DATA, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void sendEventToFirebase(Task<UploadTask.TaskSnapshot> task, NewsFeed feed) {
        if (task != null) {
            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(complete -> {
                if (complete.isSuccessful()) {
                    Uri download_uri = complete.getResult();
                    feed.setImg_url(String.valueOf(download_uri));
                    firestoreController.addNewsFeedToFireStore(feed.convertNewsFeedToMap(feed), manager);
                } else {
                    Log.e(TAG, "Download URL not found");
                }

            });

            progressDialog.dismiss();

        }

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
