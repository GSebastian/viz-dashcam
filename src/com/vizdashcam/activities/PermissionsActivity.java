package com.vizdashcam.activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.vizdashcam.AdapterPermissionPager;
import com.vizdashcam.BuildConfig;
import com.vizdashcam.InteractiveExpandingCircleView;
import com.vizdashcam.R;
import com.vizdashcam.utils.PermissionUtils;

import java.util.List;

import rebus.permissionutils.PermissionEnum;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PermissionsActivity";

    private InteractiveExpandingCircleView mExpandingCircleView;
    private ViewPager mViewPager;
    private Button mBtnNextFinish;

    private AdapterPermissionPager mAdapter;

    private List<String> mNecessaryPermissions;

    private BroadcastReceiver mPermissionGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupNextButton();

            Integer missingPermissionIndex = hasAllNecessaryPermissions();
            if (missingPermissionIndex != null) {
                mViewPager.setCurrentItem(missingPermissionIndex);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);

        mNecessaryPermissions = PermissionUtils.computeNecessaryPermissions(PermissionsActivity.this);

        // Upon rotation when all permissions were granted but "done" was not pressed, mNecessaryPermissions
        // will become of size 0. In that case, just dismiss
        if (mNecessaryPermissions.size() == 0) {
            finish();
        } else {

            findViews();
            initViews();

            LocalBroadcastManager.getInstance(this).registerReceiver(mPermissionGrantedReceiver,
                    new IntentFilter(PermissionUtils.PERMISSION_GRANTED_BROADCAST));
        }
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
        int totalItems = mNecessaryPermissions.size();

        mBtnNextFinish.setVisibility(
                !hasPermission(mNecessaryPermissions.get(currentItem)) ||
                        (currentItem == totalItems - 1 &&
                                hasAllNecessaryPermissions() != null) ?
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

        mAdapter = new AdapterPermissionPager(mNecessaryPermissions, getSupportFragmentManager());
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

        // Remove clipping for pages
        mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                page.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });

        mExpandingCircleView.maxRadiusDIP = 150;
        if (!BuildConfig.BUILD_TYPE.equals("emuTesting")) {
             mExpandingCircleView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void setupNextButton() {
        if (hasAllNecessaryPermissions() == null) {
            mBtnNextFinish.setText(R.string.done);
        } else {
            mBtnNextFinish.setText(R.string.next);
        }
        handleNextBtnVisibility();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Integer hasAllNecessaryPermissions() {
        for (int i = 0; i < mNecessaryPermissions.size(); i++) {
            String permission = mNecessaryPermissions.get(i);
            if (permission.equals(PermissionUtils.PERMISSION_OVERLAY)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    continue;
                }
                if (!Settings.canDrawOverlays(this)) {
                    return i;
                }
            } else {
                if (!rebus.permissionutils.PermissionUtils.isGranted(this,
                        PermissionEnum.fromManifestPermission(permission))) {
                    return i;
                }
            }
        }

        return null;
    }

    //region View.OnClickListener±
    @Override
    public void onClick(View view) {
        if (view == mBtnNextFinish) {
            Integer missingPermissionIndex = hasAllNecessaryPermissions();
            if (missingPermissionIndex == null) {
                finish();
            } else {
                mViewPager.setCurrentItem(missingPermissionIndex);
            }
        }
    }
    //endregion

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String permission) {
        if (permission.equals(PermissionUtils.PERMISSION_OVERLAY)) {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.M | Settings.canDrawOverlays(getApplicationContext());
        } else {
            return rebus.permissionutils.PermissionUtils.isGranted(
                    this,
                    PermissionEnum.fromManifestPermission(permission));
        }
    }
}
