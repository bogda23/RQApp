package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.databinding.FragmentFavoriteLocationBinding;
import com.usv.rqapp.models.firestoredb.FavoriteLocation;

public class FavoriteLocationsFragment extends Fragment {
    private final static String TAG = "FavoriteLocationsFragment";

    private FragmentFavoriteLocationBinding binding;
    private View favLocView;

    private FirestoreController db;
    private FirestoreRecyclerAdapter adapter;
    private FragmentManager manager;


    @Override
    public void onStart() {
        super.onStart();
        manager = getFragmentManager();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteLocationBinding.inflate(inflater, container, false);
        favLocView = binding.getRoot();


        initFirestore();
        loadFavoriteLocationsFromFirestore();


        return favLocView;
    }

    /**
     *
     */
    private void loadFavoriteLocationsFromFirestore() {

        Query query = db.getDb().collection(FavoriteLocation.LOC_FAVORITE).document(db.getFirebaseUser().getUid()).collection(FavoriteLocation.LOCATII).orderBy(FavoriteLocation.MOMENT_POSTARE, Query.Direction.DESCENDING)/*.limit(FavoriteLocation.QUERY_LIMIT)*/;

        FirestoreRecyclerOptions<FavoriteLocation> options = new FirestoreRecyclerOptions.Builder<FavoriteLocation>()
                .setQuery(query, FavoriteLocation.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<FavoriteLocation, FavoriteLocationsViewHoldeer>(options) {

            @NonNull
            @Override
            public FavoriteLocationsViewHoldeer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_fav_item, parent, false);
                return new FavoriteLocationsViewHoldeer(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FavoriteLocationsViewHoldeer holder, int position, @NonNull FavoriteLocation model) {


                holder.favoriteLocationTitle.setText(model.getTitlul_locatiei());

                handleDeleteButton(holder.deleteFavLocation, model.getId_locatie());
                handleNavigateButton(holder.navigateButton, model.getTitlul_locatiei(), model.getCoords());
            }

            @Override
            public boolean onFailedToRecycleView(@NonNull FavoriteLocationsViewHoldeer holder) {
                Log.e(TAG, "onFailedToRecycleView: ERUUAREEE");
                return super.onFailedToRecycleView(holder);
            }
        };

        binding.rvFavoriteLocations.setHasFixedSize(true);
        binding.rvFavoriteLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFavoriteLocations.setAdapter(adapter);
    }

    private void handleNavigateButton(Button navigateButton, String titlul_locatiei, GeoPoint coords) {
        navigateButton.setOnClickListener(click -> {
            FragmentOpener.loadNextFragment(NavigatorFragment.newInstanceWithGeoPoint(titlul_locatiei, coords), manager);
        });
    }

    private void handleDeleteButton(ImageView deleteButton, String id_locatie) {
        deleteButton.setOnClickListener(click -> {
            db.deleteFavoriteLocationFromFirestore(id_locatie);
        });
    }

    /**
     *
     */
    private void initFirestore() {
        db = new FirestoreController();
    }

    /**
     * @return
     */
    public static FavoriteLocationsFragment newInstance() {
        FavoriteLocationsFragment fragment = new FavoriteLocationsFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }

    /**
     * Firestore View Holder For App
     */
    private class FavoriteLocationsViewHoldeer extends RecyclerView.ViewHolder {

        private TextView favoriteLocationTitle;
        private ImageView deleteFavLocation;
        private Button navigateButton;


        public FavoriteLocationsViewHoldeer(@NonNull View itemView) {
            super(itemView);
            favoriteLocationTitle = itemView.findViewById(R.id.tv_fav_location_title);
            deleteFavLocation = itemView.findViewById(R.id.img_delete_fav_item);
            navigateButton = itemView.findViewById(R.id.btn_navigate_to_fav_location);
        }
    }
}
