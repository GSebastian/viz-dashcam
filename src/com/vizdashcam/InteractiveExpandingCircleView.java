package com.vizdashcam;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class InteractiveExpandingCircleView extends View {

    private Paint circlePaint;

    private int currentRadiusDIP = 150;

    public float originX = 0;
    public float originY = 0;

    int minRadiusDIP = 100;
    int maxRadiusDIP = 300;
    int circleColor = Color.WHITE;

    public InteractiveExpandingCircleView(Context context) {
        super(context);
        initialise();
    }

    public InteractiveExpandingCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public InteractiveExpandingCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InteractiveExpandingCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialise();
    }

    private void initialise() {
        circlePaint = new Paint();
        circlePaint.setColor(circleColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(originX, originY, com.vizdashcam.utils.ViewUtils.dp2px(currentRadiusDIP), circlePaint);
    }

    public void setFraction(float fraction) {
        // 0.5 ... 0
        // 1 ... 1

        if (fraction < 0.5f) {
            currentRadiusDIP = (int)(((float) maxRadiusDIP - (float) minRadiusDIP) * (fraction * 2f) + (float) minRadiusDIP);
        } else {
            currentRadiusDIP = (int)((float) maxRadiusDIP - ((float) maxRadiusDIP - (float) minRadiusDIP) * Math.abs(1f -
            (fraction * 2f)));
        }

        invalidate();
    }
}
