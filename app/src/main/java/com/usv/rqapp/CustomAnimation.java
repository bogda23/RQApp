package com.usv.rqapp;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CustomAnimation {

    private static Animation genericAnimation;

    public static int[] animation = {R.anim.enter_right_to_left, R.anim.exit_right_to_left,
            R.anim.enter_left_to_right, R.anim.exit_left_to_right};
    public static int[] animationExit = {R.anim.enter_left_to_right, R.anim.exit_left_to_right};
    public static int[] animationFadeExit = {R.anim.fade_in, R.anim.fade_out};

    public static void applyAnimationTo(View view, int animationResource) {
        genericAnimation = AnimationUtils.loadAnimation(view.getContext(), animationResource);
        view.startAnimation(genericAnimation);

    }

}

