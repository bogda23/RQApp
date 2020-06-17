package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usv.rqapp.databinding.FragmentNewsFeedBinding;

public class NewsFeedFragment extends Fragment {
    private View feedView;
    private FragmentNewsFeedBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentNewsFeedBinding.inflate(inflater, container, false);
        feedView = binding.getRoot();

        return feedView;
    }

    public static NewsFeedFragment newInstance() {
        NewsFeedFragment fragment = new NewsFeedFragment();
        /**
         // do some initial setup if needed, for example Listener etc
         */
        return fragment;
    }
}
