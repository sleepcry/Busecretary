package com.chaos.sleepcry.busecretary.colorpalette;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.chaos.sleepcry.busecretary.R;

public class ColorCell extends View {
	int mColor;

	public ColorCell(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context
				.obtainStyledAttributes(attrs, R.styleable.Color);
		mColor = ta.getColor(R.styleable.Color_elemcolor, 0xff000000);
	}

	public ColorCell(Context context, int color) {
		super(context);
		mColor = color;
	}

	private boolean mBSelected = false;
	private int mPhase = 0;
	public void select(boolean bSel) {
		mBSelected = bSel;
		mPhase = 0;
	}
	public void onDraw(Canvas canvas){
		//super.onDraw(canvas);
		canvas.save();
		int left = getLeft();
		int right = getRight();
		int top = getTop();
		int bottom = getBottom();
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);
		final int edget = 4;
		paint.setStrokeWidth(edget);
		if(mBSelected){
			PathEffect effect = new  DashPathEffect(new float[] { 3, 2,
					2, 2 }, mPhase);
			paint.setPathEffect(effect);
			mPhase ++;
			if(mPhase >= 10000) {
				mPhase = 0;
			}
		}
		paint.setColor(Color.WHITE);
		canvas.drawRect(new Rect(left,top,right,bottom), paint);		
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(mColor);
		paint.setPathEffect(null);
		Rect rect = new Rect(left+edget,top+edget,right-edget,bottom-edget);
		canvas.drawRect(rect, paint);
		if(mBSelected){
			postInvalidateDelayed(100);
		}
		canvas.restore();
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}
}
