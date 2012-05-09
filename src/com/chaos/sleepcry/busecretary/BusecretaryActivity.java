package com.chaos.sleepcry.busecretary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import utils.LOG;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.sleepcry.busecretary.notify.NotificationData;
import com.chaos.sleepcry.busecretary.notify.NotifyActivity;
import com.chaos.sleepcry.busecretary.notify.NotifyDatabase;
import com.chaos.sleepcry.busecretary.notify.NotifyReceiver;

public class BusecretaryActivity extends Activity implements OnClickListener {

	private TextView mTitleDesc = null;
	private ProgressBar mPbPregress = null;
	private MainView mCur = null;
	private MainView mNext = null;
	private MainView mPrevious = null;
	DatePickerView mdpv = null;
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
	public static final int DB_VER = 9;
	public static final String NOTI_ID = "noti_id";
	public static final int ROLLBACK = 0;
	public static final int COMMIT_NEXT = 1;
	public static final int COMMIT_PREVIOUS = 2;

	/*
	 * @}
	 */
	// the current point
	private Point mPosCur = null;
	// the point when pressed
	private Point mPosDown = null;
	// the previous point
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
		this.registerForContextMenu(mCur.getPaintBoard());
		this.registerForContextMenu(mNext.getPaintBoard());
		this.registerForContextMenu(mPrevious.getPaintBoard());
		ViewGroup.LayoutParams subLayout = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		this.addContentView(mPrevious, subLayout);
		this.addContentView(mNext, subLayout);
		this.addContentView(mCur, subLayout);
		DisplayMetrics dspMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dspMetrics);
		mWidthPixel = dspMetrics.widthPixels;
		mPrevious.translate(-mWidthPixel);
		mNext.translate(mWidthPixel);
		// mCur.translate(new PaneAnimation(mWidthPixel/2));
		/*
		 * initialize the title
		 */
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title);
		try {
			mPbPregress = (ProgressBar) this.findViewById(R.id.pb_progress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mTitleDesc = (TextView) this.findViewById(R.id.tv_title);
		mDB = new NotifyDatabase(this, DB_VER);
		mLstNotis = mDB.query(System.currentTimeMillis());
		Collections.sort(mLstNotis);
		mGeocoder = new Geocoder(this, Locale.getDefault());
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
			Bundle extras = getIntent().getExtras();
			if (intent != null && extras != null
					&& extras.getInt(NOTI_ID, -1) != -1) {
				int id = intent.getExtras().getInt(NOTI_ID);
				data = getById(id);
			}
			// MyDrawable mydraw = extras.getParcelable("background");
			// if(mydraw != null){
			// mPb.add(mydraw);
			// }
			switchNotif(data == null ? mLstNotis.get(0) : data);
		}
		notifyUI(null, mCurNoti, null);
		mPosDown = new Point(-1, -1);
		mPosCur = new Point(-1, -1);
		mPosPre = new Point(-1, -1);
		View v = new View(this, null, 0);
		v.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent motion) {
				int action = motion.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mPosDown.set((int) motion.getX(), (int) motion.getY());
					mPosCur.set((int) motion.getX(), (int) motion.getY());
					mPosPre.set((int) motion.getX(), (int) motion.getY());
					mTotalOffset = 0;
					break;
				case MotionEvent.ACTION_UP:
					double orientation = Math.abs(getOrientation(mPosDown,
							mPosCur));
					int half = BusecretaryActivity.this.getWindowManager()
							.getDefaultDisplay().getHeight() / 2;
					if (orientation >= Math.PI / 4 && mPosCur.y > half
							&& mPosDown.y < half) {
						mCur.collapse();
					}
					mPosCur.set((int) motion.getX(), (int) motion.getY());
					int mode = ROLLBACK;
					if (motion.getEventTime() - motion.getDownTime() <= 100) {

						if (mPosDown.x - mPosCur.x >= 10) {
							// move to previous
							moveNext();
							mode = COMMIT_NEXT;
						} else if (mPosCur.x - mPosDown.x >= 10) {
							// move to next
							movePrevious();
							mode = COMMIT_PREVIOUS;
						}
					} else if (mTotalOffset > 0
							&& mTotalOffset > Math.min(mWidthPixel / 2, 150)) {
						// move to next
						movePrevious();
						mode = COMMIT_PREVIOUS;
					} else if (mTotalOffset < 0
							&& mTotalOffset < -Math.min(mWidthPixel / 2, 150)) {
						// move to previous
						moveNext();
						mode = COMMIT_NEXT;
					}
					// roll back
					commitTranslate(mode);
					if (mPosDown.x - mPosCur.x >= 10) {
						motion.setAction(MotionEvent.ACTION_CANCEL);
					}
					mTotalOffset = 0;
					break;
				case MotionEvent.ACTION_MOVE:
					// don`t move to previous if this is the first
					if (mCurNoti.getLocation() < 1
							&& motion.getX() - mPosDown.x > 0) {
						break;
					}
					// don't move to next if this is the last
					if (mCurNoti.getLocation() == mLstNotis.size()
							&& motion.getX() - mPosDown.x < 0) {
						break;
					}
					mPosCur.set((int) motion.getX(), (int) motion.getY());

					double orientation2 = Math.abs(getOrientation(mPosDown,
							mPosCur));
					if (orientation2 >= Math.PI / 4) {
						break;
					}
					translate(mPosCur.x - mPosPre.x);
					mTotalOffset += mPosCur.x - mPosPre.x;
					mPosPre.set((int) motion.getX(), (int) motion.getY());
					break;
				case MotionEvent.ACTION_CANCEL:
					mPosDown.set(-1, -1);
					mPosCur.set(-1, -1);
					mTotalOffset = 0;
					break;
				}
				mCur.dispatchTouchEvent(motion);
				return true;
			}

		});
		this.addContentView(v, new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

	}

	public static final double getOrientation(Point p1, Point p2) {
		if (p2.x == p1.x) {
			return 0;
		}
		return Math.atan((p2.y - p1.y) / (p2.x - p1.x));
	}

	@Override
	public void onClick(View v) {
		if (v instanceof Button) {
			Button btn = (Button) v;
			int id = btn.getId();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			switch (id) {
			// when
			case OperationAdapter.WHEN:
				Calendar cal = mCurNoti.getWhen().getCalendar();
				mdpv = new DatePickerView(this,
						cal != null ? cal.getTimeInMillis()
								: System.currentTimeMillis());
				new AlertDialog.Builder(this)
						.setView(mdpv)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Calendar cal = Calendar.getInstance();
										cal.setTimeInMillis(mdpv.getTime());
										mCurNoti.getWhen().setCalendar(cal);
										updateUI();

									}

								}).show();
				break;
			// where
			case OperationAdapter.WHERE:
				Intent whereIntent = new Intent();
				whereIntent.setAction(Intent.ACTION_GET_CONTENT);
				whereIntent.addCategory(Intent.CATEGORY_DEFAULT);
				whereIntent.addCategory(Intent.CATEGORY_OPENABLE);
				whereIntent.setData(Uri.parse("geo://weiz.mobi/"));
				startActivityForResult(whereIntent, OperationAdapter.WHERE);
				break;
			// ring
			case OperationAdapter.NOTIFICATION:
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("audio/*");
				this.startActivityForResult(intent, OperationAdapter.NOTIFICATION);
				break;
			// repeat
			case OperationAdapter.REPEAT:
				AlertDialog dlg2 = new AlertDialog.Builder(this)
						.setSingleChoiceItems(
								new ArrayAdapter<String>(
										this,
										android.R.layout.select_dialog_singlechoice,
										mCurNoti.getRepeatCategory().toList()), 0,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mCurNoti.setRepeatCategory(RepeatCategory
												.getInstance(which + 1));
										updateUI();

									}
								}).setTitle("").create();
				dlg2.show();
				break;
			// desc
			case OperationAdapter.WHAT:
				break;
			// weather
			case OperationAdapter.WEATHER:
				break;
			// about
			case OperationAdapter.SEARCH:
				break;
			}
		}
	}

	/*
	 * switch to a new notification data to edit synchronize the list
	 */
	public void switchNotif(NotificationData data) {

		// process the current notification
		if (mCurNoti != null && mLstNotis.indexOf(mCurNoti) == -1) {
//			mCurNoti.setWhat(mCur.getDesc());
			if (mCurNoti.getWhen().getCalendar().getTimeInMillis() <= System
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
			bundle.putString(NotifyDatabase.DESC, mCurNoti.getWhat());
			bundle.putString(NotifyDatabase.RING, mCurNoti.getRing());
			intent.putExtras(bundle);
			PendingIntent pIntent = PendingIntent.getBroadcast(this,
					mCurNoti.getId(), intent, 0);
			// PendingIntent.getActivity(
			// BusecretaryActivity.this, 0, intent, 0);
			AlarmManager am = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			// the time interval the alarm will be launched
			long interval = 0;
			// the time when to start the alarm
			long triggerTime = mCurNoti.getWhen().getCalendar()
					.getTimeInMillis();

			if (triggerTime > System.currentTimeMillis()) {
				boolean bRepeat = true;
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(triggerTime);
				switch (mCurNoti.getRepeatCategory()) {
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
			mDB.insert(mCurNoti.getId(), mCurNoti.getWhen().getCalendar()
					.getTimeInMillis(), mCurNoti.getWhat(), mCurNoti.getRing(),
					mCurNoti.getRepeatCategory().getId(), null);
			// synchronize the list
			mLstNotis.add(mCurNoti.getLocation(), mCurNoti);
		}
		// process the to-go one
		if (mLstNotis.indexOf(data) != -1 && data != null) {
			Intent intent = new Intent(BusecretaryActivity.this,
					NotifyActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(NotifyDatabase.DESC, data.getWhat());
			bundle.putString(NotifyDatabase.RING, data.getRing());
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
//			mCur.setWhat(mCurNoti.getWhat());
		}
	}

	public void onDestroy() {
		super.onDestroy();
		switchNotif(null);
		for (int i = 0; i < mLstNotis.size(); i++) {
			mLstNotis.get(i).setBmp(null);
		}
		mCurNoti.setBmp(null);
		System.gc();
	}

	private Geocoder mGeocoder = null;
	private Address mAddress = null;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (RESULT_OK != resultCode) {
			return;
		}
		switch (requestCode) {
		// TODO:
		case OperationAdapter.NOTIFICATION: // retrieve music
			mCurNoti.setRing(data.getDataString());
			updateUI();
			break;
		case OperationAdapter.WHERE:
			if (data != null && data.getExtras() != null && data.getExtras().containsKey("data")) {
				String geoData = data.getStringExtra("data");
				if(geoData == null)break;
				String[] lonlat = geoData.split(",");
				if(lonlat.length < 2)break;
				double latitude = Double.parseDouble(lonlat[0]);
				double longitude = Double.parseDouble(lonlat[1]);
				List<Address> addresses = null;
				String whereString = "";
				try {
					addresses = mGeocoder.getFromLocation(latitude, longitude,1);
					if (addresses != null && addresses.size() > 0) {
						mAddress = addresses.get(0);
						
						for(int i=0;i<=mAddress.getMaxAddressLineIndex();i++) {
							whereString +=  mAddress.getAddressLine(i);
						}
						mCurNoti.setWhere(whereString);
						updateUI();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					whereString = geoData;
				}
				mCurNoti.setWhere(whereString);
				updateUI();
			}
			break;

		}
	}

	private void notifyUI(NotificationData pre, NotificationData data,
			NotificationData next) {
		if (pre == null) {
			pre = new NotificationData();
		}
		if (data == null) {
			data = new NotificationData();
		}
		if (next == null) {
			next = new NotificationData();
		}
		// include the current one
		int total = mLstNotis.size() + 1;
		// one-based index
		int loc = data.getLocation() + 1;
		mPbPregress.setProgress(loc);
		mPbPregress.setMax(total);
		mTitleDesc.setText(data.getWhat() + " " + loc + "/" + total);
		mCur.notifyUI(data);
		mPrevious.notifyUI(pre);
		mNext.notifyUI(next);
		mCur.setData(mCurNoti);
		LOG.logMem(this);
	}

	private NotificationData getById(int id) {
		for (int i = 0; i < mLstNotis.size(); i++) {
			NotificationData data = mLstNotis.get(i);
			if (data.getId() == id) {
				return data;
			}
		}
		return null;
	}

	protected void updateUI() {
		notifyUI(null, mCurNoti, null);
	}

	public boolean moveNext() {
		// the one must be found,or else, it`s exception
		if (mCurNoti.getLocation() != -1) {
			// if this is not the last one, then move to next
			if (mCurNoti.getLocation() < mLstNotis.size()) {
				NotificationData data = mLstNotis.get(mCurNoti.getLocation());
				switchNotif(data);
			}
		}
		return true;
	}

	public boolean movePrevious() {
		// if this is not the first one, then move back
		if (mCurNoti.getLocation() >= 1) {
			NotificationData data = mLstNotis.get(mCurNoti.getLocation() - 1);
			switchNotif(data);
			return true;
		}
		return false;
	}

	public void commitTranslate(int mode) {
		int commitOffset = 0;
		if (mTotalOffset == 0) {
			return;
		}
		MainView temp = null;
		switch (mode) {
		case ROLLBACK:
			commitOffset = -mTotalOffset;
			Log.d("animation", "rollback " + commitOffset);
			break;
		case COMMIT_NEXT:
			commitOffset = -mWidthPixel - (mTotalOffset % mWidthPixel);
			mPrevious.translate(3 * mWidthPixel);
			temp = mCur;
			mCur = mNext;
			mNext = mPrevious;
			mPrevious = temp;
			mNext.reset();
			mCur.reset();
			mPrevious.reset();
			Log.d("animation", "next " + commitOffset);
			break;
		case COMMIT_PREVIOUS:
			commitOffset = mWidthPixel - mTotalOffset % mWidthPixel;
			mNext.translate(-3 * mWidthPixel);
			temp = mCur;
			mCur = mPrevious;
			mPrevious = mNext;
			mNext = temp;
			mNext.reset();
			mCur.reset();
			mPrevious.reset();
			Log.d("animation", "previous " + commitOffset);
			break;
		}
		translate(commitOffset, 300);
		// update the UI with new data
		NotificationData pre = null;
		NotificationData next = null;
		if (mCurNoti != null) {
			int location = mCurNoti.getLocation();
			if (location - 1 >= 0) {
				pre = mLstNotis.get(location - 1);
			}
			if (location < mLstNotis.size()) {
				next = mLstNotis.get(location);
			} else {
				next = new NotificationData(mCurNoti.getId() + 1,
						mCurNoti.getLocation() + 1);
			}
		}
		switch (mode) {
		case COMMIT_NEXT:
			if (next != null) {
				next.setBmp(null);
			}
			break;
		case COMMIT_PREVIOUS:
			if (pre != null) {
				pre.setBmp(null);
			}
			break;
		}

		notifyUI(pre, mCurNoti, next);
	}

	public void translate(int offset) {
		Log.d("animation", "tranalste:" + offset);
		mCur.translate(offset);
		mPrevious.translate(offset);
		mNext.translate(offset);
	}

	public void translate(int offset, int duration) {
		Log.d("animation", "tranalste:" + offset);
		mCur.translate(offset, duration);
		mPrevious.translate(offset, duration);
		mNext.translate(offset, duration);
	}

	public void onResume() {
		super.onResume();
		mCur.resume();
		mPrevious.resume();
		mNext.resume();
	}

	public void onPause() {
		super.onPause();
		mCur.pause();
		mPrevious.pause();
		mNext.pause();
	}

	public void onCreateContextMenu(ContextMenu cm, View v,
			ContextMenuInfo cminfo) {
		if (v.getId() == R.id.canvas) {
			cm.clear();
			this.getMenuInflater().inflate(R.menu.totaledit, cm);
		}
	}

	public boolean onContextItemSelected(MenuItem menuItem) {
		int id = menuItem.getItemId();
		switch (id) {
		case R.id.menuedit:
			return true;
		case R.id.menuredo:
			return true;
		case R.id.menuundo:
			return true;
		case R.id.menuclear:
			return true;
		}
		return false;
	}
}