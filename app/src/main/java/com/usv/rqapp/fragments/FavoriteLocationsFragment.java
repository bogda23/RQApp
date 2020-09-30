package com.usv.rqapp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.usv.rqapp.CONSTANTS;
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
     * Preluarea locațiilor favorite pentru fiecare utilizator
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
                binding.clNoItemFound.setVisibility(View.GONE);
                return new FavoriteLocationsViewHoldeer(view);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                binding.clNoItemFound.setVisibility(View.VISIBLE);
                super.onError(e);
            }

            @Override
            public boolean onFailedToRecycleView(@NonNull FavoriteLocationsViewHoldeer holder) {
                Log.e(TAG, "onFailedToRecycleView: ERUUAREEE");
                binding.clNoItemFound.setVisibility(View.VISIBLE);
                return super.onFailedToRecycleView(holder);
            }

            @Override
            public int getItemCount() {
                if (super.getItemCount() < 1) {
                    binding.clNoItemFound.setVisibility(View.VISIBLE);
                }
                return super.getItemCount();
            }

            @Override
            protected void onBindViewHolder(@NonNull FavoriteLocationsViewHoldeer holder, int position, @NonNull FavoriteLocation model) {

                if (model != null) {
                    binding.clNoItemFound.setVisibility(View.GONE);
                }
                holder.favoriteLocationTitle.setText(model.getTitlul_locatiei());
                handleDeleteButton(holder.deleteFavLocation, model.getId_locatie());
                handleNavigateButton(holder.navigateButton, model.getTitlul_locatiei(), model.getCoords());

            }


        };

        binding.rvFavoriteLocations.setHasFixedSize(true);
        binding.rvFavoriteLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFavoriteLocations.setAdapter(adapter);
    }


    /**
     * Începe navigarea spre o locație din lista locatiilor favorite
     *
     * @param navigateButton
     * @param titlul_locatiei
     * @param coords
     */
    private void handleNavigateButton(Button navigateButton, String titlul_locatiei, GeoPoint coords) {
        navigateButton.setOnClickListener(click -> {
            FragmentOpener.loadNextFragment(NavigatorFragment.newInstanceWithGeoPoint(titlul_locatiei, coords), manager);
        });
    }


    /**
     * Sterge o locație favorită
     *
     * @param deleteButton
     * @param id_locatie
     */
    private void handleDeleteButton(ImageView deleteButton, String id_locatie) {
        deleteButton.setOnClickListener(click -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle(CONSTANTS.DELETE_ACCOUNT);
            dialog.setMessage(CONSTANTS.DELETE_FAVORITE_ELEMENT);
            dialog.setPositiveButton(getString(R.string.sterge_elementul), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.deleteFavoriteLocationFromFirestore(id_locatie);
                }
            });
            dialog.setNegativeButton(getString(R.string.anuleaza), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        });
    }


    /**
     * Inițializează controlerul pentru baza de date
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
     * Posesorul informațiilor despre locațiile favorite
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
