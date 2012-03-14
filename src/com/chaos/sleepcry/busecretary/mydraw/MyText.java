package com.chaos.sleepcry.busecretary.mydraw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class MyText extends Mydraw {
	String mText;
	Point mDrawPos;
	int mColor;

	public MyText(String text, Point pt, int c, int layer) {
		super(layer);
		mText = text;
		mDrawPos = pt;
		mColor = c;
	}

	public String toString() {
		return mText;
	}
	
	@Override
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(mColor);
		Rect bound = new Rect(mParent.getLeft(), mParent.getTop(),
				mParent.getRight(), mParent.getBottom());
		canvas.drawText(mText, bound.left + bound.width() * mDrawPos.x,
				bound.top + bound.height() * mDrawPos.y, paint);
	}

}
