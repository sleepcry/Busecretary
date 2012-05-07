package com.chaos.sleepcry.busecretary.colorpalette;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.chaos.sleepcry.busecretary.R;

public class ColorCell extends View{
	int mColor;
	public ColorCell(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.Color);
		mColor = ta.getColor(R.styleable.Color_elemcolor, 0xff000000);
	}
	public ColorCell(Context context,int color) {
		super(context); 
		mColor = color;
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
		paint.setStrokeWidth(2);
		if(this.isPressed()){
			paint.setColor(Color.BLACK);
			canvas.drawLine(left, top, left, bottom, paint);
			canvas.drawLine(left, top, right, top, paint);
			paint.setColor(Color.WHITE);
			canvas.drawLine(right, top,right,bottom,paint);
			canvas.drawLine(left,bottom,right,bottom,paint);
		}else{
			paint.setColor(Color.BLACK);
			canvas.drawLine(right, top,right,bottom,paint);
			canvas.drawLine(left,bottom,right,bottom,paint);
			paint.setColor(Color.WHITE);
			canvas.drawLine(left, top, left, bottom, paint);
			canvas.drawLine(left, top, right, top, paint);
		}		
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setColor(mColor);
		Rect rect = new Rect(left+2,top+2,right-2,bottom-2);
		canvas.drawRect(rect, paint);
		canvas.restore();
	}
	public int getColor() {
		return mColor;		
	}
	public void setColor(int color){
		mColor = color;
	}
}
