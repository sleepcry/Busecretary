package com.chaos.sleepcry.busecretary.colorpalette;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.chaos.sleepcry.busecretary.R;

public class ColorPalette extends LinearLayout implements OnClickListener {
	int mColor;
	OnColorChangedListener mColorChangedListener;
	public ColorPalette(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.colorpalette, this);
		init();
		mColor = Color.WHITE;
	}

	private void init() {
		mColorChangedListener = null;
		findViewById(R.id.cell1).setOnClickListener(this);	
		findViewById(R.id.cell2).setOnClickListener(this);	
		findViewById(R.id.cell3).setOnClickListener(this);	
		findViewById(R.id.cell4).setOnClickListener(this);	
		findViewById(R.id.cell5).setOnClickListener(this);	
		findViewById(R.id.cell6).setOnClickListener(this);	
		findViewById(R.id.cell7).setOnClickListener(this);	
		findViewById(R.id.cell8).setOnClickListener(this);	
		findViewById(R.id.cell9).setOnClickListener(this);	
		findViewById(R.id.cell10).setOnClickListener(this);	
		findViewById(R.id.cell11).setOnClickListener(this);	
		findViewById(R.id.cell12).setOnClickListener(this);	
		findViewById(R.id.cell13).setOnClickListener(this);	
		findViewById(R.id.cell14).setOnClickListener(this);	
		findViewById(R.id.cell15).setOnClickListener(this);	
		findViewById(R.id.cell16).setOnClickListener(this);		
	}

	public ColorPalette(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.colorpalette, this);
		init();
		mColor = Color.WHITE;
	}

	public int getColor() {
		return mColor;
	}

	@Override
	public void onClick(View v) {
		if (v instanceof ColorCell) {
			mColor = ((ColorCell) v).getColor();
			if(mColorChangedListener != null){
				mColorChangedListener.onColorChange(mColor);
			}
		}

	}
	public void setOnColorChangedListener(OnColorChangedListener listener){
		mColorChangedListener = listener;
	}
	public static interface OnColorChangedListener{
		public void onColorChange(int color);
	}

}
