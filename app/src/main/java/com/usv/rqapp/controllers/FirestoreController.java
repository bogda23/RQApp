package com.usv.rqapp.controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usv.rqapp.databinding.FragmentUserAccountBinding;
import com.usv.rqapp.models.db.User;

import java.util.HashMap;
import java.util.Map;

public class FirestoreController {

    private static final String TAG = "DbController";
    private FirebaseFirestore db;
    private String userID;

    /**
     *
     */
    public FirestoreController() {
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

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * @param user
     * @return
     */
/*    public Boolean checkIfUserExists(User user) {
        final Boolean[] userExists = {false};
        db.collection(User.UTILIZATORI)
                .whereEqualTo(User.ID_UTILIZATOR, user.getId_utilizator())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (Objects.requireNonNull(task.getResult()).getDocuments().size() > 0) {
                            userExists[0] = true;
                            Log.e(TAG, String.format("Userul exista---->>>>>>> %s", task.getResult().getDocuments().toString()));
                        }
                    }
                }).addOnFailureListener(e -> {
            Log.e(TAG, "Userul NU exista ia eroare---->>>>>>> ");
            e.printStackTrace();
            userExists[0] = false;

        });
        return userExists[0];
    }*/

}
