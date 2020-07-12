package com.usv.rqapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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
import com.google.firebase.firestore.GeoPoint;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.usv.rqapp.CONSTANTS;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.databinding.FragmentMapboxBinding;
import com.usv.rqapp.interfaces.IVibrationSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


public class MapBoxFragment extends Fragment implements OnMapReadyCallback, PermissionsListener, OnCameraTrackingChangedListener {
    private static final String TAG = "MapBoxFragment";
    private static final String ARG_LOCATION_TITLE = "argLocationTitle";
    private static final String ARG_GEOPOINT = "argGeoPoint";

    private static final String SAVED_STATE_CAMERA = "saved_state_camera";
    private static final String SAVED_STATE_RENDER = "saved_state_render";
    private static final String SAVED_STATE_LOCATION = "saved_state_location";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 10;

    @CameraMode.Mode
    private int cameraMode = CameraMode.TRACKING;
    @RenderMode.Mode
    private int renderMode = RenderMode.NORMAL;

    private final int REQUEST_CODE_START_ACTIVITY = 61;
    private final float DEFAULT_ZOOM = 15;

    private FragmentMapboxBinding binding;
    private View mapBoxView;
    private FragmentManager manager;

    private MapboxMap map;
    private LocationComponent locationComponent;

    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;

    private Location lastLocation;
    private LatLng destinationLatLng;
    private LocationCallback locationCallback;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private AutocompleteSessionToken token;

    private String eventTitleReceivedFromFeed;
    private LatLng eventGeoPointReceivedFromFeed;

    private PlaceAutocompleteFragment autocompleteFragment;

