package com.chaos.sleepcry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class BusecretaryActivity extends Activity implements OnClickListener {
	
	private TextView mTitleDesc = null;
	private ProgressBar mPbPregress = null;
	private MainView mCur = null;
	private MainView mNext = null;
	private MainView mPrevious = null;
	/*
	 * the date used to define a notification
	 */
	

	private NotificationData mCurNoti = null; // the notification currently edit
												// on
	private NotifyDatabase mDB = null;
	/*
	 * this list must synchronize with database, must synchronize with the set
	 * alarm
	 */
	private List<NotificationData> mLstNotis = null;
	/*
	 * @{ fields definition
	 */
	public static final String DESC = "desc";
	public static final String RING = "ring";
	public static final String CATEGORY = "category";
	public static final String DAY = "day";
	public static final int DB_VER = 3;
	public static final String NOTI_ID = "noti_id";
	public static final int ROLLBACK = 0;
	public static final int COMMIT_NEXT = 1;
	public static final int COMMIT_PREVIOUS = 2;

	/*
	 * @}
	 */
	//the current point
	private Point mPosCur = null;
	//the point when pressed
	private Point mPosDown = null;	
	//the previous point
	private Point mPosPre = null;
	private int mWidthPixel;
	private int mTotalOffset;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		mCur = new MainView(this);
		mNext = new MainView(this);
		mPrevious = new MainView(this);
		ViewGroup.LayoutParams subLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		this.addContentView(mPrevious,subLayout);
		this.addContentView(mNext, subLayout);
		this.addContentView(mCur, subLayout);
		DisplayMetrics dspMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dspMetrics);
		mWidthPixel = dspMetrics.widthPixels;
		mPrevious.translate(-mWidthPixel);
		mNext.translate(mWidthPixel);
		//mCur.translate(new PaneAnimation(mWidthPixel/2));
		/*
		 * initialize the title
		 */
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title);
		mPbPregress = (ProgressBar) this.findViewById(R.id.pb_progress);
		mPbPregress.setBackgroundColor(0x007fff7f);
		mTitleDesc = (TextView) this.findViewById(R.id.tv_title);
		mDB = new NotifyDatabase(this, DB_VER);
		mLstNotis = mDB.query(System.currentTimeMillis());
		/*
		 * if the list has not been built if gotten nothing from the
		 * database,the first time run this program,for example.
		 */
		if (mLstNotis == null) {
			mLstNotis = new ArrayList<NotificationData>();
		}
		if (mLstNotis.size() == 0) {
			switchNotif(new NotificationData(0, 0));
		}
		// if found something,at least one record
		else {
			Intent intent = getIntent();
			NotificationData data = null;
			if(intent != null && intent.getExtras()!= null && intent.getExtras().getInt(NOTI_ID,-1) != -1){
				int id = intent.getExtras().getInt(NOTI_ID);
				data = getById(id);
			}
			switchNotif(data==null?mLstNotis.get(0):data);
		}
		mPosDown = new Point(-1,-1);
		mPosCur = new Point(-1,-1);
		mPosPre = new Point(-1,-1);
		View v = new View(this, null, 0);
		v.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent motion) {
				int action = motion.getAction();
				int index = motion.getActionIndex();
				switch(action){
				case MotionEvent.ACTION_DOWN:
					mPosDown.set((int)motion.getX(),(int)motion.getY());
					mPosCur.set((int)motion.getX(),(int)motion.getY());
					mPosPre.set((int)motion.getX(),(int)motion.getY());
					mTotalOffset = 0;
					break;
				case MotionEvent.ACTION_UP:
					mPosCur.set((int)motion.getX(),(int)motion.getY());
					int mode = ROLLBACK;
					if(motion.getEventTime() - motion.getDownTime() <= 100 ){
						
						if(mPosDown.x - mPosCur.x >= 10){
							//move to previous
							moveNext();
							mode = COMMIT_NEXT;
						}else if(mPosCur.x - mPosDown.x >= 10){
							//move to next
							movePrevious();
							mode = COMMIT_PREVIOUS;
						}
					}else if(mTotalOffset > 0 && mTotalOffset > mWidthPixel/2){
						//move to next
						movePrevious();
						mode = COMMIT_PREVIOUS;
					}else if(mTotalOffset < 0 && mTotalOffset < -mWidthPixel/2){
						//move to previous
						moveNext();
						mode = COMMIT_NEXT;
					}
					// roll back
					commitTranslate(mode);
					mTotalOffset = 0;
					break;
				case MotionEvent.ACTION_MOVE:
					//don`t move to previous if this is the first
					if (mCurNoti.getLocation() < 1 && motion.getX() - mPosDown.x > 0) {
						break;
					}
					mPosCur.set((int) motion.getX(), (int) motion.getY());
					translate(mPosCur.x - mPosPre.x);
					mTotalOffset += mPosCur.x - mPosPre.x;
					mPosPre.set((int) motion.getX(), (int) motion.getY());
					break;
				case MotionEvent.ACTION_CANCEL:
					mPosDown.set(-1,-1);
					mPosCur.set(-1,-1);
					mTotalOffset = 0;
					break;
				}
				return true;
			}
			
		});
		this.addContentView(v, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
	}

	@Override
	public void onClick(View v) {
		int nID = v.getId();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		switch (nID) {
		case R.id.btn_date_desc:
			DatePickerDialog datepicker = new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {

						@Override
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							Calendar cal = mCurNoti.getDay().getCalendar();
							cal.set(Calendar.YEAR, year);
							cal.set(Calendar.MONTH, monthOfYear);
							cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							mCurNoti.getDay().setCalendar(cal);
							updateUI();
						}

					}, calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
			datepicker.show();
			break;
		case R.id.btn_time_desc:
			TimePickerDialog timepicker = new TimePickerDialog(this,
					new TimePickerDialog.OnTimeSetListener() {

						@Override
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							Calendar cal = mCurNoti.getDay().getCalendar();
							cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
							cal.set(Calendar.MINUTE, minute);
							mCurNoti.getDay().setCalendar(cal);
							updateUI();
						}
					}, calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE), true);
			timepicker.show();
			break;
		case R.id.btn_ring_desc:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("audio/*");
			this.startActivityForResult(intent, 0);
			break;
		case R.id.btn_repeat_desc:
			AlertDialog dlg = new AlertDialog.Builder(this)
					.setSingleChoiceItems(
							new ArrayAdapter<String>(
									this,
									android.R.layout.select_dialog_singlechoice,
									mCurNoti.getCategory().toList()), 0,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mCurNoti.setCategory(RepeatCategory
											.getInstance(which + 1));
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
	 * switch to a new notification data to edit synchronize the list
	 */
	public void switchNotif(NotificationData data) {
		
		// process the current notification
		if (mCurNoti != null && mLstNotis.indexOf(mCurNoti) == -1) {
			if (mCurNoti.getRing() == null) {
				Toast.makeText(this, "the notification ring is empty!",
						Toast.LENGTH_SHORT).show();
			}
			mCurNoti.setDesc(mCur.getDesc());
			if (mCurNoti.getDesc() == null || mCurNoti.getDesc().equals("")) {
				Toast.makeText(this, "the description is empty!",
						Toast.LENGTH_SHORT).show();
			}
			if (mCurNoti.getDay().getCalendar().getTimeInMillis() <= System
					.currentTimeMillis()) {
				Toast.makeText(this, "the time is in the past!",
						Toast.LENGTH_SHORT).show();
			}
			/*
			 * @{ set the current notification
			 */
			Intent intent = new Intent(BusecretaryActivity.this,
					NotifyReceiver.class);
			Bundle bundle = new Bundle();
			bundle.putString(DESC, mCurNoti.getDesc());
			bundle.putString(RING, mCurNoti.getRing());
			intent.putExtras(bundle);
			PendingIntent pIntent = PendingIntent.getBroadcast(this, mCurNoti.getId(), intent, 0);
			//PendingIntent.getActivity(
			//		BusecretaryActivity.this, 0, intent, 0);
			AlarmManager am = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			// the time interval the alarm will be launched
			long interval = 0;
			// the time when to start the alarm
			long triggerTime = mCurNoti.getDay().getCalendar().getTimeInMillis();

			if (triggerTime > System.currentTimeMillis()) {
				boolean bRepeat = true;
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(triggerTime);
				switch (mCurNoti.getCategory()) {
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
				if (bRepeat) {
					am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime,
							interval, pIntent);
				} else {
					am.set(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);
				}
			}
			/*
			 * save the current notification
			 */
			mDB.insert(mCurNoti.getId(), mCurNoti.getDay().getCalendar()
					.getTimeInMillis(), mCurNoti.getDesc(), mCurNoti.getRing(),
					mCurNoti.getCategory().getId());
			// synchronize the list
			mLstNotis.add(mCurNoti.getLocation(), mCurNoti);
		}
		// process the to-go one
		if (mLstNotis.indexOf(data) != -1 && data != null) {
			Intent intent = new Intent(BusecretaryActivity.this,
					NotifyActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(DESC, data.getDesc());
			bundle.putString(RING, data.getRing());
			intent.putExtras(bundle);
			PendingIntent pIntent = PendingIntent.getActivity(
					BusecretaryActivity.this, 0, intent, 0);
			/*
			 * cancel this notification when enter and set it again while exit
			 * with the modification
			 */
			pIntent.cancel();
			data.setLocation(mLstNotis.indexOf(data));
			mLstNotis.remove(data);
		}
		// move to the specified data
		if (data != null) {
			mCurNoti = data;
			mCur.setDesc(mCurNoti.getDesc());
		}

		// update the UI with new data
		NotificationData pre = null;
		NotificationData next = null;
		if(data != null){
			int location = data.getLocation();
			if( location-1 >= 0){
				pre = mLstNotis.get(location-1);
			}
			if(location + 1 < mLstNotis.size()-1 ){
				next = mLstNotis.get(location + 1);
			}else{
				next = new NotificationData(data.getId() + 1, data.getLocation() + 1);
			}
		}
		notifyUI(pre,mCurNoti,next);

	}

	public void onDestroy(){
		super.onDestroy();
		switchNotif(null);
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (RESULT_OK != resultCode) {
			return;
		}
		switch (requestCode) {
		case 0: // retrieve music
			mCurNoti.setRing(data.getDataString());
			updateUI();
			break;
		}
	}

	private void notifyUI(NotificationData pre,NotificationData data,NotificationData next) {
		if (data == null) {
			return;
		}
		
		//include the current one
		int total = mLstNotis.size() + 1;
		//one-based index
		int loc = data.getLocation() + 1;
		mPbPregress.setProgress(loc);
		mPbPregress.setMax(total);
		mTitleDesc.setText(data.getDesc() + " " + loc + "/" + total);
		mCur.notifyUI(data);
		mPrevious.notifyUI(pre);
		mNext.notifyUI(next);
	}
	private NotificationData getById(int id){
		for (int i = 0; i < mLstNotis.size(); i++) {
			NotificationData data = mLstNotis.get(i);
			if (data.getId() == id) {
				return data;
			}
		}
		return null;
	}
	private void updateUI() {
		notifyUI(null,mCurNoti,null);
	}
	public boolean moveNext(){
		// the one must be found,or else, it`s exception
		if (mCurNoti.getLocation() != -1) {
			// if this is not the last one, then move to next
			if (mCurNoti.getLocation() < mLstNotis.size()) {
				NotificationData data = mLstNotis
						.get(mCurNoti.getLocation());
				switchNotif(data);
			}
			// if this is the last one
			else {
				int maxId = 0;
				if(mLstNotis.size() >= 1){
					maxId = mLstNotis.get(mLstNotis.size() - 1).getId();
				}
				if(mCurNoti.getId() > maxId){
					maxId = mCurNoti.getId();
				}
				NotificationData data = new NotificationData(
						maxId + 1, mLstNotis.size()+1);
				switchNotif(data);
			}
		}
		return true;
	}
	public boolean movePrevious(){
		// if this is not the first one, then move back
		if (mCurNoti.getLocation() >= 1) {
			NotificationData data = mLstNotis.get(mCurNoti.getLocation() - 1);
			switchNotif(data);
			return true;
		}
		return false;
	}
	public void commitTranslate(int mode){
		int commitOffset = 0;
		if(mTotalOffset == 0){
			return;
		}
		MainView temp = null;
		switch(mode){
		case ROLLBACK:
			commitOffset = -mTotalOffset;
			Log.d("animation","rollback " + commitOffset);
			break;
		case COMMIT_NEXT:
			commitOffset = -mWidthPixel - (mTotalOffset%mWidthPixel);
			mPrevious.translate(3*mWidthPixel);
			temp = mCur;
			mCur = mNext;
			mNext = mPrevious;
			mPrevious = temp;
			Log.d("animation","next " + commitOffset);
			break;
		case COMMIT_PREVIOUS:
			commitOffset = mWidthPixel - mTotalOffset%mWidthPixel;
			mNext.translate(-3*mWidthPixel);
			temp = mCur;
			mCur = mPrevious;
			mPrevious = mNext;
			mNext = temp;
			Log.d("animation","previous " + commitOffset);
			break;
		}
		translate(commitOffset,1000);
		// update the UI with new data
		NotificationData pre = null;
		NotificationData next = null;
		if(mCurNoti != null){
			int location = mCurNoti.getLocation();
			if( location-1 >= 0){
				pre = mLstNotis.get(location-1);
			}
			if(location + 1 < mLstNotis.size()-1 ){
				next = mLstNotis.get(location + 1);
			}else{
				next = new NotificationData(mCurNoti.getId() + 1, mCurNoti.getLocation() + 1);
			}
		}
		notifyUI(pre,mCurNoti,next);
	}
	public void translate(int offset){
		Log.d("animation", "tranalste:" + offset);
		mCur.translate(offset);
		mPrevious.translate(offset);
		mNext.translate(offset);
	}
	public void translate(int offset,int duration){
		Log.d("animation", "tranalste:" + offset);
		mCur.translate(offset,duration);
		mPrevious.translate(offset,duration);
		mNext.translate(offset,duration);
	}
}