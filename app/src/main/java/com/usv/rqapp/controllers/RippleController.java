package com.usv.rqapp.controllers;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.usv.rqapp.R;

import javax.annotation.Nullable;

public class RippleController {
    private static ValueAnimator vAnimator;
    private final static int DEFAULT_REPEAT_TIMES = 3;


    public static void OverLay(final GroundOverlay groundOverlay, boolean startAnimation, @Nullable Integer REPEAT_TIMES) {
        if (groundOverlay != null) {
            vAnimator = ValueAnimator.ofInt(0, 2000);
            if (REPEAT_TIMES == null) {
                vAnimator.setRepeatCount(DEFAULT_REPEAT_TIMES);
            } else {
                vAnimator.setRepeatCount(REPEAT_TIMES);
            }
            //vAnimator.setIntValues(0, 500);
            vAnimator.setDuration(3000);
            vAnimator.setEvaluator(new IntEvaluator());
            vAnimator.setInterpolator(new LinearInterpolator());
            vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    Integer i = (Integer) valueAnimator.getAnimatedValue();
                    groundOverlay.setDimensions(i);
                    groundOverlay.setTransparency(animatedFraction);
                }
            });
            if (startAnimation) {
                vAnimator.start();
            } else {
                vAnimator.end();
            }

        }

    }
}
