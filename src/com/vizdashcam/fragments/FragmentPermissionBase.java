package com.vizdashcam.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vizdashcam.R;
import com.vizdashcam.utils.ViewUtils;
import com.vizdashcam.utils.VizPermissionUtils;

import java.util.ArrayList;

import rebus.permissionutils.FullCallback;
import rebus.permissionutils.PermissionEnum;
import rebus.permissionutils.PermissionManager;

public abstract class FragmentPermissionBase extends Fragment implements OnClickListener {

    private static final String TAG = "FragmentPermissionBase";

    private ImageView mIvPermissionIcon;
    private TextView mTvPermissionDescription;
    private Button mBtnGrantPermission;

    private int topMarginStart = (int) ViewUtils.dp2px(-120);
    private int topMarginEnd = 0;

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
        mBtnGrantPermission.setTextColor(ContextCompat.getColor(getContext(), R.color.C3LightGray));

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

        mBtnGrantPermission.setTextColor(ContextCompat.getColor(getContext(), R.color.C3Red));
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
        Intent intent = new Intent(VizPermissionUtils.PERMISSION_GRANTED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    //region Scroll fraction-based animations
    public void setAnimationFraction(float fraction) {
        setIconMarginAnimationFraction(fraction);
        setIconAlphaAnimationFraction(fraction);
    }

    private void setIconMarginAnimationFraction(float fraction) {
        if (mIvPermissionIcon != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mIvPermissionIcon
                    .getLayoutParams();


            layoutParams.topMargin = topMarginStart + (int) ((float) (topMarginEnd - topMarginStart) * fraction);

            mIvPermissionIcon.setLayoutParams(layoutParams);
            mIvPermissionIcon.requestLayout();
        }
    }

    private void setIconAlphaAnimationFraction(float fraction) {
        if (mIvPermissionIcon != null) {
            mIvPermissionIcon.setAlpha(fraction);
        }
    }
    //endregion

    //region View.OnClickListener
    @Override
    public void onClick(View view) {
        if (view == mBtnGrantPermission) {
            grantPermission();
        }
    }
    //endregion
}
