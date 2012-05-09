package com.chaos.sleepcry.busecretary.mydraw;

import com.chaos.sleepcry.busecretary.canvasedit.Settings;

import android.graphics.AvoidXfermode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class MyPolyLine extends Mydraw {
	int mColor;
	int mLineWidth;
	Paint mPaint;
	Path mPath;
	public MyPolyLine() {
		super(0,null);
		genPaint();
	}

	public MyPolyLine(PointF[] pts, int c, int layer, int lineWidt,PaintBoard parent) {
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
	public void genPatn(PointF[] pts) {
		if(pts == null || pts.length <= 1) return;
		mPath.reset();
		Rect bound = getBounds();
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
		mPaint.setStrokeWidth(mLineWidth);
		mPaint.setColor(mColor);
		canvas.drawPath(mPath, mPaint);
	}
	public void setPaint(int flags, int color, int tol) {
		if ((flags & Settings.BLUR) == Settings.BLUR) {
			mPaint.setMaskFilter(mBlur);
		}
		if ((flags & Settings.EMBOSS) == Settings.EMBOSS) {
			mPaint.setMaskFilter(mEmboss);
		}
		if ((flags & Settings.ERASE) == Settings.ERASE) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		} else if ((flags & Settings.SRC) == Settings.SRC) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		} else if ((flags & Settings.SRCATOP) == Settings.SRCATOP) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		} else if ((flags & Settings.SRCIN) == Settings.SRCIN) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		} else if ((flags & Settings.SRCOUT) == Settings.SRCOUT) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
		} else if ((flags & Settings.SRCOVER) == Settings.SRCOVER) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
		} else if ((flags & Settings.DST) == Settings.DST) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
		} else if ((flags & Settings.DSTATOP) == Settings.DSTATOP) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
		} else if ((flags & Settings.DSTIN) == Settings.DSTIN) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		} else if ((flags & Settings.DSTOUT) == Settings.DSTOUT) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
		} else if ((flags & Settings.DSTOVER) == Settings.DSTOVER) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
		} else if ((flags & Settings.DARKEN) == Settings.DARKEN) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
		} else if ((flags & Settings.LIGHTEN) == Settings.LIGHTEN) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
		} else if ((flags & Settings.SCREEN) == Settings.SCREEN) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
		} else if ((flags & Settings.MULTIPLY) == Settings.MULTIPLY) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
		} else if ((flags & Settings.XOR) == Settings.XOR) {
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
		} else if ((flags & Settings.AVOID) == Settings.AVOID) {
			mPaint.setXfermode(new AvoidXfermode(color, tol,
					AvoidXfermode.Mode.AVOID));
		} else if ((flags & Settings.TARGET) == Settings.TARGET) {
			mPaint.setXfermode(new AvoidXfermode(color, tol,
					AvoidXfermode.Mode.TARGET));
		}
	}
}
