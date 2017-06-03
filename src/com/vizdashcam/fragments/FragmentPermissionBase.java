package com.vizdashcam.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vizdashcam.R;
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

    private AnimatorSet mIconAnimations;
    private ValueAnimator mTopMarginAnimator;
    private ObjectAnimator mAlphaAnimator;

    private int topMarginStart = -120;
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
        initAnimator();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // load data here
        } else {
            resetAnimation();
        }
    }

    private void initAnimator() {
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvPermissionIcon.getLayoutParams();

        mTopMarginAnimator = ValueAnimator.ofInt(topMarginStart, topMarginEnd);
        mTopMarginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                mIvPermissionIcon.requestLayout();
            }
        });

        mAlphaAnimator = ObjectAnimator.ofFloat(mIvPermissionIcon, View.ALPHA, 0, 1);

        mIconAnimations = new AnimatorSet();
        mIconAnimations.playTogether(mAlphaAnimator, mTopMarginAnimator);
        mIconAnimations.setInterpolator(new AccelerateDecelerateInterpolator());
        mIconAnimations.setDuration(700);

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

    public void animateEntry() {

        mIconAnimations.start();
    }

    public void resetAnimation() {

        if (mIvPermissionIcon != null) {
            mIvPermissionIcon.setAlpha(0f);
        }

        if (mIvPermissionIcon != null) {
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvPermissionIcon
                    .getLayoutParams();
            params.topMargin = topMarginStart;
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
        Intent intent = new Intent(VizPermissionUtils.PERMISSION_GRANTED_BROADCAST);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    //region View.OnClickListener
    @Override
    public void onClick(View view) {
        if (view == mBtnGrantPermission) {
            grantPermission();
        }
    }
    //endregion
}
