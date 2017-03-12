package com.vizdashcam.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vizdashcam.R;

public class PermissionsExplanationFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvDescription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_permissions_request, container);

//        initViews(rootView);
//
//        return rootView;
    }

    private void initViews(View rootView) {
        tvTitle = (TextView) rootView.findViewById(R.id.tvTitle);
        tvDescription = (TextView) rootView.findViewById(R.id.tvDescription);
    }
}
