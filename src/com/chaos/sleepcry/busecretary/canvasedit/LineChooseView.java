package com.chaos.sleepcry.busecretary.canvasedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.chaos.sleepcry.busecretary.R;

public class LineChooseView extends View {
	int mLineWidth;
	int mColor;

	public LineChooseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mLineWidth = context.obtainStyledAttributes(attrs, R.styleable.line)
				.getDimensionPixelSize(R.styleable.line_width, 2);
		mColor = context.obtainStyledAttributes(attrs, R.styleable.Color)
				.getColor(R.styleable.Color_elemcolor, Color.RED);
		mLineWidth = 3;
	}

	public LineChooseView(Context context) {
		super(context);
		mLineWidth = 3;
		mColor = Color.RED;
	}

	public void setColor(int color) {
		mColor = color;
		invalidate();
	}

	public void setLineWidth(int width) {
		mLineWidth = width;
		invalidate();
	}

	public int getLineWidth() {
		return mLineWidth;
	}

	public int getColor() {
		return mColor;
	}

	public void onDraw(Canvas canvas) {
		int top = getTop();
		int height = getBottom() - top;
		Paint paint = new Paint();
		paint.setStrokeWidth(mLineWidth);
		paint.setColor(mColor);
		canvas.drawLine(getLeft(), top + height / 3, getRight(), top + height
				/ 3, paint);
	}
}
