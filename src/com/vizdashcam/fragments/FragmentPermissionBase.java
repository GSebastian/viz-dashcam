package com.vizdashcam.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vizdashcam.R;
import com.vizdashcam.utils.PermissionUtils;

import java.util.ArrayList;

import rebus.permissionutils.FullCallback;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;

public abstract class FragmentPermissionBase extends Fragment implements OnClickListener {

    private static final String TAG = "FragmentPermissionBase";

    private ImageView mIvPermissionIcon;
    private TextView mTvPermissionDescription;
    private Button mBtnGrantPermission;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handleResult(requestCode, permissions, grantResults);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_permission, null);

        findViews(rootView);
        initViews();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (hasPermission()) {
            markPermissionGranted();
        }
    }

    public void markPermissionGranted() {
        mBtnGrantPermission.setEnabled(false);
        mBtnGrantPermission.setText(R.string.permission_granted);
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
            grantPermission();
        }
    }

    public abstract int getImageResource();

    public abstract int getTextResource();

    public boolean hasPermission() {
        return rebus.permissionutils.PermissionUtils.isGranted(this.getContext(),
                PermissionEnum.fromManifestPermission(getPermission()));
    }

    public void grantPermission() {
        PermissionManager.with(this)
                .permission(PermissionEnum.fromManifestPermission(getPermission()))
                .callback(new FullCallback() {
                    @Override
                    public void result(ArrayList<PermissionEnum> permissionsGranted, ArrayList<PermissionEnum>
                            permissionsDenied, ArrayList<PermissionEnum> permissionsDeniedForever,
                                       ArrayList<PermissionEnum> permissionsAsked) {
                        if (permissionsGranted.size() > 0) {
                            markPermissionGranted();
                            sendPermissionGranted();
                        }
                    }
                })
                .ask();
    }

    public abstract String getPermission();

    public void sendPermissionGranted() {
        Intent intent = new Intent(PermissionUtils.PERMISSION_GRANTED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
}
