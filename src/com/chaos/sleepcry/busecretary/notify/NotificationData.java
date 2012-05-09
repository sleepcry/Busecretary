package com.chaos.sleepcry.busecretary.notify;

import java.util.Calendar;

import android.graphics.Bitmap;

import com.chaos.sleepcry.busecretary.Day;
import com.chaos.sleepcry.busecretary.RepeatCategory;

public class NotificationData implements Comparable<NotificationData> {
	private int mId;
	private Day mDay;
	private String mDesc;
	private String mRing;
	private RepeatCategory mCategory;
	private int mLocation = 0; // the current record location in the total
	String mBmpPath;
	Bitmap mBitMap;

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
		mBmpPath = null;
	}

	/*
	 * @{ getter & setter
	 */
	public void setLocation(int mLocation) {
		this.mLocation = mLocation;
	}

	public int getLocation() {
		return mLocation;
	}

	public void setRepeatCategory(RepeatCategory mCategory) {
		this.mCategory = mCategory;
	}

	public RepeatCategory getRepeatCategory() {
		return mCategory;
	}

	public void setRing(String mRing) {
		this.mRing = mRing;
	}

	public String getRing() {
		return mRing;
	}

	public void setWhat(String mDesc) {
		this.mDesc = mDesc;
	}

	public String getWhat() {
		return mDesc;
	}

	public void setWhen(Day mDay) {
		this.mDay = mDay;
	}

	public Day getWhen() {
		return mDay;
	}

	public void setId(int mId) {
		this.mId = mId;
	}
	private String mWhereString = null;
	public void setWhere(String where) {
		mWhereString = where;
	}
	public String getWhere() {
		return mWhereString;
	}

	public int getId() {
		return mId;
	}

	public String getBmpPath() {
		return mBmpPath;
	}

	public void setBmpPath(String bmp) {
		this.mBmpPath = bmp;
	}

	public Bitmap getBmp() {
		return mBitMap;
	}

	public void setBmp(Bitmap bmp) {
		if (mBitMap != null) {
			mBitMap.recycle();
		}
		mBitMap = bmp;
	}

	@Override
	public int compareTo(NotificationData another) {
		return (int) (another.mDay.getCalendar().getTimeInMillis() - mDay
				.getCalendar().getTimeInMillis());
	}
}