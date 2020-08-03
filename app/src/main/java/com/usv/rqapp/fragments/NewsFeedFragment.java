package com.usv.rqapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mapbox.geocoder.GeocoderCriteria;
import com.mapbox.geocoder.MapboxGeocoder;
import com.squareup.picasso.Picasso;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.databinding.FragmentNewsFeedBinding;
import com.usv.rqapp.interfaces.NewsFeedFragmentListener;
import com.usv.rqapp.models.firestoredb.NewsFeed;

import java.util.HashMap;
import java.util.Map;

import static com.usv.rqapp.CONSTANTS.MAPBOX_ACCESS_TOKEN;

public class NewsFeedFragment extends Fragment {

    private String TAG = "NewsFeedFragment";
    private NewsFeedFragmentListener listener;

    private View feedView;
    private FragmentNewsFeedBinding binding;


    private FirestoreController db;
    private FirestoreRecyclerAdapter adapter;
    private FragmentManager manager;
    private MapboxGeocoder client;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NewsFeedFragmentListener) {
            listener = (NewsFeedFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement " + NewsFeedFragmentListener.TAG);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentNewsFeedBinding.inflate(inflater, container, false);
        feedView = binding.getRoot();

        initFirestore();
        loadNewsFromFirestore();


        return feedView;
    }

    private void loadGeoMappig(NewsFeed model) {
        client = new MapboxGeocoder.Builder()
                .setAccessToken(MAPBOX_ACCESS_TOKEN)
                //  .setCoordinates(model.getLoc_eveniment().getLongitude(), model.getLoc_eveniment().getLatitude())
                .setType(GeocoderCriteria.TYPE_PLACE)
                .build();
    }


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

    private void initFirestore() {
        db = new FirestoreController();
    }

    /**
     *
     */
    private void loadNewsFromFirestore() {
        Query query = db.getDb().collection(NewsFeed.POSTARI).orderBy(NewsFeed.MOMENT_POSTARE, Query.Direction.DESCENDING)/*.limit(NewsFeed.QUERY_LIMIT)*/;

        FirestoreRecyclerOptions<NewsFeed> options = new FirestoreRecyclerOptions.Builder<NewsFeed>()
                .setQuery(query, NewsFeed.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<NewsFeed, NewsFeedViewHoldeer>(options) {


            @NonNull
            @Override
            public NewsFeedViewHoldeer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_item, parent, false);
                binding.clNoItemFound.setVisibility(View.GONE);
                return new NewsFeedViewHoldeer(view);
            }

            @Override
            public boolean onFailedToRecycleView(@NonNull NewsFeedViewHoldeer holder) {
                Log.e(TAG, "onFailedToRecycleView: empty");
                binding.clNoItemFound.setVisibility(View.VISIBLE);
                return super.onFailedToRecycleView(holder);
            }

            @Override
            public void onError(@NonNull FirebaseFirestoreException e) {
                binding.clNoItemFound.setVisibility(View.VISIBLE);
                super.onError(e);
            }

            @Override
            public int getItemCount() {
                if (super.getItemCount() < 1) {
                    binding.clNoItemFound.setVisibility(View.VISIBLE);
                }
                return super.getItemCount();
            }

            @Override
            protected void onBindViewHolder(@NonNull NewsFeedViewHoldeer holder, int position, @NonNull NewsFeed model) {

                if (DateHandler.getTimeBetween(model.getMoment_postare()) >= DateHandler.EXPIRATION_TIME) {
                    db.deleteEventOnNewsFeed(model.getId_postare(), model.getImg_url());
                } else {
                    holder.newsTime.setText(DateHandler.getTimeBetweenNowAndThen(model.getMoment_postare()));
                    holder.newsTitle.setText(String.valueOf(model.getTitlu_eveniment()));
                    holder.newsDescription.setText(model.getDescriere());
                    holder.newsUser.setText(model.getUtilizator());
                    holder.newsVoteCount.setText(String.valueOf(model.getAprecieri()));
                    holder.newsEventLocation.setText(model.getLoc_eveniment());
                    if (model.getImg_url() != null) {
                        Picasso.get().load(Uri.parse(model.getImg_url())).into(holder.newsFeedEventImage);
                    }

                    initialValueOfPressed(holder, model);
                    handleUpVoteOnNewsFeed(holder, model);
                    handleDownVoteOnNewsFeed(holder, model);
                    handleShareContentButton(holder.newsFeedShare, model);
                    handleGetEventLocation(holder.newsEventLocation, model.getTitlu_eveniment(), model.getCoords());
                }


            }
        };

        binding.rvNewsFeedList.setHasFixedSize(true);
        binding.rvNewsFeedList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNewsFeedList.setAdapter(adapter);

    }

    private void handleGetEventLocation(TextView newsEventLocation, String titlu_eveniment, GeoPoint coords) {
        newsEventLocation.setOnClickListener(click -> {
            listener.onLocationPressed(titlu_eveniment, coords);
        });
    }

    private void handleShareContentButton(ImageView newsFeedShare, NewsFeed model) {
        newsFeedShare.setOnClickListener(click -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");

            String shareSubject = model.getTitlu_eveniment();
            String shareBody = "Titlul: " + shareSubject + "\t\nDescriere: " + model.getDescriere() + "\t\nLoc eveniment: " + model.getLoc_eveniment() + "\t\nLocație: lat[" + model.getCoords().getLatitude() + "], lng[" + model.getCoords().getLongitude() + "]";


            sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(sendIntent, "Share using"));

        });
    }


    /**
     * @param holder
     * @param model
     * @Description Checks for initial value on for upVote or downVote
     */
    private void initialValueOfPressed(NewsFeedViewHoldeer holder, NewsFeed model) {
        db.getDb().collection(NewsFeed.APRECIERI).document(model.getId_postare()).collection(NewsFeed.APRECIERI_UTILIZATOR).document(db.getFirebaseUser().getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Am gasit documetul
                DocumentSnapshot doc = task.getResult();
                if (doc.getLong(NewsFeed.APRECIATION_VAL) != null) {
                    switch (doc.getLong(NewsFeed.APRECIATION_VAL).intValue()) {
                        case 1:
                            holder.newsFeedUpVote.setImageResource(R.drawable.arrow_up);
                            holder.newsFeedDownVote.setImageResource(R.color.transparent);
                            break;
                        case -1:
                            holder.newsFeedDownVote.setImageResource(R.drawable.arrow_up);
                            holder.newsFeedUpVote.setImageResource(R.color.transparent);
                            break;
                        default:
                            holder.newsFeedUpVote.setImageResource(R.color.transparent);
                            holder.newsFeedDownVote.setImageResource(R.color.transparent);
                            break;
                    }

                }

            } else if (task == null) {
                //Nu am gasit documentul
                Toast.makeText(getContext(), "Offline", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Nu am gasit documentul");
            } else {
                // Avem o oxceptie
                Log.e(TAG, task.getException().getMessage());
            }
        });
    }

    private void handleDownVoteOnNewsFeed(NewsFeedViewHoldeer holder, NewsFeed model) {
        holder.newsFeedDownVote.setOnClickListener(click -> {
            db.getDb().collection(NewsFeed.APRECIERI).document(model.getId_postare()).collection(NewsFeed.APRECIERI_UTILIZATOR).document(db.getFirebaseUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Am gasit documetul
                    DocumentSnapshot doc = task.getResult();


                    db.getDb().collection(NewsFeed.POSTARI).document(model.getId_postare()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Integer currentNrLikes;
                            currentNrLikes = Math.toIntExact(task1.getResult().getLong(NewsFeed.APRECIERI));

                            if (doc.get(NewsFeed.APRECIATION_VAL) == null || doc.getLong(NewsFeed.APRECIATION_VAL) == 0 || doc.getLong(NewsFeed.APRECIATION_VAL) == 1) {
                                Map<String, Object> map = new HashMap<>();
                                map.put(NewsFeed.APRECIATION_VAL, -1);


                                Map<String, Object> mapTotalLikes = new HashMap<>();
                                mapTotalLikes.put(NewsFeed.APRECIERI, currentNrLikes.intValue() - 1);


                                db.getDb().collection(NewsFeed.APRECIERI).document(model.getId_postare()).collection(NewsFeed.APRECIERI_UTILIZATOR).document(db.getFirebaseUser().getUid()).set(map).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        db.getDb().collection(NewsFeed.POSTARI).document(model.getId_postare()).update(mapTotalLikes).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {
                                                Log.e(TAG, "Actualizare totala like");
                                                holder.newsFeedDownVote.setImageResource(R.drawable.arrow_up);
                                                holder.newsFeedUpVote.setImageResource(R.color.transparent);
                                                holder.newsVoteCount.setText(mapTotalLikes.get(NewsFeed.APRECIERI).toString());
                                            } else {
                                                Log.e(TAG, task3.getException().getMessage());
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, task2.getException().getMessage());
                                    }
                                });
                            }
                        }
                    });


                } else if (task == null) {
                    //Nu am gasit documentul
                    Toast.makeText(getContext(), "Conectează-te la internet", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Nu am gasit documentul");
                } else {
                    // Avem o oxceptie
                    Log.e(TAG, task.getException().getMessage());
                }
            });
        });


    }

    private void handleUpVoteOnNewsFeed(NewsFeedViewHoldeer holdeer, NewsFeed model) {
        holdeer.newsFeedUpVote.setOnClickListener(click -> {
            db.getDb().collection(NewsFeed.APRECIERI).document(model.getId_postare()).collection(NewsFeed.APRECIERI_UTILIZATOR).document(db.getFirebaseUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Am gasit documetul
                    DocumentSnapshot doc = task.getResult();


                    db.getDb().collection(NewsFeed.POSTARI).document(model.getId_postare()).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Integer currentNrLikes;
                            currentNrLikes = Math.toIntExact(task1.getResult().getLong(NewsFeed.APRECIERI));


                            if (doc.get(NewsFeed.APRECIATION_VAL) == null || doc.getLong(NewsFeed.APRECIATION_VAL) == 0 || doc.getLong(NewsFeed.APRECIATION_VAL) == -1) {
                                Map<String, Object> mapForUser = new HashMap<>();
                                mapForUser.put(NewsFeed.APRECIATION_VAL, 1);


                                Map<String, Object> mapTotalLikes = new HashMap<>();
                                mapTotalLikes.put(NewsFeed.APRECIERI, currentNrLikes.intValue() + 1);


                                db.getDb().collection(NewsFeed.APRECIERI).document(model.getId_postare()).collection(NewsFeed.APRECIERI_UTILIZATOR).document(db.getFirebaseUser().getUid()).set(mapForUser).addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        db.getDb().collection(NewsFeed.POSTARI).document(model.getId_postare()).update(mapTotalLikes).addOnCompleteListener(task3 -> {
                                            if (task3.isSuccessful()) {
                                                Log.e(TAG, "Actualizare totala like");
                                                holdeer.newsFeedUpVote.setImageResource(R.drawable.arrow_up);
                                                holdeer.newsFeedDownVote.setImageResource(R.color.transparent);
                                                holdeer.newsVoteCount.setText(mapTotalLikes.get(NewsFeed.APRECIERI).toString());
                                            } else {
                                                Log.e(TAG, task3.getException().getMessage());
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, task2.getException().getMessage());
                                    }
                                });

                            }


                        }
                    });


                } else if (task == null) {
                    //Nu am gasit documentul
                    Toast.makeText(getContext(), "Conectează-te la internet", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Nu am gasit documentul");
                } else {
                    // Avem o oxceptie
                    Log.e(TAG, task.getException().getMessage());
                }
            });
        });
    }

    /**
     * @return
     */
    public static NewsFeedFragment newInstance() {
        NewsFeedFragment fragment = new NewsFeedFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }


    /**
     * Firestore View Holder For App
     */
    private class NewsFeedViewHoldeer extends RecyclerView.ViewHolder {

        private TextView newsTitle;
        private TextView newsDescription;
        private TextView newsUser;
        private TextView newsTime;
        private TextView newsVoteCount;
        private TextView newsEventLocation;
        private ImageView newsFeedUpVote;
        private ImageView newsFeedDownVote;
        private ImageView newsFeedShare;
        private RoundedImageView newsFeedEventImage;


        public NewsFeedViewHoldeer(@NonNull View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.tv_news_feed_title);
            newsDescription = itemView.findViewById(R.id.tv_news_feed_message);
            newsUser = itemView.findViewById(R.id.tv_news_user);
            newsTime = itemView.findViewById(R.id.tv_news_feed_time);
            newsVoteCount = itemView.findViewById(R.id.tv_vote_count);
            newsEventLocation = itemView.findViewById(R.id.tv_event_location);
            newsFeedUpVote = itemView.findViewById(R.id.img_up_vote);
            newsFeedDownVote = itemView.findViewById(R.id.img_down_vote);
            newsFeedEventImage = itemView.findViewById(R.id.img_feed_image);
            newsFeedShare = itemView.findViewById(R.id.img_share);
        }
    }

}
