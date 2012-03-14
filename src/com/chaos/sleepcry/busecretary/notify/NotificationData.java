package com.chaos.sleepcry.busecretary.notify;

import java.util.Calendar;

import android.graphics.Bitmap;

import com.chaos.sleepcry.busecretary.Day;
import com.chaos.sleepcry.busecretary.RepeatCategory;

public class NotificationData {
	private int mId;
	private Day mDay;
	private String mDesc;
	private String mRing;
	private RepeatCategory mCategory;
	private int mLocation = 0; // the current record location in the total
	Bitmap mBmp;

	public NotificationData() {
		this(0, -1);
	}

	public NotificationData(int id, int loc) {
		setId(id);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		mDay = new Day(cal);
		mDesc = new String("");
		mRing = null;
		mCategory = RepeatCategory.NONE;
		mLocation = loc;
		mBmp = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
	}

	/*@{
	 * getter & setter
	 */
	public void setLocation(int mLocation) {
		this.mLocation = mLocation;
	}

	public int getLocation() {
		return mLocation;
	}

	public void setCategory(RepeatCategory mCategory) {
		this.mCategory = mCategory;
	}

	public RepeatCategory getCategory() {
		return mCategory;
	}

	public void setRing(String mRing) {
		this.mRing = mRing;
	}

	public String getRing() {
		return mRing;
	}

	public void setDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	public String getDesc() {
		return mDesc;
	}

	public void setDay(Day mDay) {
		this.mDay = mDay;
	}

	public Day getDay() {
		return mDay;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public int getId() {
		return mId;
	}
	public Bitmap getBmp(){
		return mBmp;
	}
	public void setBmp(Bitmap bmp){
		this.mBmp = bmp;
	}
}