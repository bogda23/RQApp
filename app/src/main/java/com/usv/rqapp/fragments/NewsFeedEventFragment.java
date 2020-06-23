package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.usv.rqapp.controllers.FragmentOpener;
import com.usv.rqapp.databinding.FragmentAddItemInNewsFeedBinding;

public class NewsFeedEventFragment extends Fragment {


    private FragmentAddItemInNewsFeedBinding binding;
    private View newEventView;
    private FragmentManager manager;

    @Override
    public void onStart() {
        super.onStart();
        manager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentAddItemInNewsFeedBinding.inflate(inflater, container, false);
        newEventView = binding.getRoot();

        handleBackButton();

        return newEventView;
    }

    private void handleBackButton() {
        binding.btnBack.setOnClickListener(click -> {
            FragmentOpener.loadNextFragment(NewsFeedFragment.newInstance(), manager);
        });
    }

    public static NewsFeedEventFragment newInstance() {
        NewsFeedEventFragment fragment = new NewsFeedEventFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