    //Vibrations service
    private IVibrationSender vibrationSender;
    private Boolean isVibrationSystemActive = false;
    private MapboxNavigation navigation;


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity().getApplicationContext(), CONSTANTS.PERMISSION_DENIED, Toast.LENGTH_LONG).show();
            return;
        }
        map = mapboxMap;
        map.setStyle(new Style.Builder().fromUri("mapbox://styles/bogda23/ckc0k7qup0j1j1js7ach9jiem"));

        createDeviceGpsLocationRequest();
        handleCustomTrackLocationButton();
        putMarkerOnEvent();
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);

                handleStartRoadTripButton();
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        binding = FragmentMapboxBinding.inflate(inflater, container, false);
        mapBoxView = binding.getRoot();

        /** Put your functions here*/
        getLocationGeoPoint();

        initMapBox(savedInstanceState);
        restoreLastKnownDataForMapBox(savedInstanceState);

        initPlacesAPi();
        loadPlaces(savedInstanceState);
        executeSearchComponents();

        addNewsFeedEventsToFirestore();
        addFavoriteLocation();


        return mapBoxView;
    }


    private void initMapBox(Bundle savedInstanceState) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        binding.mapBox.onCreate(savedInstanceState);
        binding.mapBox.getMapAsync(this);
    }

    private void initPlacesAPi() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity(), getResources().getString(R.string.google_api_key));
        placesClient = Places.createClient(getActivity().getApplicationContext());
        token = AutocompleteSessionToken.newInstance();

    }

    private void addFavoriteLocation() {
        binding.fabLocationFavorite.setOnClickListener(click -> {
            if (lastLocation != null) {
                FragmentOpener.loadNextFragmentWithStack(FavoriteEventFragment.newInstance(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude())), manager);
            }
        });
    }

    private void handleStartRoadTripButton() {
        binding.btnStartRoadTrip.setOnClickListener(click -> {
            if (destinationLatLng != null) {
                createRouteBetweenMeAndDestionatio(destinationLatLng);
                if (currentRoute != null) {
                    boolean simulateRoute = false;
                    NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                            .directionsRoute(currentRoute)
                            .shouldSimulateRoute(simulateRoute)
                            .build();


                    // Call this method with Context from within an Activity
                    handleVibrationPermissions(options);

                }
            } else {
                binding.btnStartRoadTrip.setVisibility(View.GONE);
            }
        });
    }

    public void handleVibrationPermissions(NavigationLauncherOptions options) {

        //Initializarea listener-ului
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(CONSTANTS.VIBRATIONS_ACCESS);
        dialog.setMessage(getString(R.string.vibration_permission_message));
        dialog.setPositiveButton(CONSTANTS.ACCEPTA, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isVibrationSystemActive = true;
                vibrationSender.onVibrationSignalListener(isVibrationSystemActive);
                startNavigationWithOptions(options);
            }
        });
        dialog.setNegativeButton(CONSTANTS.NU_ACCEPT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startNavigationWithOptions(options);
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();

    }

    private void startNavigationWithOptions(NavigationLauncherOptions options) {
        if (currentRoute.distance() > 1) {
            NavigationLauncher.startNavigation(getActivity(), options);
        } else {
            binding.btnStartRoadTrip.setVisibility(View.GONE);
        }
    }

    private void executeSearchComponents() {
        handleSearchAction();
        handleTextChangedOnSearch();
        getSuggestionsOnSearch();
    }

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
                        LatLng latLngOfPlace = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                        if (latLngOfPlace != null) {

                            clearAllMarkersFromMap();

                            setDestination(latLngOfPlace);
                            addMarkerToPlace(latLngOfPlace, place.getName(), place.getAddress());
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

    private void setDestination(LatLng latLngOfPlace) {
        moveMapCameraToLatLng(latLngOfPlace);
        destinationLatLng = latLngOfPlace;
        binding.btnStartRoadTrip.setVisibility(View.VISIBLE);
    }

    private void clearAllMarkersFromMap() {
        if (navigationMapRoute != null) {
            navigationMapRoute.removeRoute();
            destinationLatLng = null;
        }
        map.clear();
        binding.btnStartRoadTrip.setVisibility(View.GONE);
    }

    private void addMarkerToPlace(LatLng latLngOfPlace, String title, String address) {
        map.addMarker(new MarkerOptions().position(latLngOfPlace).title(title).snippet(address));
    }

    private void createRouteBetweenMeAndDestionatio(LatLng destination) {

        Point destinationPoint = Point.fromLngLat(destination.getLongitude(), destination.getLatitude());
        Point originPoint = Point.fromLngLat(lastLocation.getLongitude(),
                lastLocation.getLatitude());

        GeoJsonSource source = map.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        // binding.btnCloudRain.setEnabled(true);
        //  binding.btnCloudRain.setBackgroundResource(R.color.colorGreen);
    }

    private void loadPlaces(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            autocompleteFragment = PlaceAutocompleteFragment.newInstance(getString(R.string.access_token));

            if (autocompleteFragment != null && manager != null) {
                manager.beginTransaction().add(R.id.fragment_frame, autocompleteFragment, "PlaceAutocompleteFragment").commit();
            }
        } else {
            if (manager.findFragmentByTag("PlaceAutocompleteFragment") != null) {
                autocompleteFragment = (PlaceAutocompleteFragment)
                        manager.findFragmentByTag("PlaceAutocompleteFragment");
            }
        }

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(CarmenFeature carmenFeature) {
                Toast.makeText(getContext(),
                        carmenFeature.text(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                manager.popBackStackImmediate();
            }
        });
    }

    private void restoreLastKnownDataForMapBox(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            cameraMode = savedInstanceState.getInt(SAVED_STATE_CAMERA);
            renderMode = savedInstanceState.getInt(SAVED_STATE_RENDER);
            lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
        }
    }

    private void addNewsFeedEventsToFirestore() {

        binding.btnAddNewsFeedPost.setOnClickListener(click -> {
            FragmentOpener.loadNextFragmentWithStack(NewsFeedEventFragment.newInstance(), manager);
        });

    }

    private void moveMapCameraToLocation(Location mLastKnownLocation) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
      //  map.animateCamera(cameraUpdate, 900);
        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                        .zoom(DEFAULT_ZOOM)
                        .build()), 1500);

    }

    private void moveMapCameraToLatLng(LatLng latLng) {
        if (map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom((latLng), DEFAULT_ZOOM);
            map.animateCamera(cameraUpdate);

        }
    }

    private void putMarkerOnEvent() {
        if (map != null && eventGeoPointReceivedFromFeed != null && eventTitleReceivedFromFeed != null) {
            map.clear();
            map.addMarker(new MarkerOptions().position(eventGeoPointReceivedFromFeed).title(eventTitleReceivedFromFeed).snippet(eventTitleReceivedFromFeed));
            moveMapCameraToLatLng(eventGeoPointReceivedFromFeed);
        }
    }

    private void getLocationGeoPoint() {
        if (getArguments() != null) {
            eventTitleReceivedFromFeed = getArguments().getString(ARG_LOCATION_TITLE);
            double[] arr = getArguments().getDoubleArray(ARG_GEOPOINT);
            eventGeoPointReceivedFromFeed = new LatLng(arr[0], arr[1]);
            setDestination(eventGeoPointReceivedFromFeed);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

            // Enable the most basic pulsing styling by ONLY using
            // the `.pulseEnabled()` method
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(getContext())
                    .pulseEnabled(true)
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = map.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    lastLocation = task.getResult();
                    if (lastLocation != null) {
                        moveCameraDynamically();
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
                                lastLocation = locationResult.getLastLocation();

                                moveCameraDynamically();

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

    private void handleCustomTrackLocationButton() {
        binding.imgCustomLocation.setOnClickListener(click -> {

            if (binding.materialSearchBar.isSuggestionsVisible()) {
                binding.materialSearchBar.clearSuggestions();
            }
            if (binding.materialSearchBar.isSearchEnabled()) {
                binding.materialSearchBar.disableSearch();
            }
            if (lastLocation != null) {
                moveMapCameraToLocation(lastLocation);
            } else {
                Toast.makeText(getActivity(), "Te rugăm să activezi locația", Toast.LENGTH_LONG).show();
                createDeviceGpsLocationRequest();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setDeviceGpsLocationVisibility() {
        map.getUiSettings().setLogoEnabled(false);
        map.getUiSettings().setCompassEnabled(false);

    }

    private void moveCameraDynamically() {
        if (eventGeoPointReceivedFromFeed == null) {
            moveMapCameraToLocation(lastLocation);
        } else {
            moveMapCameraToLatLng(eventGeoPointReceivedFromFeed);
        }
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(getContext())
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        if (response.isSuccessful()) {
                            currentRoute = response.body().routes().get(0);
                        }

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, binding.mapBox, map, R.style.NavigationMapRoute);
                        }

                        if (currentRoute != null) {
                            navigationMapRoute.addRoute(currentRoute);
                        }

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    /***-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

    @Override
    public void onCameraTrackingDismissed() {

    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), CONSTANTS.PERMISSION_DENIED_MESSAGE, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getContext(), CONSTANTS.PERMISSION_DENIED_MESSAGE, Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IVibrationSender) {
            vibrationSender = (IVibrationSender) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement " + IVibrationSender.TAG);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        vibrationSender = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapBox.onResume();
        isVibrationSystemActive = false;
        vibrationSender.onVibrationSignalListener(isVibrationSystemActive);
    }

    @Override
    public void onStart() {
        super.onStart();
        manager = getActivity().getSupportFragmentManager();
        binding.mapBox.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        binding.mapBox.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapBox.onPause();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapBox.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapBox.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapBox.onSaveInstanceState(outState);

        outState.putInt(SAVED_STATE_CAMERA, cameraMode);
        outState.putInt(SAVED_STATE_RENDER, renderMode);
        if (locationComponent != null) {
            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
        }
    }


    public static MapBoxFragment newInstance() {
        MapBoxFragment fragment = new MapBoxFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }

    public static MapBoxFragment newInstanceWithGeoPoint(String titleEvent, GeoPoint geoPoint) {
        MapBoxFragment fragment = new MapBoxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_TITLE, titleEvent);
        args.putDoubleArray(ARG_GEOPOINT, new double[]{geoPoint.getLatitude(), geoPoint.getLongitude()});
        fragment.setArguments(args);

        return fragment;
    }
}
