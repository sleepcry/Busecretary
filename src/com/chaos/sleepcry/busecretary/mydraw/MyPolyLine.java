package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;

public class MyPolyLine extends Mydraw {
	PointF[] mPts;
	int mColor;
	int mLineWidth;

	public MyPolyLine() {
		super(0);
	}

	public MyPolyLine(PointF[] pts, int c, int layer, int lineWidt) {
		super(layer);
		mPts = pts;
		mColor = c;
		mLineWidth = lineWidt;
	}

	public void update(PointF[] pts) {
		mPts = pts;
	}

	public String toString() {
		return "PolyLine";
	}
	
	public void draw(Canvas canvas) {
		if (mPts == null || mPts.length <= 1) {
			return;
		}
		Rect bound = new Rect(mParent.getLeft(), mParent.getTop(),
				mParent.getRight(), mParent.getBottom());
		Paint paint = new Paint();
		paint.setColor(mColor);
		paint.setStrokeWidth(mLineWidth);
		paint.setStyle(Style.STROKE);
		Path polypath = new Path();		
		for (int i = 0; i < mPts.length - 1; i++) {
			float x1 = bound.left + bound.width() * mPts[i].x;
			float x2 = bound.left + bound.width() * mPts[i+1].x;
			float y1 = bound.top + bound.height()* mPts[i].y;
			float y2 = bound.top + bound.height()* mPts[i+1].y;
			if(i == 0) {
				polypath.moveTo(x1, y1);
			}
			polypath.lineTo(x2, y2);
		}
		canvas.drawPath(polypath, paint);
	}

}
