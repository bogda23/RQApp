package com.usv.rqapp.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usv.rqapp.data.db.User;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DbController {

    private static final String TAG = "DbController";
    private FirebaseFirestore db;

    /**
     *
     */
    public DbController() {
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
