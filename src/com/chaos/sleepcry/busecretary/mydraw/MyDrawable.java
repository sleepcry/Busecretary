package com.chaos.sleepcry.busecretary.mydraw;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class MyDrawable extends Mydraw implements Parcelable {
	BitmapDrawable mDrawable;
	RectF mDrawPos;
	String mUrl = null;
	static Context msCtxt = null;

	public MyDrawable(BitmapDrawable d, RectF rectf, int layer) {
		super(layer);
		mDrawable = d;
		mDrawPos = rectf;
	}

	public MyDrawable(String url, RectF rectf, int layer) {
		super(layer);
		mUrl = url;
		FileInputStream input = null;
		try {
			input = msCtxt.openFileInput(url);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		mDrawable = new BitmapDrawable(BitmapFactory.decodeStream(input));
		mDrawPos = rectf;
	}
	
	public String toString(){
		if(mUrl==null||mUrl.length() == 0) {
			return "image";
		}else {
			return mUrl;
		}
	}
	public Bitmap getBmp() {
		return mDrawable!=null?mDrawable.getBitmap():null;
	}
	
	/*
	 * this should be invoked before you take this class as a parcelable
	 */
	static public void setContext(Context ctxt) {
		msCtxt = ctxt;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect rect = new Rect();
		Rect bound = new Rect(mParent.getLeft(), mParent.getTop(),
				mParent.getRight(), mParent.getBottom());
		rect.left = (int) (bound.left + bound.width() * mDrawPos.left);
		rect.right = (int) (bound.left + bound.width() * mDrawPos.right);
		rect.top = (int) (bound.top + bound.height() * mDrawPos.top);
		rect.bottom = (int) (bound.top + bound.height() * mDrawPos.bottom);
		mDrawable.setBounds(rect);
		mDrawable.draw(canvas);

	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (mDrawable instanceof BitmapDrawable) {
			dest.writeString(mUrl);
			dest.writeInt(mLayer);
			dest.writeFloat(mDrawPos.left);
			dest.writeFloat(mDrawPos.top);
			dest.writeFloat(mDrawPos.right);
			dest.writeFloat(mDrawPos.bottom);
		}

	}

	public static final Parcelable.Creator<MyDrawable> CREATOR = new Parcelable.Creator<MyDrawable>() {
		public MyDrawable createFromParcel(Parcel in) {
			String url = in.readString();
			int layer = in.readInt();
			RectF rectf = new RectF(in.readFloat(), in.readFloat(),
					in.readFloat(), in.readFloat());
			return new MyDrawable(url, rectf, layer);
		}

		public MyDrawable[] newArray(int size) {
			return new MyDrawable[size];
		}

	};

}
