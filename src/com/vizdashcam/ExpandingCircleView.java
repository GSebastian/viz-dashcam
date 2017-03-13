package com.vizdashcam;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.vizdashcam.utils.CameraUtils;

public class ExpandingCircleView extends View {

    private AnimatorSet animatorSet;
    private Paint circlePaint;

    float x, y;
    private int currentSizePX;
    private int startSizePX = 0;
    private int endSizePX;

    private int currentAlpha;
    private int startAlpha = 255;
    private int endAlpha = 0;

    private long duration = 1000;

    public ExpandingCircleView(Context context) {
        super(context);
        initialise();
    }

    public ExpandingCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public ExpandingCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExpandingCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialise();
    }

    private void initialise() {
        circlePaint = new Paint();
        circlePaint.setARGB(startAlpha, 255, 255, 255);

        endSizePX = CameraUtils.getDisplayWidth(getContext()) * 2;

        ValueAnimator sizeAnimator = new ValueAnimator();
        sizeAnimator.setObjectValues(startSizePX, endSizePX);
        sizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentSizePX = (int) animation.getAnimatedValue();
            }
        });

        ValueAnimator transparencyAnimator = new ValueAnimator();
        transparencyAnimator.setObjectValues(startAlpha, endAlpha);
        transparencyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAlpha = (int) animation.getAnimatedValue();
            }
        });

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(sizeAnimator, transparencyAnimator);
        animatorSet.setDuration(duration);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (animatorSet.isRunning()) {
            invalidate();
            circlePaint.setAlpha(currentAlpha);
            canvas.drawCircle(x, y, currentSizePX, circlePaint);
        }
    }

    public void startAnimation(float X, float Y) {
        this.x = X;
        this.y = Y;
        currentSizePX = startSizePX;
        currentAlpha = startAlpha;

        animatorSet.start();

        invalidate();
    }
}
