package com.chaos.sleepcry.busecretary.colorpalette;

import android.graphics.Color;

public class ColorItem implements Comparable<ColorItem>{
	long lastUseTime = 0;
	int color = Color.WHITE;
	public ColorItem(long timeInMillis, int color) {
		this.lastUseTime = timeInMillis;
		this.color = color;
	}
	@Override
	public int compareTo(ColorItem another) {
		return (int) (another.lastUseTime-lastUseTime);
	}
	public int getColor(){
		return color;
	}
	public void setColor(int color){
		this.color = color;
	}

}
