package com.chaos.sleepcry;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class BusecretaryActivity extends Activity implements OnClickListener{
	/*@{
	 * define the UI elements
	 */
	private Button mBtnDateDesc = null; //describe the date
	private Button mBtnTimeDesc = null; //describe the time on the day
	private Button mBtnRingDesc = null; //describe the notification ring
	private Button mBtnRepeatDesc = null; //describe the repeat pattern
	private EditText mEtDesc = null; //describe the notification content
	/*@}
	 * 
	 */
	
	/*
	 * the date used to define a notification
	 */
	private class NotificationData{
		public Day day;
		public String desc;
		public String ring;
		public RepeatCategory category;
		public NotificationData(){
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			day = new Day(cal,DayDisplay.TODAY);
			desc = new String("call my mom");
			ring = null;
			category = RepeatCategory.NONE;
		}
	}
	private NotificationData mCurNoti = null;	//the notification currently edit on 
	/*@{
	 * fields definition
	 */
	public static final String DESC = "desc";
	public static final String RING = "ring";
	public static final String CATEGORY = "category";
	/*@}
	 * 
	 */
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*@{
         * initialize all view components
         */
        mBtnDateDesc = (Button)this.findViewById(R.id.btn_date_desc);
        mBtnDateDesc.setOnClickListener(this);
        mBtnTimeDesc = (Button)this.findViewById(R.id.btn_time_desc);
        mBtnTimeDesc.setOnClickListener(this);
        mBtnRingDesc = (Button)this.findViewById(R.id.btn_ring_desc);
        mBtnRingDesc.setOnClickListener(this);
        mBtnRepeatDesc = (Button)this.findViewById(R.id.btn_repeat_desc);
        mBtnRepeatDesc.setOnClickListener(this);
        mEtDesc = (EditText)this.findViewById(R.id.et_desc);    
        /*@}
         * 
         */
        mCurNoti = new NotificationData();
        updateUI();
    }
	@Override
	public void onClick(View v) {
		int nID = v.getId();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		switch(nID){
		case R.id.btn_date_desc:
			DatePickerDialog datepicker = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {	
					Calendar cal = mCurNoti.day.getCalendar();
					cal.set(Calendar.YEAR,year);
					cal.set(Calendar.MONTH,monthOfYear);
					cal.set(Calendar.DAY_OF_MONTH,dayOfMonth);
					mCurNoti.day.setCalendar(cal);
					updateUI();
				}
				
			},calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE));
			datepicker.show();
			break;
		case R.id.btn_time_desc:
			TimePickerDialog timepicker = new TimePickerDialog(this,new TimePickerDialog.OnTimeSetListener() {
				
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					Calendar cal = mCurNoti.day.getCalendar();
					cal.set(Calendar.HOUR_OF_DAY,hourOfDay);
					cal.set(Calendar.MINUTE,minute);	
					mCurNoti.day.setCalendar(cal);
					updateUI();
				}
			},calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true);
			timepicker.show();
			break;
		case R.id.btn_ring_desc:
//			Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
//					new String[]{MediaStore.Audio.Media.TITLE},
//					MediaStore.Audio.Media.IS_MUSIC 	+ "=1",null,null);
//			cursor.moveToFirst();
//			List<String> lstRing = new ArrayList<String>();
//			while(!cursor.isNull(0)){
//				lstRing.add(cursor.getString(0));
//				cursor.moveToNext();
//			}
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("audio/*");
			this.startActivityForResult(intent, 0);
			break;
		case R.id.btn_repeat_desc:
			AlertDialog dlg = new AlertDialog.Builder(this).setSingleChoiceItems(
					new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, 
							mCurNoti.category.toList()), 0,new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mCurNoti.category = RepeatCategory.getInstance(which + 1);
									updateUI();
									
								}
							}).setTitle("").create();
			dlg.show();
			break;
			default:
				break;
		}
		
	}
	/*
	 * switch to a new notification data to edit
	 */
	public void switchNotif(NotificationData data){
		/*@{
		 * set the current notification
		 */
		Intent intent = new Intent(BusecretaryActivity.this,NotifyActivity.class);
		intent.putExtra(DESC,mCurNoti.desc);
		intent.putExtra(RING,mCurNoti.ring);
		//intent.putExtra(CATEGORY,mCurNoti.category);
		PendingIntent pIntent = PendingIntent.getActivity(BusecretaryActivity.this, 0, intent, 0);
		AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		long interval = 0;	//the time interval the alarm will be launched
		long triggerTime = mCurNoti.day.getCalendar().getTimeInMillis(); //the time when to start the alarm
		boolean bRepeat = true;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(triggerTime);
		switch(mCurNoti.category){
		case EVERYDAY:
			cal.add(Calendar.DATE, 1);
			interval = cal.getTimeInMillis() - triggerTime;
			break;
		case EVERYHOUR:
			cal.add(Calendar.HOUR, 1);
			interval = cal.getTimeInMillis() - triggerTime;
			break;
		case EVERYMONTH:
			cal.add(Calendar.MONTH, 1);
			interval = cal.getTimeInMillis() - triggerTime;
			break;
		case EVERYYEAR:
			cal.add(Calendar.YEAR, 1);
			interval = cal.getTimeInMillis() - triggerTime;
			break;
		case LAST:
			cal.add(Calendar.MINUTE, 10);
			interval = cal.getTimeInMillis() - triggerTime;
			break;
		case NONE:
			bRepeat = false;
			break;
		}
//		cal.setTimeInMillis(System.currentTimeMillis());
//		cal.add(Calendar.MINUTE, 1);
//		triggerTime = cal.getTimeInMillis();
		int month = cal.get(Calendar.DAY_OF_MONTH) + 1;
		Toast.makeText(this,""+cal.get(Calendar.YEAR)+"-"+
				cal.get(Calendar.MONTH)+ "-" + 
				month + "   " +
				cal.get(Calendar.HOUR_OF_DAY) + ":" + 
				cal.get(Calendar.MINUTE) + ":" + 
				cal.get(Calendar.SECOND) , Toast.LENGTH_LONG).show();
		if(bRepeat){
			am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pIntent);
		}else{
			am.set(AlarmManager.RTC_WAKEUP,triggerTime,pIntent);
			//this.startActivity(intent);
		}
		
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(RESULT_OK != resultCode){
			return;
		}
		switch(requestCode){
		case 0: //retrieve music
			mCurNoti.ring = data.getDataString();
			switchNotif(null);
			break;
		}
	}
	private void updateUI(){
		mBtnDateDesc.setText(mCurNoti.day.getDateString());
		mBtnTimeDesc.setText(mCurNoti.day.getTimeString());
		//mBtnRingDesc
		mBtnRepeatDesc.setText(mCurNoti.category.getDesc());
	}
}