package com.chaos.sleepcry.busecretary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.sleepcry.busecretary.notify.NotificationData;
import com.chaos.sleepcry.busecretary.notify.NotifyDatabase;
import com.chaos.sleepcry.busecretary.notify.NotifyReceiver;
import com.chaos.sleepcry.busecretary.utils.LOG;
import com.chaos.sleepcry.busecretary.utils.MathUtils;
import com.chaos.sleepcry.busecretary.utils.SmartMediaPlayer;

public class BusecretaryActivity extends Activity implements OnClickListener,
		OnTouchListener {

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
	public static final int DB_VER = 13;
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
	boolean is_horizontal_move = false;
	boolean is_vertical_move = false;
	static final double MOVE_SENSITIVITY = 80;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		SmartMediaPlayer.initVolumeType(this);
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

		mGeocoder = new Geocoder(this, Locale.getDefault());
		mPosDown = new Point(-1, -1);
		mPosCur = new Point(-1, -1);
		mPosPre = new Point(-1, -1);
		View v = new View(this, null, 0);
		v.setOnTouchListener(this);
		this.addContentView(v, new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		initNotif();
	}

	protected void onNewIntent(Intent intent) {
		initNotif();
	}

	private void initNotif() {
		mLstNotis = mDB.query(System.currentTimeMillis());
		if (mLstNotis != null)
			Collections.sort(mLstNotis);
		/*
		 * if get nothing from the database,the first time run this program,for
		 * example.
		 */
		if (mLstNotis == null) {
			mLstNotis = new ArrayList<NotificationData>();
		}
		if (mLstNotis.size() > 0) {
			mCurNoti = mLstNotis.get(0);
			Intent intent = getIntent();
			Bundle extras = getIntent().getExtras();
			if (intent != null && extras != null
					&& extras.getInt(NOTI_ID, -1) != -1) {
				int id = intent.getExtras().getInt(NOTI_ID);
				mCurNoti = getById(id);
			}
			switchNotif(mCurNoti);
		}
		int index = mLstNotis.indexOf(mCurNoti);
		NotificationData pre = null;
		NotificationData next = null;
		if (index >= 1) {
			pre = mLstNotis.get(index - 1);
		}
		if (index < mLstNotis.size() - 1) {
			next = mLstNotis.get(index + 1);
		}
		notifyUI(pre, mCurNoti, next);
	}

	public boolean canMoveHorizontal(float x) {
		// don`t move to previous if this is the first
		if (mCurNoti.getLocation() < 1 && x - mPosDown.x > 0) {
			return false;
		}
		// don't move to next if this is the last
		if (mCurNoti.getLocation() == mLstNotis.size() && x - mPosDown.x < 0) {
			return false;
		}
		return true;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();
		this.getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int id = item.getItemId();
		switch (id) {
		case R.id.remove:
			removeCurrent();
			break;
		case R.id.menushare:
			if(mCurNoti == null)break;
			try {
				String url = Media.insertImage(getContentResolver(),
						mCurNoti.getBmpPath(), mCurNoti.getWhat(), mCurNoti.getWhat());
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				Uri screenshotUri = Uri.parse(url);
				sharingIntent.setType("image/png");
				sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
				sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "DrawingToDo");
				sharingIntent.putExtra(Intent.EXTRA_TEXT, mCur.getDescription(mCurNoti));
				sharingIntent.putExtra(Intent.EXTRA_TITLE, "DrawingToDo");
				startActivity(Intent.createChooser(sharingIntent,
						"Share image using"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return true;
		default:
			break;
		}

		return false;
	}

	public void removeCurrent() {
		if (mCurNoti == null)
			return;
		int curIndex = mCurNoti.getLocation();
		// for(int i = curIndex;i<mLstNotis.size();i++) {
		// NotificationData data = mLstNotis.get(i);
		// data.setLocation(data.getLocation()-1);
		// }
		if (curIndex == -1)
			return;
		// there are more at right, move right
		if (curIndex < mLstNotis.size()) {
			NotificationData data = mLstNotis.get(mCurNoti.getLocation());
			mDB.delete(mCurNoti.getId());
			mCurNoti = null;
			switchNotif(data);
			commitTranslate(COMMIT_NEXT);
			// TODO:
		} else if (curIndex > 0) {
			// there are more at left, move left
			NotificationData data = mLstNotis.get(mCurNoti.getLocation() - 1);
			mDB.delete(mCurNoti.getId());
			mCurNoti = null;
			switchNotif(data);
			commitTranslate(COMMIT_PREVIOUS);
		} else {
			// there is only this one
			mDB.delete(mCurNoti.getId());
			mCurNoti = null;
			commitTranslate(ROLLBACK);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent motion) {
		if (mCurNoti == null)
			return false;
		boolean bret = mCur.dispatchTouchEvent(motion);
		if (motion.getPointerCount() > 1) {
			return bret;
		}
		int action = motion.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mPosDown.set((int) motion.getX(), (int) motion.getY());
			mPosCur.set((int) motion.getX(), (int) motion.getY());
			mPosPre.set((int) motion.getX(), (int) motion.getY());
			mTotalOffset = 0;
			is_horizontal_move = false;
			is_vertical_move = false;
			break;
		case MotionEvent.ACTION_UP:
			mPosCur.set((int) motion.getX(), (int) motion.getY());
			if (is_vertical_move) {
				int half = BusecretaryActivity.this.getWindowManager()
						.getDefaultDisplay().getHeight() / 2;
				if (mPosCur.y > half && mPosDown.y < half) {
					mCur.collapse();
				}
				LOG.D("sensitivity", "vertical commit");
			} else if (is_horizontal_move) {
				LOG.D("sensitivity", "horizontal commit");
				// if (!canMoveHorizontal(motion.getX())) {
				// break;
				// }
				int mode = ROLLBACK;
				if (motion.getEventTime() - motion.getDownTime() <= 100) {

					if (mPosDown.x - mPosCur.x >= 100) {
						// move to previous
						moveNext();
						mode = COMMIT_NEXT;
					} else if (mPosCur.x - mPosDown.x >= 100) {
						// move to next
						movePrevious();
						mode = COMMIT_PREVIOUS;
					}
				} else if (mTotalOffset > 0
						&& mTotalOffset > Math.min(mWidthPixel / 2, 200)) {
					// move to next
					movePrevious();
					mode = COMMIT_PREVIOUS;
				} else if (mTotalOffset < 0
						&& mTotalOffset < -Math.min(mWidthPixel / 2, 200)) {
					// move to previous
					moveNext();
					mode = COMMIT_NEXT;
				}
				// roll back
				commitTranslate(mode);
				if (mPosDown.x - mPosCur.x >= 10) {
					motion.setAction(MotionEvent.ACTION_CANCEL);
				}
			}
			mTotalOffset = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			mPosCur.set((int) motion.getX(), (int) motion.getY());
			// if (!canMoveHorizontal(motion.getX())) {
			// break;
			// }
			if (!is_vertical_move && !is_horizontal_move) {
				if (MathUtils.dst(new PointF(motion.getX(), motion.getY()),
						new PointF(mPosDown.x, mPosDown.y)) >= MOVE_SENSITIVITY) {
					double orientation2 = Math.abs(MathUtils.getOrientation(
							mPosDown, mPosCur));
					if (orientation2 >= Math.PI / 3) {
						is_vertical_move = true;
						LOG.D("sensitivity", "vertical move");
					} else if (orientation2 <= Math.PI / 6
							&& canMoveHorizontal(motion.getX())) {
						is_horizontal_move = true;
						LOG.D("sensitivity", "horizontal move");
					}
				}
			}
			if (is_horizontal_move) {
				translate(mPosCur.x - mPosPre.x);
				mTotalOffset += mPosCur.x - mPosPre.x;
				mPosPre.set(mPosCur.x, mPosCur.y);
			}
			break;
		}
		return true;
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
				mdpv = new DatePickerView(this,mCurNoti.getWhen());
				new AlertDialog.Builder(this)
						.setView(mdpv)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mCurNoti.setWhen(mdpv.getTime());
										updateUI();
									}

								}).show();
				break;
			// where
			case OperationAdapter.WHERE:
				try {
					Intent whereIntent = new Intent();
					whereIntent.setAction(Intent.ACTION_GET_CONTENT);
					whereIntent.addCategory(Intent.CATEGORY_DEFAULT);
					whereIntent.addCategory(Intent.CATEGORY_OPENABLE);
					whereIntent.setData(Uri.parse("geo://weiz.mobi/"));
					startActivityForResult(whereIntent, OperationAdapter.WHERE);
				} catch (Exception e) {
					new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMessage(R.string.noweiz).show();
//					Uri uri = Uri.parse("market://details?id=com.chaoslab.locationshare");
				}

				break;
			// ring
			case OperationAdapter.NOTIFICATION:
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("audio/*");
				this.startActivityForResult(intent,
						OperationAdapter.NOTIFICATION);
				break;
			// repeat
			case OperationAdapter.REPEAT:
				AlertDialog dlg2 = new AlertDialog.Builder(this)
						.setSingleChoiceItems(RepeatCategory.toArray(),
								mCurNoti.getRepeatCategory().getId() - 1,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mCurNoti.setRepeatCategory(RepeatCategory
												.getInstance(which + 1));
										updateUI();
										dialog.dismiss();
									}
								}).setTitle("").create();
				dlg2.show();
				break;
			// desc
			case OperationAdapter.WHAT:
				break;
			// about
			case OperationAdapter.SEARCH:
				SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
				sm.startSearch(mCurNoti.getWhat(), false,
						this.getComponentName(), null, true);
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
			// mCurNoti.setWhat(mCur.getDesc());
			if (mCurNoti.getWhen() <= System.currentTimeMillis()) {
				Toast.makeText(this, "the time is in the past!",
						Toast.LENGTH_SHORT).show();
			}

			// the time interval the alarm will be launched
			long interval = 0;
			// the time when to start the alarm
			long triggerTime = mCurNoti.getWhen();

			if (triggerTime > System.currentTimeMillis()) {
				/*
				 * @{ set the current notification
				 */
				Intent intent = new Intent(BusecretaryActivity.this,
						NotifyReceiver.class);
				Bundle bundle = new Bundle();
				bundle.putString(NotifyDatabase.WHAT, mCurNoti.getWhat());
				bundle.putLong(NotifyDatabase.WHEN, mCurNoti.getWhen());
				bundle.putString(NotifyDatabase.WHERE, mCurNoti.getWhere());
				bundle.putString(NotifyDatabase.RING, mCurNoti.getRing());
				bundle.putString(NotifyDatabase.BMP, mCurNoti.getBmpPath());
				bundle.putInt(NotifyDatabase.ID, mCurNoti.getId());
				bundle.putInt(NotifyDatabase.CATEGORY, mCurNoti.getRepeatCategory().getId());
				intent.putExtras(bundle);
				LOG.D("notification", "what?:" + mCurNoti.getWhat());
				LOG.D("notification", "when?" + mCurNoti.getWhen());
				LOG.D("notification", "where?" + mCurNoti.getWhere());
				LOG.D("notification", "ring?" + mCurNoti.getRing());
				LOG.D("notification", "bmp?" + mCurNoti.getBmpPath());
				LOG.D("notification", "id?" + mCurNoti.getId());
				PendingIntent pIntent = PendingIntent.getBroadcast(this,
						mCurNoti.getId(), intent, 0);
				AlarmManager am = (AlarmManager) this
						.getSystemService(Context.ALARM_SERVICE);
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
				LOG.D("notification", "set alarm id:" + mCurNoti.getId());
			}
			/*
			 * save the current notification
			 */
			mDB.insert(mCurNoti.getId(), mCurNoti.getWhen(), mCurNoti.getWhat(), mCurNoti.getRing(),
					mCurNoti.getRepeatCategory().getId(), null,
					mCurNoti.getWhere());
			// synchronize the list
			mLstNotis.add(mCurNoti.getLocation(), mCurNoti);
		}
		// process the to-go one
		if (mLstNotis.indexOf(data) != -1 && data != null) {
			Intent intent = new Intent(BusecretaryActivity.this,
					NotifyReceiver.class);
			PendingIntent pIntent = PendingIntent.getBroadcast(this,
					data.getId(), intent, 0);

			/*
			 * cancel this notification when enter and set it again while exit
			 * with the modification
			 */
			pIntent.cancel();
			LOG.D("notification", "cancel alarm id:" + data.getId());
			data.setLocation(mLstNotis.indexOf(data));
			mLstNotis.remove(data);
		}
		// move to the specified data
		if (data != null) {
			mCurNoti = data;
			// mCur.setWhat(mCurNoti.getWhat());
		}
	}

	public void onDestroy() {
		super.onDestroy();
		mNext.destroy();
		mCur.destroy();
		mPrevious.destroy();
		if (mCurNoti != null) {
			switchNotif(null);
			if (mCurNoti.getBmp() != null) {
				mCurNoti.getBmp().recycle();
			}
			mCurNoti = null;
		}

		mLstNotis.clear();
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
			if (data != null && data.getExtras() != null
					&& data.getExtras().containsKey("data")) {
				String geoData = data.getStringExtra("data");
				if (geoData == null)
					break;
				String[] lonlat = geoData.split(",");
				if (lonlat.length < 2)
					break;
				double latitude = Double.parseDouble(lonlat[0]);
				double longitude = Double.parseDouble(lonlat[1]);
				List<Address> addresses = null;
				String whereString = "";
				try {
					addresses = mGeocoder.getFromLocation(latitude, longitude,
							1);
					if (addresses != null && addresses.size() > 0) {
						mAddress = addresses.get(0);

						for (int i = 0; i <= mAddress.getMaxAddressLineIndex(); i++) {
							whereString += mAddress.getAddressLine(i);
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
		// include the current one
		int total = mLstNotis.size() + (data != null ? 1 : 0);
		// one-based index
		int loc = data == null ? 0 : (data.getLocation() + 1);
		mPbPregress.setProgress(loc);
		mPbPregress.setMax(total);
		mTitleDesc.setText((data == null ? "" : data.getWhat()) + " " + loc
				+ "/" + total);
		mCur.notifyUI(data);
		mPrevious.notifyUI(pre);
		mNext.notifyUI(next);
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
		notifyUI(mPrevious.getData(), mCurNoti, mNext.getData());
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
		// if (mTotalOffset == 0) {
		// return;
		// }
		MainView temp = null;
		boolean effect = true;
		switch (mode) {
		case ROLLBACK:
			effect = false;
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
		translate(commitOffset, 300, effect);
		// update the UI with new data
		NotificationData pre = null;
		NotificationData beforepre = null;
		NotificationData afternext = null;
		NotificationData next = null;
		if (mCurNoti != null) {
			int location = mCurNoti.getLocation();
			if (location - 1 >= 0) {
				pre = mLstNotis.get(location - 1);
			}
			if (location - 2 >= 0) {
				beforepre = mLstNotis.get(location - 2);
			}

			if (location < mLstNotis.size()) {
				next = mLstNotis.get(location);
			}
			if (location < mLstNotis.size() - 1) {
				afternext = mLstNotis.get(location + 1);
			}
		}
		/*
		 * to release memory critically
		 */
		switch (mode) {
		case COMMIT_NEXT:
			if (beforepre != null && beforepre.getBmp() != null) {
				beforepre.getBmp().recycle();
				beforepre.setBmp(null);
			}
			break;
		case COMMIT_PREVIOUS:
			if (afternext != null && afternext.getBmp() != null) {
				afternext.getBmp().recycle();
				afternext.setBmp(null);
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

	public void translate(int offset, int duration, boolean effect) {
		Log.d("animation", "tranalste:" + offset);
		mCur.translate(offset, duration, effect);
		mPrevious.translate(offset, duration, false);
		mNext.translate(offset, duration, false);
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
		// mNext.reset();
		// mCur.reset();
		// mPrevious.reset();
		// if (mCurNoti != null) {
		// switchNotif(null);
		// mCurNoti.getBmp().recycle();
		// mCurNoti = null;
		// }
		//
		// mLstNotis.clear();
		// System.gc();
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