package com.usv.rqapp.interfaces;

import com.google.firebase.firestore.GeoPoint;

public interface NewsFeedFragmentListener {
    public final static String TAG = "NewsFeedLocationListener";

    void onLocationPressed(String titleEvent,GeoPoint geoPoint);
}
