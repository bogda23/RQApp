package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.usv.rqapp.databinding.FragmentChangePasswordBinding;

public class ChangePasswordFragment extends Fragment {

    private View changePasswordView;
    private FragmentChangePasswordBinding binding;

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        changePasswordView = binding.getRoot();

        backPressHandler();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Metodă care ne duce înapoi la fragmentul anterior
     */
    private void backPressHandler() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();

            }
        });
    }


}
