package com.usv.rqapp.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jpardogo.android.googleprogressbar.library.FoldingCirclesDrawable;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;
import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.databinding.FragmentWelcomeBinding;

public class WelcomeFragment extends Fragment {

    private View welcomeView;
    private FragmentWelcomeBinding binding;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        welcomeView = binding.getRoot();

        loadGoogleProgressBar();
        countDownEvent();

        return welcomeView;
    }

    private void countDownEvent() {
        new CountDownTimer(3000, 100) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                loadApp();
            }
        }.start();
    }

    private void loadApp() {
       // CustomAnimation.applyAnimationTo(binding.clRootWelcome, R.anim.fade_out);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animationFadeExit[0], CustomAnimation.animationFadeExit[1]).replace(R.id.fragment_frame, LoginFragment.newInstance()).commit();

    }

    private void loadGoogleProgressBar() {

        try {
            CustomAnimation.applyAnimationTo(binding.ivLogo, R.anim.top_to_middle);
            CustomAnimation.applyAnimationTo(binding.googleProgress, R.anim.search_fade);
            binding.googleProgress.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(getContext()).colors(getResources().getIntArray(R.array.my_color_array))
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
