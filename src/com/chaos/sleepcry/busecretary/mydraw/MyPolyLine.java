package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;

public class MyPolyLine extends Mydraw {
	int mColor;
	int mLineWidth;
	Paint mPaint;
	Path mPath;
	public MyPolyLine() {
		super(0,null);
		genPaint();
	}

	public MyPolyLine(PointF[] pts, int c, int layer, int lineWidt,View parent) {
		super(layer,parent);
		mColor = c;
		mLineWidth = lineWidt;
		mPath = new Path();
		genPaint();
		genPatn(pts);
	}
	private void genPaint() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
	}
	private void genPatn(PointF[] pts) {
		if(pts == null) return;
		mPath.reset();
		Rect bound = new Rect(mParent.getLeft(), mParent.getTop(),
				mParent.getRight(), mParent.getBottom());
		for (int i = 0; i < pts.length - 1; i++) {
			float x1 = bound.left + bound.width() * pts[i].x;
			float x2 = bound.left + bound.width() * pts[i+1].x;
			float y1 = bound.top + bound.height()* pts[i].y;
			float y2 = bound.top + bound.height()* pts[i+1].y;
			if(i == 0) {
				mPath.moveTo(x1, y1);
			}
			mPath.quadTo(x2, y2,(x1+x2)/2,(y1+y2)/2);
		}
	}
	public void update(PointF[] pts) {
		genPatn(pts);
	}

	public String toString() {
		return "PolyLine";
	}
	
	public void draw(Canvas canvas) {
		if (mPath == null) {
			return;
		}
		mPaint.setColor(mColor);
		mPaint.setStrokeWidth(mLineWidth);
		canvas.drawPath(mPath, mPaint);
	}

}
