package com.vizdashcam;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

public class SpeedListener implements LocationListener {

    public static final String TAG = "SpeedListener";

    TextView display;
    GlobalState mAppState;

    public SpeedListener(TextView display, GlobalState appState) {
        this.display = display;
        this.mAppState = appState;
    }

    @Override
    public void onLocationChanged(Location location) {
        String rawString = getSpeedString(location.getSpeed())
                + getUnitsString();
        int unitsPos = rawString.indexOf("/") - 2;
        SpannableString ss = new SpannableString(rawString);
        ss.setSpan(new RelativeSizeSpan(0.5f), unitsPos, rawString.length(), 0);

        display.setText(ss);
    }

    @Override
    public void onProviderDisabled(String arg0) {
    }

    @Override
    public void onProviderEnabled(String arg0) {
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    private String getSpeedString(float mps) {
        int kph = (int) (mps * 3600 / 1000);
        int mph = (int) (mps * 3600 / 1609);
        if (mAppState.detectSpeedometersUnitsMeasure() == GlobalState.SPEEDOMETER_KPH) {
            return Integer.toString(kph);
        } else {
            return Integer.toString(mph);
        }
    }

    private String getUnitsString() {
        if (mAppState.detectSpeedometersUnitsMeasure() == GlobalState.SPEEDOMETER_KPH) {
            return "KM/H";
        } else {
            return "MI/H";
        }
    }

    public void initView() {
        display.setVisibility(View.VISIBLE);
        String rawString = "0" + getUnitsString();
        int unitsPos = rawString.indexOf("/") - 2;
        SpannableString ss = new SpannableString(rawString);
        ss.setSpan(new RelativeSizeSpan(0.5f), unitsPos, rawString.length(), 0);

        display.setText(ss);
    }
}
