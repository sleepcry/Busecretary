package com.chaos.sleepcry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class BusecretaryActivity extends Activity implements OnClickListener {
	/*
	 * @{ define the UI elements
	 */
	private Button mBtnDateDesc = null; // describe the date
	private Button mBtnTimeDesc = null; // describe the time on the day
	private Button mBtnRingDesc = null; // describe the notification ring
	private Button mBtnRepeatDesc = null; // describe the repeat pattern
	private Button mBtnPrevious = null;
	private Button mBtnNext = null;
	private EditText mEtDesc = null; // describe the notification content
	private TextView mTitleDesc = null;
	private ProgressBar mPbPregress = null;
	/*
	 * @}
	 */

	/*
	 * the date used to define a notification
	 */
	public static class NotificationData {
		public int mId;
		public Day day;
		public String desc;
		public String ring;
		public RepeatCategory category;
		public int location = 0; // the current record location in the total

		public NotificationData() {
			this(0, -1);
		}

		public NotificationData(int id, int loc) {
			mId = id;
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			day = new Day(cal);
			desc = new String("");
			ring = null;
			category = RepeatCategory.NONE;
			location = loc;
		}
	}

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

	/*
	 * @}
	 */
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		/*
		 * @{ initialize all view components
		 */
		mBtnDateDesc = (Button) this.findViewById(R.id.btn_date_desc);
		mBtnDateDesc.setOnClickListener(this);
		mBtnTimeDesc = (Button) this.findViewById(R.id.btn_time_desc);
		mBtnTimeDesc.setOnClickListener(this);
		mBtnRingDesc = (Button) this.findViewById(R.id.btn_ring_desc);
		mBtnRingDesc.setOnClickListener(this);
		mBtnRepeatDesc = (Button) this.findViewById(R.id.btn_repeat_desc);
		mBtnRepeatDesc.setOnClickListener(this);
		mBtnPrevious = (Button) this.findViewById(R.id.btn_previous);
		mBtnPrevious.setOnClickListener(this);
		mBtnNext = (Button) this.findViewById(R.id.btn_next);
		mBtnNext.setOnClickListener(this);
		mEtDesc = (EditText) this.findViewById(R.id.et_desc);
		/*
		 * @}
		 */
		WindowManager wm = this.getWindowManager();
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title);
		/*
		 * initialize the title
		 */
		mPbPregress = (ProgressBar) this.findViewById(R.id.pb_progress);
		mPbPregress.setBackgroundColor(0x007fff7f);
		mTitleDesc = (TextView) this.findViewById(R.id.tv_title);
		mDB = new NotifyDatabase(this, 2);
		mLstNotis = mDB.query(System.currentTimeMillis());
		/*
		 * if the list has not been built if gotten nothing from the
		 * database,the first time run this program,for example.
		 */
		if (mLstNotis == null) {
			mLstNotis = new ArrayList<NotificationData>();
		}
		if (mLstNotis.size() == 0) {
			// NotificationData data = new NotificationData(0);
			// mLstNotis.add(data);
			// mCurNoti = data;
			switchNotif(new NotificationData(0, 0));
		}
		// if found something,at least one record
		else {
			// mCurNoti = mLstNotis.get(0);
			switchNotif(mLstNotis.get(0));
		}
		Application app = this.getApplication();
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
							Calendar cal = mCurNoti.day.getCalendar();
							cal.set(Calendar.YEAR, year);
							cal.set(Calendar.MONTH, monthOfYear);
							cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							mCurNoti.day.setCalendar(cal);
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
							Calendar cal = mCurNoti.day.getCalendar();
							cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
							cal.set(Calendar.MINUTE, minute);
							mCurNoti.day.setCalendar(cal);
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
									mCurNoti.category.toList()), 0,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mCurNoti.category = RepeatCategory
											.getInstance(which + 1);
									updateUI();

								}
							}).setTitle("").create();
			dlg.show();
			break;
		case R.id.btn_previous:
			// if this is not the first one, then move back
			if (mCurNoti.location >= 1) {
				NotificationData data = mLstNotis.get(mCurNoti.location - 1);
				switchNotif(data);
			}
			break;
		case R.id.btn_next:
			// the one must be found,or else, it`s exception
			if (mCurNoti.location != -1) {
				// if this is not the last one, then move to next
				if (mCurNoti.location < mLstNotis.size()) {
					NotificationData data = mLstNotis
							.get(mCurNoti.location);
					switchNotif(data);
				}
				// if this is the last one
				else {
					int maxId = 0;
					if(mLstNotis.size() >= 1){
						maxId = mLstNotis.get(mLstNotis.size() - 1).mId;
					}
					if(mCurNoti.mId > maxId){
						maxId = mCurNoti.mId;
					}
					NotificationData data = new NotificationData(
							maxId + 1, mLstNotis.size()+1);
					switchNotif(data);
				}
			}
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
			if (mCurNoti.ring == null) {
				Toast.makeText(this, "the notification ring is empty!",
						Toast.LENGTH_SHORT).show();
			}
			mCurNoti.desc = mEtDesc.getText().toString();
			if (mCurNoti.desc == null || mCurNoti.desc.equals("")) {
				Toast.makeText(this, "the description is empty!",
						Toast.LENGTH_SHORT).show();
			}
			if (mCurNoti.day.getCalendar().getTimeInMillis() <= System
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
			bundle.putString(DESC, mCurNoti.desc);
			bundle.putString(RING, mCurNoti.ring);
			intent.putExtras(bundle);
			PendingIntent pIntent = PendingIntent.getBroadcast(this, mCurNoti.mId, intent, 0);
			//PendingIntent.getActivity(
			//		BusecretaryActivity.this, 0, intent, 0);
			AlarmManager am = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			// the time interval the alarm will be launched
			long interval = 0;
			// the time when to start the alarm
			long triggerTime = mCurNoti.day.getCalendar().getTimeInMillis();
			boolean bRepeat = true;
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(triggerTime);
			switch (mCurNoti.category) {
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
			int month = cal.get(Calendar.DAY_OF_MONTH) + 1;
			if (bRepeat) {
				am.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval,
						pIntent);
			} else {
				am.set(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);
			}
			/*
			 * save the current notification
			 */
			mDB.insert(mCurNoti.mId, mCurNoti.day.getCalendar()
					.getTimeInMillis(), mCurNoti.desc, mCurNoti.ring,
					mCurNoti.category.getId());
			// synchronize the list
			mLstNotis.add(mCurNoti.location, mCurNoti);
		}
		// process the to-go one
		if (mLstNotis.indexOf(data) != -1) {
			Intent intent = new Intent(BusecretaryActivity.this,
					NotifyActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(DESC, mCurNoti.desc);
			bundle.putString(RING, mCurNoti.ring);
			intent.putExtras(bundle);
			PendingIntent pIntent = PendingIntent.getActivity(
					BusecretaryActivity.this, 0, intent, 0);
			/*
			 * cancel this notification when enter and set it again while exit
			 * with the modification
			 */
			pIntent.cancel();
			data.location = mLstNotis.indexOf(data);
			mLstNotis.remove(data);
		}
		// move to the specified data
		if (data != null) {
			mCurNoti = data;
			mEtDesc.setText(mCurNoti.desc);
		}
		// update the UI with new data
		notifyUI(mCurNoti);

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
			mCurNoti.ring = data.getDataString();
			updateUI();
			break;
		}
	}

	private void notifyUI(NotificationData data) {
		if (data == null) {
			return;
		}
		mBtnDateDesc.setText(data.day.getDateString());
		mBtnTimeDesc.setText(data.day.getTimeString());
		if (data.ring != null) {
			Uri ring = Uri.parse(data.ring);
			Cursor cursor = getContentResolver().query(ring,
					new String[] { MediaStore.Audio.Media.TITLE }, null, null,
					null);
			cursor.moveToFirst();
			if (!cursor.isNull(0)) {
				mBtnRingDesc.setText(cursor.getString(0));
			}
		} else {
			mBtnRingDesc.setText("choose a ring here...");
		}
		mBtnRepeatDesc.setText(data.category.getDesc());
		//include the current one
		int total = mLstNotis.size() + 1;
		//one-based index
		int loc = data.location + 1;
		mPbPregress.setProgress(loc);
		mPbPregress.setMax(total);
		mTitleDesc.setText(data.desc + " " + loc + "/" + total);
		//mEtDesc.setText(data.desc);
	}

	private void updateUI() {
		notifyUI(mCurNoti);
	}
}