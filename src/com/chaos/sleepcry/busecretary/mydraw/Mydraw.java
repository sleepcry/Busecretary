package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public abstract class Mydraw implements Comparable<Mydraw> {
	// used to compare
	int mLayer;
	View mParent = null;
	public Mydraw(int layer){
		mLayer = layer;
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
}
