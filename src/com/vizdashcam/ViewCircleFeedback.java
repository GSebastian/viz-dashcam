package com.vizdashcam;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class ViewCircleFeedback extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holder;
	private AnimThread animThread;

	public ViewCircleFeedback(Context context) {
		super(context);

		setBackgroundColor(Color.TRANSPARENT);
		setZOrderOnTop(true);

		holder = getHolder();
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		if (animThread != null) {
			while (retry) {
				try {
					animThread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void animate(Pair<Integer, Integer> lastFeedbackCoords) {
		animThread = new AnimThread(holder, lastFeedbackCoords);
		animThread.start();
	}

	class AnimThread extends Thread {

		private SurfaceHolder holder;
		private int radius = 0;
		private Pair<Integer, Integer> lastFeedbackCoords;

		public AnimThread(SurfaceHolder holder,
				Pair<Integer, Integer> lastFeedbackCoords) {
			this.holder = holder;
			this.lastFeedbackCoords = lastFeedbackCoords;
		}

		@Override
		public void run() {
			while (radius < 350) {
				Canvas canvas = null;
				try {
					canvas = holder.lockCanvas();
					synchronized (holder) {
						canvas.drawColor(Color.TRANSPARENT, Mode.MULTIPLY);
						Paint paint = new Paint();
						paint.setColor(Color.WHITE);
						paint.setAlpha(150);
						canvas.drawCircle(lastFeedbackCoords.first,
								lastFeedbackCoords.second, radius, paint);
					}
				} finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}

				}
				radius += 25;
			}
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(Color.TRANSPARENT, Mode.MULTIPLY);
			holder.unlockCanvasAndPost(canvas);
		}
	}

}