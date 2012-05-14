package com.chaos.sleepcry.busecretary.utils;

import java.util.Calendar;

import android.content.Context;

import com.chaos.sleepcry.busecretary.R;

public class TimeUtils {
	final String later, before, month, day, hour, minute, second;
	String[] days = new String[5];

	public TimeUtils(Context context) {
		later = context.getString(R.string.later);
		before = context.getString(R.string.before);
		month = context.getString(R.string.month);
		day = context.getString(R.string.day);
		hour = context.getString(R.string.hour);
		minute = context.getString(R.string.minute);
		second = context.getString(R.string.second);
		days[0] = context.getString(R.string.beforeyesterday);
		days[1] = context.getString(R.string.yesterday);
		days[3] = context.getString(R.string.tomorrow);
		days[4] = context.getString(R.string.aftertomorrow);
	}

	/*
	 * generate a string to describe the time
	 * 
	 * @param time to be described
	 */
	public String timeString(long time) {
		Calendar now = Calendar.getInstance();
		Calendar target = Calendar.getInstance();
		target.setTimeInMillis(time);
		if (now.get(Calendar.YEAR) != target.get(Calendar.YEAR)) {
			return generalString(time);
		}
		// month
		int now_month = now.get(Calendar.MONTH);
		int target_month = target.get(Calendar.MONTH);
		if (target_month > now_month) {
			return (target_month - now_month) +" " +  month +" " +  later;
		} else if (target_month < now_month) {
			return (now_month - target_month) +" " +  month +" " +  before;
		}
		// day
		int now_day = now.get(Calendar.DAY_OF_MONTH);
		int target_day = target.get(Calendar.DAY_OF_MONTH);
		if(Math.abs(now_day - target_day) <= 2 && now_day != target_day) {
			return days[target_day-now_day + 2];
		}
		if (now_day > target_day) {
			return (now_day - target_day) + " " + day +" " +  before;
		} else if (target_day > now_day) {
			return (target_day - now_day) +" " +  day +" " +  later;
		}
		// hour
		int now_hour = now.get(Calendar.HOUR_OF_DAY);
		int target_hour = target.get(Calendar.HOUR_OF_DAY);
		if (now_hour > target_hour) {
			return (now_hour - target_hour) +" " +  hour +" " +  before;
		} else if (target_hour > now_hour) {
			return (target_hour - now_hour) +" " +  hour +" " +  later;
		}
		// minutes
		int now_minute = now.get(Calendar.MINUTE);
		int target_minute = target.get(Calendar.MINUTE);
		if (now_minute > target_minute) {
			return (now_minute - target_minute) +" " +  minute +" " +  before;
		} else if (target_minute > now_minute) {
			return (target_minute - now_minute) +" " +  minute +" " +  later;
		}
		// seconds
		int now_second = now.get(Calendar.SECOND);
		int target_second = target.get(Calendar.SECOND);
		if (now_second > target_second) {
			return (now_second - target_second) +" " +  second +" " +  before;
		} else if (target_second > now_second) {
			return (target_second - now_second) +" " +  second +" " +  later;
		}
		return "just now";
	}

	public String generalString(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minite = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		String strDay = year + "-" + (month < 10 ? "0" : "") + month + "-"
				+ (day < 10 ? "0" : "") + day;
		String strTime = (hour < 10 ? "0" : "") + hour + ":"
				+ (minite < 10 ? "0" : "") + minite + ":"
				+ (second < 10 ? "0" : "") + second;
		return strDay + " " + strTime;
	}
}
