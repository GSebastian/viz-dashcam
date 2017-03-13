package com.vizdashcam.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vizdashcam.R;

public abstract class FragmentPermissionBase extends Fragment implements OnClickListener {

    private ImageView mIvPermissionIcon;
    private TextView mTvPermissionDescription;
    private Button mBtnGrantPermission;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_permission, null);

        findViews(rootView);
        initViews();

        return rootView;
    }

    private void findViews(View rootView) {
        mIvPermissionIcon = (ImageView) rootView.findViewById(R.id.ivPermissionIcon);
        mTvPermissionDescription = (TextView) rootView.findViewById(R.id.tvPermissionDescription);
        mBtnGrantPermission = (Button) rootView.findViewById(R.id.btnGrantPermission);
    }


    private void initViews() {
        mIvPermissionIcon.setImageResource(getImageResource());

        mTvPermissionDescription.setText(getTextResource());

        mBtnGrantPermission.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnGrantPermission) {

        }
    }

    public abstract int getImageResource();

    public abstract int getTextResource();
}
