package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.usv.rqapp.NavigatorFragment;
import com.usv.rqapp.R;
import com.usv.rqapp.controllers.DateHandler;
import com.usv.rqapp.controllers.FirestoreController;
import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.databinding.FragmentNewsFeedBinding;
import com.usv.rqapp.models.firestoredb.NewsFeed;

public class NewsFeedFragment extends Fragment {
    private View feedView;
    private FragmentNewsFeedBinding binding;

    private FirestoreController db;
    private FirestoreRecyclerAdapter adapter;
    private FragmentManager manager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentNewsFeedBinding.inflate(inflater, container, false);
        feedView = binding.getRoot();

        initFirestore();
        loadNewsFromFirestore();
        handleAddNewsFeedEvent();

        return feedView;
    }

    private void handleAddNewsFeedEvent() {
        binding.imgAddNewsFeedPost.setOnClickListener(click -> {
            FragmentOpener.loadNextFragment(NavigatorFragment.newInstance(),manager);
        });
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
        Query query = db.getDb().collection(NewsFeed.POSTARI).orderBy(NewsFeed.MOMENT_POSTARE).limit(NewsFeed.QUERY_LIMIT);

        FirestoreRecyclerOptions<NewsFeed> options = new FirestoreRecyclerOptions.Builder<NewsFeed>()
                .setQuery(query, NewsFeed.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<NewsFeed, NewsFeedViewHoldeer>(options) {
            @NonNull
            @Override
            public NewsFeedViewHoldeer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_feed_item, parent, false);
                return new NewsFeedViewHoldeer(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NewsFeedViewHoldeer holder, int position, @NonNull NewsFeed model) {

                holder.newsTitle.setText(String.valueOf(model.getTitlu_eveniment()));
                holder.newsDescription.setText(model.getDescriere());
                holder.newsUser.setText(model.getUtilizator());
                holder.newsTime.setText(DateHandler.getTimeBetweenNowAndThen(model.getMoment_postare()));
                holder.newsVoteCount.setText(String.valueOf(model.getAprecieri()));
                if (model.getLoc_eveniment() != null) {
                    holder.newsEventLocation.setText(String.valueOf(model.getLoc_eveniment().getLatitude() + " " + model.getLoc_eveniment().getLongitude()));
                }


            }
        };

        binding.rvNewsFeedList.setHasFixedSize(true);
        binding.rvNewsFeedList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNewsFeedList.setAdapter(adapter);
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
     *
     */
    private class NewsFeedViewHoldeer extends RecyclerView.ViewHolder {

        private TextView newsTitle;
        private TextView newsDescription;
        private TextView newsUser;
        private TextView newsTime;
        private TextView newsVoteCount;
        private TextView newsEventLocation;


        public NewsFeedViewHoldeer(@NonNull View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.tv_news_feed_title);
            newsDescription = itemView.findViewById(R.id.tv_news_feed_message);
            newsUser = itemView.findViewById(R.id.tv_news_user);
            newsTime = itemView.findViewById(R.id.tv_news_feed_time);
            newsVoteCount = itemView.findViewById(R.id.tv_vote_count);
            newsEventLocation = itemView.findViewById(R.id.tv_event_location);
        }
    }
}
