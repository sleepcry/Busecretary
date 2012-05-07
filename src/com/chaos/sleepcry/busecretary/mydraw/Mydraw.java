package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;

public abstract class Mydraw implements Comparable<Mydraw> {
	// used to compare
	int mLayer;
	View mParent = null;
	boolean bVisiable = true;
	public Mydraw(int layer,View parent){
		mLayer = layer;
		mParent = parent;
	}
	public abstract void draw(Canvas canvas);
	public void setLayer(int layer){
		mLayer = layer;
	}
	public int getLayer(){
		return mLayer;
	}
	@Override
	public int compareTo(Mydraw another) {
		if (mLayer > another.mLayer) {
			return 1;
		} else if (mLayer < another.mLayer) {
			return -1;
		} else {
			return 0;
		}
	}
	public void setView(View parent){
		mParent = parent;
	}
	public boolean isVisible(){
		return bVisiable;
	}
	public void setVisible(boolean val){
		bVisiable = val;
	}
	public PointF getAbsFromRel(PointF ptf) {
		Rect bound = new Rect(mParent.getLeft(), mParent.getTop(),
				mParent.getRight(), mParent.getBottom());
		PointF ptf2 = new PointF();
		ptf2.x = bound.left + bound.width() * ptf.x;
		ptf2.y = bound.top + bound.height() * ptf.y;
		return ptf2;
	}
}
