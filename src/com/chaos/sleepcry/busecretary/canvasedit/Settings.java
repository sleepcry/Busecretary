package com.chaos.sleepcry.busecretary.canvasedit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.AvoidXfermode;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.mydraw.MyPolyLine;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;

public class Settings extends Activity {
	public static final int BLUR = 0x1;
	public static final int EMBOSS = 0x2;
	public static final int ADD = 0x3;
	public static final int ERASE = 0x8;
	public static final int SRC = 0x10;
	public static final int SRCATOP = 0x20;
	public static final int SRCIN = 0x40;
	public static final int SRCOUT = 0x80;
	public static final int SRCOVER = 0x100;
	public static final int DST = 0x200;
	public static final int DSTATOP = 0x400;
	public static final int DSTIN = 0x800;
	public static final int DSTOUT = 0x1000;
	public static final int DSTOVER = 0x2000;
	public static final int SCREEN = 0x4000;
	public static final int DARKEN = 0x8000;
	public static final int LIGHTEN = 0x10000;
	public static final int MULTIPLY = 0x20000;
	public static final int XOR = 0x40000;
	public static final int AVOID = 0x80000;
	public static final int TARGET = 0x100000;

	public static String EXTRAS = "data";
	public static String COLOR = "color";
	public static int DEFCOLOR = 0x7fffffff;
	private int mResult1 = SRC;
	private int mResult2 = 0;
	PaintBoard mPb;
	MyPolyLine mLine;
	MyPolyLine[] mVerticalLines;
	EditText mA, mR, mG, mB;
	int mColor;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.settings);
		mPb = (PaintBoard) findViewById(R.id.surfaceView1);
		mPb.setEditable(false);
		mA = (EditText) findViewById(R.id.a);
		mR = (EditText) findViewById(R.id.r);
		mG = (EditText) findViewById(R.id.g);
		mB = (EditText) findViewById(R.id.b);
		mColor = getIntent().getIntExtra(COLOR, DEFCOLOR);
		mA.setText(String.valueOf((mColor>>24)&0xff));
		mR.setText(String.valueOf((mColor>>16)&0xff));
		mG.setText(String.valueOf((mColor>>8)&0xff));
		mB.setText(String.valueOf(mColor&0xff));
		int result = getIntent().getIntExtra(EXTRAS, SRC);
		initSettings(result);
		
	}
	private void initSettings(int flags) {
		mResult2 = flags & 0x3;
		if((mResult2 &BLUR) == BLUR ) {
			((CheckBox)findViewById(R.id.flur)).setChecked(true);
		}
		if((mResult2 &EMBOSS) == EMBOSS ) {
			((CheckBox)findViewById(R.id.emboss)).setChecked(true);
		}
		mResult1 = flags & 0xfffffffc;
		if ((mResult2 & Settings.ERASE) == Settings.ERASE) {
			((RadioButton)findViewById(R.id.clear)).setChecked(true);
		} else if ((mResult1 & Settings.SRC) == Settings.SRC) {
			((RadioButton)findViewById(R.id.src)).setChecked(true);
		} else if ((mResult2 & Settings.SRCATOP) == Settings.SRCATOP) {
			((RadioButton)findViewById(R.id.src_atop)).setChecked(true);
		} else if ((mResult1 & Settings.SRCIN) == Settings.SRCIN) {
			((RadioButton)findViewById(R.id.src_in)).setChecked(true);
		} else if ((mResult1 & Settings.SRCOUT) == Settings.SRCOUT) {
			((RadioButton)findViewById(R.id.src_out)).setChecked(true);
		} else if ((mResult1 & Settings.SRCOVER) == Settings.SRCOVER) {
			((RadioButton)findViewById(R.id.src_over)).setChecked(true);
		} else if ((mResult1 & Settings.DST) == Settings.DST) {
			((RadioButton)findViewById(R.id.dst)).setChecked(true);
		} else if ((mResult1 & Settings.DSTATOP) == Settings.DSTATOP) {
			((RadioButton)findViewById(R.id.dst_atop)).setChecked(true);
		} else if ((mResult1 & Settings.DSTIN) == Settings.DSTIN) {
			((RadioButton)findViewById(R.id.dst_in)).setChecked(true);
		} else if ((mResult1 & Settings.DSTOUT) == Settings.DSTOUT) {
			((RadioButton)findViewById(R.id.dst_out)).setChecked(true);
		} else if ((mResult1 & Settings.DSTOVER) == Settings.DSTOVER) {
			((RadioButton)findViewById(R.id.dst_over)).setChecked(true);
		} else if ((mResult1 & Settings.DARKEN) == Settings.DARKEN) {
			((RadioButton)findViewById(R.id.darken)).setChecked(true);
		} else if ((mResult1 & Settings.LIGHTEN) == Settings.LIGHTEN) {
			((RadioButton)findViewById(R.id.lighten)).setChecked(true);
		} else if ((mResult1 & Settings.SCREEN) == Settings.SCREEN) {
			((RadioButton)findViewById(R.id.screen)).setChecked(true);
		} else if ((mResult1 & Settings.MULTIPLY) == Settings.MULTIPLY) {
			((RadioButton)findViewById(R.id.multiply)).setChecked(true);
		} else if ((mResult1 & Settings.XOR) == Settings.XOR) {
			((RadioButton)findViewById(R.id.xor)).setChecked(true);
		} else if ((mResult1 & Settings.AVOID) == Settings.AVOID) {
			((RadioButton)findViewById(R.id.avoid)).setChecked(true);
		} else if ((mResult1 & Settings.TARGET) == Settings.TARGET) {
			((RadioButton)findViewById(R.id.target)).setChecked(true);
		}
	}
	private void initLines(int color) {
		mVerticalLines = new MyPolyLine[8];
		int lineWidth = this.getWindowManager().getDefaultDisplay().getWidth() / 8;
		float w = 1.0f / 8.0f;
		PointF[] pts = new PointF[2];
		pts[0] = new PointF(w / 2, 0);
		pts[1] = new PointF(w / 2, 1.5f);
		int c = 0x7fff0000;
		mVerticalLines[0] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[0]);
		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7f00ff00;
		mVerticalLines[1] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[1]);

		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7f0000ff;
		mVerticalLines[2] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[2]);

		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7fffff00;
		mVerticalLines[3] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[3]);

		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7f00ffff;
		mVerticalLines[4] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[4]);

		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7fff00ff;
		mVerticalLines[5] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[5]);
		
		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7fffffff;
		mVerticalLines[6] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[6]);
		
		pts[0].offset(w, 0);
		pts[1].offset(w, 0);
		c = 0x7f000000;
		mVerticalLines[7] = new MyPolyLine(pts, c, 0, lineWidth, mPb);
		mPb.add(mVerticalLines[7]);

		pts[0].set(0, 0.5f);
		pts[1].set(1.5f, 0.5f);
		mLine = new MyPolyLine(pts, color, 1, lineWidth, mPb);
		mPb.add(mLine);
	}

	public void onOK(View v) {
		mColor = retrieveColor();
		Intent i = new Intent();
		i.putExtra(EXTRAS, mResult1 | mResult2);
		i.putExtra(COLOR, mColor);
		setResult(RESULT_OK, i);
		this.finish();
	}
	
	public void onReset(View v) {
		mResult1 = SRC;
		mResult2 = 0;
		((RadioButton) findViewById(R.id.src)).setChecked(true);
		((CheckBox)findViewById(R.id.flur)).setChecked(false);
		((CheckBox)findViewById(R.id.emboss)).setChecked(false);
	}

	public void changeXferMode(View v) {
		CheckBox cBox;
		switch (v.getId()) {
		case R.id.flur:
			cBox = (CheckBox) v;
			if (cBox.isChecked()) {
				mResult2 |= BLUR;
			} else {
				mResult2 &= ~BLUR;
			}
			break;
		case R.id.emboss:
			cBox = (CheckBox) v;
			if (cBox.isChecked()) {
				mResult2 |= EMBOSS;
			} else {
				mResult2 &= ~EMBOSS;
			}
			break;
		case R.id.clear:
			mResult1 = ERASE;
			break;
		case R.id.darken:
			mResult1 = DARKEN;
			break;
		case R.id.dst:
			mResult1 = DST;
			break;
		case R.id.dst_atop:
			mResult1 = DSTATOP;
			break;
		case R.id.dst_in:
			mResult1 = DSTIN;
			break;
		case R.id.dst_out:
			mResult1 = DSTOUT;
			break;
		case R.id.dst_over:
			mResult1 = DSTOVER;
			break;
		case R.id.lighten:
			mResult1 = LIGHTEN;
			break;
		case R.id.multiply:
			mResult1 = MULTIPLY;
			break;
		case R.id.screen:
			mResult1 = SCREEN;
			break;
		case R.id.src:
			mResult1 = SRC;
			break;
		case R.id.src_atop:
			mResult1 = SRCATOP;
			break;
		case R.id.src_in:
			mResult1 = SRCIN;
			break;
		case R.id.src_out:
			mResult1 = SRCOUT;
			break;
		case R.id.src_over:
			mResult1 = SRCOVER;
			break;
		case R.id.xor:
			mResult1 = XOR;
			break;
		case R.id.avoid:
			mResult1 = AVOID;
			break;
		case R.id.target:
			mResult1 = TARGET;
			break;
		}
		
		mPb.clear();
		mColor = retrieveColor();
		initLines(mColor);
		mLine.setPaint(mResult1 | mResult2, mColor, 10);
		mPb.invalidateAll();
	}
	private int retrieveColor() {
		int color = mColor;
		try {
			int a = Integer.valueOf(mA.getText().toString());
			int r = Integer.valueOf(mR.getText().toString());
			int g = Integer.valueOf(mG.getText().toString());
			int b = Integer.valueOf(mB.getText().toString());
			color = Color.argb(Math.min(a, 255), Math.min(r, 255),
					Math.min(g, 255), Math.min(b, 255));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return color;
	}
}
