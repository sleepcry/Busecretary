package com.chaos.sleepcry.busecretary.colorpalette;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.chaos.sleepcry.busecretary.R;


public class ColorPickerDialog extends Dialog {
    private int CENTER_X = 200;
    private int CENTER_Y = 200;
    
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }
    
    private OnColorChangedListener mListener;
    private int mInitialColor;

    private class ColorPickerView extends View {
        private Paint mPaint;
        private Paint mCenterPaint;
        private final int[] mColors;
        private OnColorChangedListener mListener;
        
        ColorPickerView(Context c, OnColorChangedListener l, int color) {
            super(c);
            mListener = l;
            mColors = new int[] {
                0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
                0xFFFFFF00, 0xFFFF0000
            };
            Shader s1 = new SweepGradient(0, 0, mColors, null);
            Shader s2 = new RadialGradient(0, 0, CENTER_X, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
            Shader s = new ComposeShader(s1, s2, PorterDuff.Mode.SCREEN );
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.FILL);
            
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setStrokeWidth(5);
        }

        @Override 
        protected void onDraw(Canvas canvas) {           
            canvas.translate(CENTER_X, CENTER_Y);            
            canvas.drawOval(new RectF(-CENTER_X, -CENTER_Y, CENTER_X, CENTER_Y), mPaint);            
        }
        
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X*2, CENTER_Y*2);
        }
        
        
        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }
        
        private int interpColor(int colors[], float unit,double dst) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }
            
            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            
            double r1 = 1-dst;
            double g1 = 1-dst;
            double b1 = 1-dst;
            double r2 = r/255.0;
            double b2 = b/255.0;
            double g2 = g/255.0;
            double r3 = r1+r2-r1*r2;
            double b3 = b1+b2-b1*b2;
            double g3 = g1+g2-g1*g2;
            r = (int)(r3*255);
            g = (int)(g3*255);
            b = (int)(b3*255);
            return Color.argb(a, r, g, b);
        }
               
        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				float x = event.getX() - CENTER_X;
				float y = event.getY() - CENTER_Y;
				float angle = (float) java.lang.Math.atan2(y, x);
				double dst = Math.sqrt(x*x + y*y)/Math.sqrt(CENTER_X*CENTER_X+CENTER_Y*CENTER_Y);
				// need to turn angle [-PI ... PI] into unit [0....1]
				float unit = angle / (2 * PI);
				if (unit < 0) {
					unit += 1;
				}
				mListener.colorChanged(interpColor(mColors, unit,dst));
			}
            return true;
        }
    }

    public ColorPickerDialog(Context context,
                             OnColorChangedListener listener,
                             int initialColor,
                             int radius) {
        super(context);
        
        mListener = listener;
        mInitialColor = initialColor;
        CENTER_X = CENTER_Y = radius;    
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mInitialColor));
        setTitle(getContext().getString(R.string.pickcolor));
    }

	
}