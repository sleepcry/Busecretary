package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.PointF;
import android.graphics.Rect;

public abstract class Mydraw implements Comparable<Mydraw> {
	public static float DIV = 1f;
	// used to compare
	int mLayer;
	PaintBoard mParent = null;
	boolean bVisiable = true;

	public Mydraw(int layer, PaintBoard parent) {
		mLayer = layer;
		mParent = parent;
	}

	public abstract void draw(Canvas canvas);

	public void setLayer(int layer) {
		if (mLayer != layer) {
			mLayer = layer;
			if (mParent != null) {
				mParent.invalidateAll();
			}
		}
	}

	public int getLayer() {
		return mLayer;
	}

	static protected MaskFilter mBlur = new BlurMaskFilter(16,
			BlurMaskFilter.Blur.NORMAL);
	static protected MaskFilter mEmboss = new EmbossMaskFilter(new float[] { 1,
			1, 1 }, 0.4f, 6, 3.5f);

	@Override
	public int compareTo(Mydraw another) {
		return mLayer - another.mLayer;
	}

	public void setView(PaintBoard parent) {
		mParent = parent;
	}

	public boolean isVisible() {
		return bVisiable;
	}

	public void setVisible(boolean val) {
		if (bVisiable != val) {
			bVisiable = val;
			if (mParent != null) {
				mParent.invalidateAll();
			}
		}
	}
	public Rect getBounds() {
		int l = mParent.getLeft();
		int t = mParent.getTop();
		int r = mParent.getRight();
		int b = mParent.getBottom();
		return new Rect(l, t,(int)(l+(r-l)*DIV), (int)(t+(b-t)*DIV));
	}
	public PointF getAbsFromRel(PointF ptf) {
		Rect bound = getBounds();
		PointF ptf2 = new PointF();
		ptf2.x = bound.left + bound.width() * ptf.x;
		ptf2.y = bound.top + bound.height() * ptf.y;
		return ptf2;
	}
}
