package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class MyPolyLine extends Mydraw {
	PointF[] mPts;
	int mColor;

	public MyPolyLine() {
		super(0);
	}

	public MyPolyLine(PointF[] pts, int c, int layer) {
		super(layer);
		mPts = pts;
		mColor = c;
	}

	public void update(PointF[] pts) {
		mPts = pts;
	}
	public String toString(){
		return "MyPolyLine";
	}
	public void draw(Canvas canvas) {
		if (mPts == null) {
			return;
		}
		Rect bound = new Rect(mParent.getLeft(), mParent.getTop(),
				mParent.getRight(), mParent.getBottom());
		Paint paint = new Paint();
		paint.setColor(mColor);
		paint.setStrokeWidth(2.0f);
		for (int i = 0; i < mPts.length - 1; i++) {
			canvas.drawLine(bound.left + bound.width() * mPts[i].x, bound.top
					+ bound.height() * mPts[i].y, bound.left + bound.width()
					* mPts[i + 1].x,
					bound.top + bound.height() * mPts[i + 1].y, paint);
		}
	}

}
