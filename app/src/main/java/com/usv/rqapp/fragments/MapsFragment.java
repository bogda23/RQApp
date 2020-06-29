package com.usv.rqapp.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.controllers.RippleController;
import com.usv.rqapp.controllers.VibrationsServiceController;
import com.usv.rqapp.databinding.FragmentMapsBinding;
import com.usv.rqapp.models.rqdb.VibrationIDLocation;
import com.usv.rqapp.models.rqdb.VibrationObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapsFragment";
    private static final String ARG_LOCATION_TITLE = "argLocationTitle";
    private static final String ARG_GEOPOINT = "argGeoPoint";

    private final int REQUEST_CODE_START_ACTIVITY = 51;
    private final float DEFAULT_ZOOM = 15;

    private static FirebaseAuth auth;
    private GoogleMap map;
    private MapView mapView;
    private Marker mPositionMarker;
    private View rootView;
    private FragmentMapsBinding binding;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private AutocompleteSessionToken token;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private GroundOverlay groundOverlay11;

    private VibrationsServiceController vibrationsServiceController;
    private FragmentManager manager;


    private String eventTitleReceivedFromFeed;
    private LatLng eventGeoPointReceivedFromFeed;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        rootView = binding.getRoot();
        binding.map.setClipToOutline(true);

        getLocationGeoPointFromNewsFeed();
        initVibrationService();

        initFirebaseAuth();
        loadMap(savedInstanceState);

        initPlacesAPi();
        executeSearchComponents();

        addNewsFeedEventsToFirestore();


        return rootView;
    }

    private void getLocationGeoPointFromNewsFeed() {
        if (getArguments() != null) {
            eventTitleReceivedFromFeed = getArguments().getString(ARG_LOCATION_TITLE);
            double[] arr = getArguments().getDoubleArray(ARG_GEOPOINT);
            eventGeoPointReceivedFromFeed = new LatLng(arr[0], arr[1]);
        }
    }


    private void putMarkerOnEvent() {
        if (map != null && eventGeoPointReceivedFromFeed != null && eventTitleReceivedFromFeed != null) {
            map.addMarker(new MarkerOptions().position(eventGeoPointReceivedFromFeed).title(eventTitleReceivedFromFeed).snippet(eventTitleReceivedFromFeed));

            moveMapCameraToLatLng(eventGeoPointReceivedFromFeed, false);
        }
    }

    private void addNewsFeedEventsToFirestore() {

        binding.btnAddNewsFeedPost.setOnClickListener(click -> {
            FragmentOpener.loadNextFragmentWithStack(NewsFeedEventFragment.newInstance(), manager);
        });

    }

    private void initVibrationService() {

        VibrationObject vibrationObject = new VibrationObject(new VibrationIDLocation(23133.23223, 23323.23), 22.22, "Bulgaria", "BG", "Halhalhal");

        vibrationsServiceController = new VibrationsServiceController();
        vibrationsServiceController.putVibrationOnLocation(vibrationObject);
        if (vibrationsServiceController.getBaseVibrations() != null) {
            Log.e(TAG, vibrationsServiceController.getBaseVibrations().toString());
        }

    }


    /**
     * @param mLastKnownLocation
     * @param showAnimation
     * @param REPEAT_TIMES
     */
    private void rippleEfectOnMap(Location mLastKnownLocation, boolean showAnimation, @Nullable Integer REPEAT_TIMES) {
        if (showAnimation) {
            LatLng location = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    groundOverlay11 = map.addGroundOverlay(new
                            GroundOverlayOptions()
                            .position(location, 0)
                            .transparency(0.5f)
                            .image(BitmapDescriptorFactory.fromResource(R.drawable.ripple)));
                    RippleController.OverLay(groundOverlay11, showAnimation, REPEAT_TIMES);
                }
            }, 1000);
        } else {
            RippleController.OverLay(groundOverlay11, showAnimation, REPEAT_TIMES);
        }

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
                        binding.materialSearchBar.disableSearch();
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
                        Log.e("mytag", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM);
                            map.animateCamera(cameraUpdate);
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
                    // binding.materialSearchBar.disableSearch();
                    binding.materialSearchBar.clearSuggestions();
                }
            }
        });
    }

    /**
     *
     */
    private void handleCustomTrackLocationButton() {
        binding.imgCustomLocation.setOnClickListener(click -> {

            if (binding.materialSearchBar.isSuggestionsVisible()) {
                binding.materialSearchBar.clearSuggestions();
            }
            if (binding.materialSearchBar.isSearchEnabled()) {
                binding.materialSearchBar.disableSearch();
            }
            if (mLastKnownLocation != null) {
                moveMapCameraToLocation(mLastKnownLocation, false);
            } else {
                Toast.makeText(getActivity(), "Te rugăm să activezi locația", Toast.LENGTH_LONG).show();
                createDeviceGpsLocationRequest();
            }
        });
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
    private void initFirebaseAuth() {
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        manager = getFragmentManager();
        if (currentUser == null) {
            // todo:  setTextToUI();
        }
    }

    /**
     * @param savedInstanceState
     */
    private void loadMap(Bundle savedInstanceState) {
        binding.map.onCreate(savedInstanceState);
        binding.map.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
            mapView = binding.map;
            mapView.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }

    /**
     * @param titleEvent
     * @param geoPoint
     */
    public static MapsFragment newInstanceWithGeoPoint(String titleEvent, GeoPoint geoPoint) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_TITLE, titleEvent);
        args.putDoubleArray(ARG_GEOPOINT, new double[]{geoPoint.getLatitude(), geoPoint.getLongitude()});
        fragment.setArguments(args);

        return fragment;
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();

    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.map.onDestroy();
        binding = null;
    }

    /**
     *
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.map.onLowMemory();
    }


    /**
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity().getApplicationContext(), CONSTANTS.PERMISSION_DENIED, Toast.LENGTH_LONG).show();
            return;
        }
        map = googleMap;
        addCustomStyleOnMap();
        createDeviceGpsLocationRequest();
        handleCustomTrackLocationButton();

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (binding.materialSearchBar.isSuggestionsVisible())
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.materialSearchBar.clearSuggestions();
                        }
                    }, 1000);
                if (binding.materialSearchBar.isSearchEnabled())
                    binding.materialSearchBar.disableSearch();
                return false;
            }
        });

        putMarkerOnEvent();

     /*   LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
*/
        /**-------------------------------------------------------------------------------------------------------------------- */
       /* LatLng geoPoint = new LatLng(locationUtils.getLocation().getLatitude(), locationUtils.getLocation().getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(geoPoint).title("Sunt aici!");
        map.animateCamera(CameraUpdateFactory.newLatLng(geoPoint));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 5));
        map.addMarker(markerOptions);*/

    }

    /**
     *
     */
    private void addCustomStyleOnMap() {
        try {
            boolean success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity().getApplicationContext(), R.raw.dark_barcelona));
            if (!success) {
                Log.e(TAG, "Maps style failed to load");
            }
        } catch (Resources.NotFoundException rs) {
            Log.e(TAG, "Maps style not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
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
        setDeviceGpsLocationVisibility();
    }

    /**
     *
     */
    @SuppressLint("MissingPermission")
    private void setDeviceGpsLocationVisibility() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);

    }


    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_START_ACTIVITY) {
            if (resultCode == getActivity().RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    /**
     *
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();
                    if (mLastKnownLocation != null && eventGeoPointReceivedFromFeed == null) {
                        moveMapCameraToLocation(mLastKnownLocation, true);
                    } else {
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
                                if (eventGeoPointReceivedFromFeed == null) {
                                    moveMapCameraToLocation(mLastKnownLocation, true);
                                }
                                mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };

                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Toast.makeText(getActivity(), "unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @param mLastKnownLocation
     * @param ripple
     */
    private void moveMapCameraToLocation(Location mLastKnownLocation, boolean ripple) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
        map.animateCamera(cameraUpdate);
        rippleEfectOnMap(mLastKnownLocation, ripple, 1);

    }

    private void moveMapCameraToLatLng(LatLng latLng, boolean ripple) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom((latLng), DEFAULT_ZOOM);
        map.animateCamera(cameraUpdate);
        rippleEfectOnMap(mLastKnownLocation, ripple, 1);

    }

    /**
     * @param location
     */
    public void locationChanged(Location location) {

        if (location == null)
            return;

        if (mPositionMarker == null) {

            mPositionMarker = map.addMarker(new MarkerOptions()
                    .flat(true)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.marker))
                    .anchor(0.5f, 0.5f)
                    .position(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude())));
        }

        animateMarker(mPositionMarker, location); // Helper method for smooth
        // animation

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location
                .getLatitude(), location.getLongitude())));

    }


    /**
     * @param marker
     * @param location
     */
    public void animateMarker(final Marker marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * location.getLongitude() + (1 - t)
                        * startLatLng.longitude;
                double lat = t * location.getLatitude() + (1 - t)
                        * startLatLng.latitude;

                float rotation = (float) (t * location.getBearing() + (1 - t)
                        * startRotation);

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(rotation);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}
