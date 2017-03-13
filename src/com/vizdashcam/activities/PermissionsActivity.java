package com.vizdashcam.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.vizdashcam.AdapterPermissionPager;
import com.vizdashcam.InteractiveExpandingCircleView;
import com.vizdashcam.R;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PermissionsActivity";

    private InteractiveExpandingCircleView mExpandingCircleView;
    private ViewPager mViewPager;
    private Button mBtnNextFinish;

    private AdapterPermissionPager mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);

        findViews();
        initViews();
    }

    private void findViews() {
        mExpandingCircleView = (InteractiveExpandingCircleView) findViewById(R.id.expandingCircleView);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mBtnNextFinish = (Button) findViewById(R.id.btnNextFinish);
    }

    private void initViews() {
        mBtnNextFinish.setOnClickListener(this);

        mAdapter = new AdapterPermissionPager(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mExpandingCircleView.setFraction(positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    //region View.OnClickListener
    @Override
    public void onClick(View view) {
        if (view == mBtnNextFinish) {

        }
    }
    //endregion
}
