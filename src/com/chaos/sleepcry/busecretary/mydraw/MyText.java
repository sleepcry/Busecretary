package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;

public class MyText extends Mydraw {
	String mText;
	PointF mDrawPos;
	int mColor;
	Path mPath;
	Paint mPathPaint;
	int mTextSize = 0;

	public MyText(String text, PointF pt, int c, int layer,int textsize, View parent) {
		super(layer, parent);
		mText = text;
		mDrawPos = pt;
		mColor = c;
		mPathPaint = new Paint();
		mPathPaint.setAntiAlias(true);
		mPathPaint.setDither(true);
		mPathPaint.setTextAlign(Paint.Align.CENTER);
//		mPathPaint.setFakeBoldText(true);
		mTextSize = textsize;
		
		mPath = new Path();
	}

	public String toString() {
		return mText;
	}

	public void setPath(PointF[] pts) {
		if(pts.length <= 2 || mParent == null) {
			return;
		}
		mPath.reset();
		PointF ptf0 = getAbsFromRel(pts[0]);
		mPath.moveTo(ptf0.x, ptf0.y);
		PointF ptf_h = getAbsFromRel(pts[pts.length/2]);
		PointF ptf_e = getAbsFromRel(pts[pts.length-1]);
		mPath.cubicTo(ptf0.x, ptf0.y, ptf_h.x, ptf_h.y,ptf_e.x,ptf_e.y);
	}

	@Override
	public void draw(Canvas canvas) {
		mPathPaint.setTextSize(mTextSize);
		mPathPaint.setColor(mColor);
		if (mPath != null) {
//			canvas.drawPath(mPath, mPathPaint);
			canvas.drawTextOnPath(mText, mPath, 0, 0, mPathPaint);
		} else {
			PointF ptf = getAbsFromRel(mDrawPos);
			canvas.drawText(mText, ptf.x,ptf.y, mPathPaint);
		}
	}

}
