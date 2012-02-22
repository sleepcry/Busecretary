package com.chaos.sleepcry;

public enum DayDisplay {
	TODAY("today"),TOMORROW("tomorrow"),OTHER("");
	private String mDay;
	private DayDisplay(String day){
		this.mDay = day;
	}
	public String getDay() {
		return mDay;
	}
	public void setDay(String mDay) {
		this.mDay = mDay;
	}
}
