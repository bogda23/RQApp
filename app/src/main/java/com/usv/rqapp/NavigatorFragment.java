package com.usv.rqapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.GeoPoint;
import com.usv.rqapp.databinding.FragmentNavigatorBinding;
import com.usv.rqapp.fragments.MapBoxFragment;
import com.usv.rqapp.fragments.MapsFragment;
import com.usv.rqapp.fragments.NewsFeedFragment;
import com.usv.rqapp.fragments.UserAccountFragment;


import java.util.ArrayList;


import devlight.io.library.ntb.NavigationTabBar;

public class NavigatorFragment extends Fragment {
    private static final String ARG_LOCATION_TITLE = "argLocationTitle";
    private static final String ARG_GEOPOINT = "argGeoPoint";

    private View navigatorView;
    public NavigationTabBar navigationTabBar;
    public ArrayList<NavigationTabBar.Model> models;
    private FragmentNavigatorBinding binding;
    private GeoPoint geoPointToSend;
    private String eventTitleToSend;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentNavigatorBinding.inflate(inflater, container, false);
        navigatorView = binding.getRoot();

        getLocationGeoPointFromNewsFeed();

        bottomBar();

        initUI();

        return navigatorView;
    }

    private void initUI() {

        binding.ntbBottomNavigation.setOnTabBarSelectedIndexListener(onTabBarSelectedIndexListener);
    }


    private void bottomBar() {
        models = new ArrayList<>();
        models.add(new NavigationTabBar.Model.Builder(ContextCompat.getDrawable(getContext(), R.drawable.user), Color.rgb(127, 96, 241)).build());
        models.add(new NavigationTabBar.Model.Builder(ContextCompat.getDrawable(getContext(), R.drawable.marker), Color.rgb(127, 96, 241)).build());
        models.add(new NavigationTabBar.Model.Builder(ContextCompat.getDrawable(getContext(), R.drawable.map_marker_dark), Color.rgb(127, 96, 241)).build());
        models.add(new NavigationTabBar.Model.Builder(ContextCompat.getDrawable(getContext(), R.drawable.feed_icon), Color.rgb(127, 96, 241)).build());
        binding.ntbBottomNavigation.setModels(models);
        binding.ntbBottomNavigation.setModelIndex(2, true);
        binding.ntbBottomNavigation.setBadgeSize(10);
        binding.ntbBottomNavigation.setIsTinted(true);
        binding.ntbBottomNavigation.setIsSwiped(true);
        binding.ntbBottomNavigation.setActiveColor(R.color.colorGrey);


    }

    private NavigationTabBar.OnTabBarSelectedIndexListener onTabBarSelectedIndexListener = new NavigationTabBar.OnTabBarSelectedIndexListener() {
        @Override
        public void onStartTabSelected(NavigationTabBar.Model model, int index) {

            switch (index) {
                case 0:

                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, UserAccountFragment.newInstance()).commit();
                    break;
                case 1:
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, MapsFragment.newInstance()).commit();


                    break;
                case 2:
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, MapBoxFragment.newInstance()).commit();


                    break;
                case 3:

                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, NewsFeedFragment.newInstance()).commit();
                    break;
            }


        }

        @Override
        public void onEndTabSelected(NavigationTabBar.Model model, int index) {

        }
    };

    public static NavigatorFragment newInstance() {
        NavigatorFragment fragment = new NavigatorFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }

    private void getLocationGeoPointFromNewsFeed() {
        if (getArguments() != null) {
            eventTitleToSend = getArguments().getString(ARG_LOCATION_TITLE);
            double[] arr = getArguments().getDoubleArray(ARG_GEOPOINT);
            geoPointToSend = new GeoPoint(arr[0], arr[1]);
            binding.ntbBottomNavigation.setModelIndex(2, true);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, MapBoxFragment.newInstanceWithGeoPoint(eventTitleToSend, geoPointToSend)).commit();
            return;
        } else {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, MapBoxFragment.newInstance()).commit();
        }
    }

    public static NavigatorFragment newInstanceWithGeoPoint(String titleEvent, GeoPoint geoPoint) {
        NavigatorFragment fragment = new NavigatorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_TITLE, titleEvent);
        args.putDoubleArray(ARG_GEOPOINT, new double[]{geoPoint.getLatitude(), geoPoint.getLongitude()});
        fragment.setArguments(args);

        return fragment;
    }
}
