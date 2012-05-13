package com.chaos.sleepcry.busecretary.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.R;

public class HintView extends TextView{

	public HintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		setBackgroundResource(R.drawable.input);
	}
	Paint mPaint;
	String mText;
	public void onDraw(Canvas canvas) {
		
	}

}
