package com.chaos.sleepcry;

import java.util.Calendar;
import java.util.Date;

public class Day {
	private Calendar mCalendar = null;
	private DayDisplay mDisp;
	public Day(Date date,DayDisplay disp){
		this.mCalendar = Calendar.getInstance();
		mCalendar.setTime(date);
		this.mDisp = disp;
	}
	public Day(Calendar cal,DayDisplay disp){
		this.mCalendar = cal;
		this.mDisp = disp;
	}

	/*@{
	 * generate string from date
	 */
	public String getDateString(){
		return mCalendar.get(Calendar.YEAR) + "-" + 
				mCalendar.get(Calendar.MONTH) + "-" + 
				mCalendar.get(Calendar.DAY_OF_MONTH);
	}
	public String getTimeString(){
		return mCalendar.get(Calendar.HOUR_OF_DAY) + 
				":" + mCalendar.get(Calendar.MINUTE) + ":" + 
				mCalendar.get(Calendar.SECOND);
	}

	/*@{
	 * setter & getter
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
