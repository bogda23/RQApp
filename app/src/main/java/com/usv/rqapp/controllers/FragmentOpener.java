package com.usv.rqapp.controllers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.usv.rqapp.CustomAnimation;
import com.usv.rqapp.R;
import com.usv.rqapp.fragments.PermissionsFragment;

public class FragmentOpener {

    public static void loadNextFragment(Fragment newFragment, FragmentManager fragmentManager) {
        FragmentManager manager = fragmentManager;
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, newFragment).commit();
    }

    public static void loadNextFragmentWithStack(Fragment newFragment, FragmentManager fragmentManager) {
        FragmentManager manager = fragmentManager;
        manager.beginTransaction().setCustomAnimations(CustomAnimation.animation[0], CustomAnimation.animation[1],
                CustomAnimation.animation[2], CustomAnimation.animation[3]).replace(R.id.fragment_frame, newFragment).addToBackStack(null).commit();
    }


}
