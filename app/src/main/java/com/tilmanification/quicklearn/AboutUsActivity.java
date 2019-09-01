package com.tilmanification.quicklearn;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutUsActivity extends Fragment {

    // ========================================================================
    // Constant Fields
    // ========================================================================

    private static final String	TAG	= AboutUsActivity.class.getSimpleName();

    // ========================================================================
    // Class Variables
    // ========================================================================

    private View aboutUsView;

    // ========================================================================
    // Methods
    // ========================================================================


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        aboutUsView = inflater.inflate(R.layout.activity_about_us, container, false);
        return aboutUsView;
    }
}
