package com.vizdashcam.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.vizdashcam.AdapterPermissionPager;
import com.vizdashcam.InteractiveExpandingCircleView;
import com.vizdashcam.R;
import com.vizdashcam.utils.PermissionUtils;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PermissionsActivity";

    private InteractiveExpandingCircleView mExpandingCircleView;
    private ViewPager mViewPager;
    private Button mBtnNextFinish;

    private AdapterPermissionPager mAdapter;

    private BroadcastReceiver mPermissionGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupNextButton();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);

        findViews();
        initViews();

        LocalBroadcastManager.getInstance(this).registerReceiver(mPermissionGrantedReceiver,
                new IntentFilter(PermissionUtils.PERMISSION_GRANTED_BROADCAST));
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupNextButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mPermissionGrantedReceiver);
    }

    private void handleNextBtnVisibility() {
        int currentItem = mViewPager.getCurrentItem();
        int totalItems = mAdapter.necessaryPermissions.size();
        mBtnNextFinish.setVisibility(currentItem == totalItems - 1 &&
                PermissionUtils.computeNecessaryPermissions(PermissionsActivity.this).size() > 0 ?
                View.INVISIBLE :
                View.VISIBLE);
    }

    private void findViews() {
        mExpandingCircleView = (InteractiveExpandingCircleView) findViewById(R.id.expandingCircleView);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mBtnNextFinish = (Button) findViewById(R.id.btnNextFinish);
    }

    private void initViews() {
        mBtnNextFinish.setOnClickListener(this);

        mAdapter = new AdapterPermissionPager(PermissionsActivity.this, getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mExpandingCircleView.setFraction(positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                setupNextButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mExpandingCircleView.maxRadiusDIP = 150;
    }

    private void setupNextButton() {
        if (PermissionUtils.computeNecessaryPermissions(PermissionsActivity.this).size() == 0) {
            mBtnNextFinish.setText(R.string.done);
        } else {
            mBtnNextFinish.setText(R.string.next);
        }
        handleNextBtnVisibility();
    }

    //region View.OnClickListener±
    @Override
    public void onClick(View view) {
        if (view == mBtnNextFinish) {
            if (PermissionUtils.computeNecessaryPermissions(PermissionsActivity.this).size() == 0) {
                finish();
            } else {
                int currentItem = mViewPager.getCurrentItem();
                int totalItems = mAdapter.necessaryPermissions.size();
                if (currentItem < totalItems - 1) { mViewPager.setCurrentItem(currentItem + 1, true); }
            }
        }
    }
    //endregion
}
