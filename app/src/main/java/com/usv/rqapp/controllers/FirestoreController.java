package com.usv.rqapp.controllers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.models.firestoredb.FavoriteLocation;
import com.usv.rqapp.models.firestoredb.NewsFeed;
import com.usv.rqapp.models.firestoredb.User;

import org.intellij.lang.annotations.RegExp;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.RegEx;

import kotlin.text.Regex;
import timber.log.Timber;

public class FirestoreController {

    private static final String TAG = "DbController";
    private FirebaseFirestore db;
    private String userID;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseStorage storage;

    /**
     *
     */
    public FirestoreController() {
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
    }


    /**
     * @param colectie
     * @param map
     * @return
     */
    public Boolean addUserToFireStore(String colectie, Map<String, Object> map) {
        final Boolean[] dataStored = {false};
        db.collection(colectie).document(map.get(User.ID_UTILIZATOR).toString())
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.e(TAG, "DocumentSnapshot added with ID: " + map.get(User.ID_UTILIZATOR));
                        dataStored[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document to firestore", e);
                        dataStored[0] = false;
                    }
                });
        return dataStored[0];
    }


    /**
     * @param manager
     * @param map
     * @return
     */
    public Boolean addNewsFeedToFireStore(Map<String, Object> map, FragmentManager manager) {
        final Boolean[] dataStored = {false};
        db.collection(NewsFeed.POSTARI).document(map.get(NewsFeed.ID_POSTARE).toString())
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.e(TAG, "DocumentSnapshot added with ID: " + map.get(NewsFeed.APRECIERI));
                        FragmentOpener.loadNextFragment(NavigatorFragment.newInstance(), manager);
                        dataStored[0] = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document to firestore", e);
                        dataStored[0] = false;
                    }
                });
        return dataStored[0];
    }


    /**
     * @param favLocation
     * @param manager
     */
    public void addFavoriteLocationToFirestore(FavoriteLocation favLocation, FragmentManager manager) {

        Map<String, Object> favLocationMap = favLocation.convertFavotiteLocationToMap(favLocation);

        db.collection(FavoriteLocation.LOC_FAVORITE).document(firebaseUser.getUid()).collection(FavoriteLocation.LOCATII).document(favLocation.getId_locatie())
                .set(favLocationMap).addOnCompleteListener(complete -> {
            if (complete.isSuccessful()) {
                Timber.e("DocumentSnapshot added with ID: " + favLocationMap.get(FavoriteLocation.ID_LOCATIE));
                FragmentOpener.loadNextFragment(NavigatorFragment.newInstance(), manager);
            } else {
                Timber.e(complete.getException().getMessage(), "Error adding document to firestore");
            }
        });
    }


    /**
     * @param user
     */
    public void updateUserDateOdBirthInFirestore(User user) {
        if (user.getId_utilizator() != null) {
            DocumentReference documentReference = db.collection(User.UTILIZATORI).document(user.getId_utilizator());
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Log.e(TAG + " User_exists", "Date of birgh " + user.getData_nastere());
                        userID = documentSnapshot.getId();
                        user.setFirstTime(false);
                        Map<String, Object> map = new HashMap<>();
                        map.put(User.DATA_NASTERE, user.getData_nastere());
                        db.collection(User.UTILIZATORI).document(user.getId_utilizator()).update(map);

                    } else {
                        Log.e(TAG + " User_exists", "Date of birth Empty");

                    }
                }
            })
                    .addOnFailureListener(e -> {
                        Log.e(TAG + "_", e.getMessage());
                    });
        }
    }


    /**
     * @param imageUrl
     * @param id_postare
     * @Description Sterge evenimentul si like-urile acestuia
     */
    public void deleteEventOnNewsFeed(String id_postare, String imageUrl) {

        //Delete likes
        db.collection(NewsFeed.APRECIERI).document(id_postare).delete().addOnCompleteListener(likeDelete -> {
            if (likeDelete.isSuccessful()) {
                Log.e(TAG, "deleteEventOnNewsFeed: Stergere like-uri cu succes");

            } else {
                Log.e(TAG, "deleteEventOnNewsFeed: Stergerea like-urilor fără succes");
                Log.e(TAG, "deleteEventOnNewsFeed: " + likeDelete.getException().getMessage());
            }
        });

        // Delete Image
        storage.getReferenceFromUrl(imageUrl).delete().addOnCompleteListener(deleteImage -> {

            if (deleteImage.isSuccessful()) {
                Log.e(TAG, "deleteEventOnNewsFeed: Stergere imagine cu succes");
            } else {
                Log.e(TAG, "deleteEventOnNewsFeed: Stergere imagine fără succes");
            }
        });
        //Delete post
        db.collection(NewsFeed.POSTARI).document(id_postare).delete().addOnCompleteListener(delete -> {
            if (delete.isSuccessful()) {
                Log.e(TAG, "deleteEventOnNewsFeed: Stergerea postare cu succes");

            } else {
                Log.e(TAG, "deleteEventOnNewsFeed: Stergerea postarii fără succes!");
            }
        });

    }

    /**
     * @param id_locatie
     */
    public void deleteFavoriteLocationFromFirestore(String id_locatie) {
        db.collection(FavoriteLocation.LOC_FAVORITE).document(getFirebaseUser().getUid()).collection(FavoriteLocation.LOCATII).document(id_locatie).delete().addOnCompleteListener(deleteLocation -> {
            if (deleteLocation.isSuccessful()) {
                Log.e(TAG, "handleDeleteButton: Locația favorită a fost ștersă cu succes");
            } else {
                Log.e(TAG, "handleDeleteButton: " + deleteLocation.getException().getMessage());
            }
        });
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public void addUpvoteToFirestore() {
        DocumentReference documentReference = db.collection(NewsFeed.POSTARI).document();
    }


}
