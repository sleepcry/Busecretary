package com.chaos.sleepcry.busecretary;

import java.util.Calendar;
import java.util.Date;

public class Day {
	private Calendar mCalendar = null;
	private DayDisplay mDisp;

	public Day(Date date) {
		this.mCalendar = Calendar.getInstance();
		mCalendar.setTime(date);
		Calendar today = Calendar.getInstance();
		today.setTime(new Date());
		if (mCalendar.get(Calendar.DAY_OF_MONTH) == today
				.get(Calendar.DAY_OF_MONTH)
				&& mCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& mCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
			mDisp = DayDisplay.TODAY;
		} else if (mCalendar.get(Calendar.DAY_OF_MONTH) == today
				.get(Calendar.DAY_OF_MONTH) + 1
				&& mCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& mCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
			mDisp = DayDisplay.TOMORROW;
		}
	}

	public Day(Calendar cal) {
		this.mCalendar = cal;
		Calendar today = Calendar.getInstance();
		today.setTime(new Date());
		if (mCalendar.get(Calendar.DAY_OF_MONTH) == today
				.get(Calendar.DAY_OF_MONTH)
				&& mCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& mCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
			mDisp = DayDisplay.TODAY;
		} else if (mCalendar.get(Calendar.DAY_OF_MONTH) == today
				.get(Calendar.DAY_OF_MONTH) + 1
				&& mCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& mCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
			mDisp = DayDisplay.TOMORROW;
		}
	}

	/*
	 * @{ generate string from date
	 */
	public String getString() {
		int month = mCalendar.get(Calendar.MONTH) + 1;
		return mCalendar.get(Calendar.YEAR) + "-" + month + "-"
				+ mCalendar.get(Calendar.DAY_OF_MONTH) + "  "+
				+ mCalendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ mCalendar.get(Calendar.MINUTE) + ":"
				+ mCalendar.get(Calendar.SECOND);
	}

	/*
	 * @{ setter & getter
	 */
	public Calendar getCalendar() {
		return mCalendar;
	}

	public void setCalendar(Calendar mCalendar) {
		this.mCalendar = mCalendar;
	}

	public DayDisplay getDisp() {
		return mDisp;
	}

	public void setDisp(DayDisplay mDisp) {
		this.mDisp = mDisp;
	}
}
