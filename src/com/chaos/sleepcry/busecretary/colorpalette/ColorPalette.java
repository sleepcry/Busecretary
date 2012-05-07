package com.chaos.sleepcry.busecretary.colorpalette;

import java.util.ArrayList;

import android.R.integer;
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
	}
	private ArrayList<ColorCell> mColorCells = new ArrayList<ColorCell>();
	private void init() {
		mColorChangedListener = null;
		mColorCells.add((ColorCell)findViewById(R.id.cell1));
		mColorCells.add((ColorCell)findViewById(R.id.cell2));
		mColorCells.add((ColorCell)findViewById(R.id.cell3));
		mColorCells.add((ColorCell)findViewById(R.id.cell4));
		mColorCells.add((ColorCell)findViewById(R.id.cell5));
		mColorCells.add((ColorCell)findViewById(R.id.cell6));
		mColorCells.add((ColorCell)findViewById(R.id.cell7));
		mColorCells.add((ColorCell)findViewById(R.id.cell8));
		mColorCells.add((ColorCell)findViewById(R.id.cell9));
		mColorCells.add((ColorCell)findViewById(R.id.cell10));
		mColorCells.add((ColorCell)findViewById(R.id.cell11));
		mColorCells.add((ColorCell)findViewById(R.id.cell12));
		mColorCells.add((ColorCell)findViewById(R.id.cell13));
		mColorCells.add((ColorCell)findViewById(R.id.cell14));
		mColorCells.add((ColorCell)findViewById(R.id.cell15));
		mColorCells.add((ColorCell)findViewById(R.id.cell16));
		for (ColorCell ccCell : mColorCells) {
			ccCell.setOnClickListener(this);
		}
	}

	public ColorPalette(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.colorpalette, this);
		init();
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
	public static interface ColorProvider{
		public int getCount();
		public int getColor(int index);
	}
	public static final int MAX_COLOR = 16;
	public void loadColor(ColorProvider provider){
		for(int i=0;i<provider.getCount();i++){
			mColorCells.get(i).setColor(provider.getColor(i));
		}
		invalidate();
	}

}
