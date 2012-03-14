package com.chaos.sleepcry.busecretary;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

public class DatePickerView extends LinearLayout{
	Calendar mDate = null;
	DatePicker mdp = null;
	TimePicker mtp = null;
	public DatePickerView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.datepicker, this);
		mDate = Calendar.getInstance();
		mdp = (DatePicker) findViewById(R.id.datePicker1);
		mtp = (TimePicker) findViewById(R.id.timePicker1);
	}

	public long getTime(){
		mDate.set(Calendar.YEAR, mdp.getYear());
		mDate.set(Calendar.MONTH,mdp.getMonth());
		mDate.set(Calendar.DAY_OF_MONTH,mdp.getDayOfMonth());
		mDate.set(Calendar.HOUR_OF_DAY,mtp.getCurrentHour());
		mDate.set(Calendar.MINUTE, mtp.getCurrentMinute());
		return mDate.getTimeInMillis();
	}
}
