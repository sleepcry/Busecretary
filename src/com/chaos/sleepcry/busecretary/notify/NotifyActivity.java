package com.chaos.sleepcry.busecretary.notify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.RepeatCategory;
import com.chaos.sleepcry.busecretary.utils.LOG;

public class NotifyActivity extends Activity implements OnClickListener{
	private MediaPlayer mPlayer = null;	//player to play the notification rings
	private TextView mTvDesc = null;
	private Button mBtnConfirm = null;
	private Button mBtnDelay = null;
	private Button mBtnCancel = null;
	PowerManager.WakeLock mScreenLock = null;
	String strWhere,strUri,strBmp,strWhat;
	RepeatCategory category;
	int mId;
	long when;
	static final long TIME_OUT = 50*1000;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.notify);
		Intent intent = getIntent();
		if(null == intent){
			finish();
		}
		PowerManager pmManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mScreenLock = pmManager.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "NotifyActivity");
		mScreenLock.acquire(TIME_OUT);
		strWhat = intent.getExtras().getString(NotifyDatabase.WHAT);
		when = intent.getExtras().getLong(NotifyDatabase.WHEN);
		strWhere = intent.getExtras().getString(NotifyDatabase.WHERE);
		strUri = intent.getExtras().getString(NotifyDatabase.RING);
		strBmp = intent.getExtras().getString(NotifyDatabase.BMP);
		category = RepeatCategory.getInstance(intent.getExtras().getInt(NotifyDatabase.CATEGORY));
		mId = intent.getExtras().getInt(NotifyDatabase.ID);
		LOG.D("notification", "notify what?:"+strWhat);
		LOG.D("notification", "notify when?"+new Date(when).toGMTString());
		LOG.D("notification", "notify where?"+strWhere);
		LOG.D("notification", "notify ring?"+strUri);
		LOG.D("notification", "notify bmp?"+strBmp);
		if(strUri != null){
			mPlayer = MediaPlayer.create(this, Uri.parse(strUri));
			mPlayer.start();
		}
		if(strBmp != null) {
			try {
				System.gc();
				File file = new File(strBmp);
				FileInputStream fis = new FileInputStream(file);
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = false;
				opts.inSampleSize = 2;
				Bitmap bmp = BitmapFactory.decodeStream(fis, null, opts);
				findViewById(R.id.root).setBackgroundDrawable(new BitmapDrawable(bmp));
				fis.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException ioe) {

			}
		}
		
		/*
		 * initialize the ui
		 */
		mTvDesc = (TextView) this.findViewById(R.id.tv_notify_msg);
		String msg = (strWhere==null?"":strWhere+"\n")  + (strWhat==null?"":strWhat);
		mTvDesc.setText(msg);
		mBtnConfirm = (Button) this.findViewById(R.id.btn_confirm);
		mBtnConfirm.setOnClickListener(this);
		mBtnDelay = (Button) this.findViewById(R.id.btn_delay);
		mBtnDelay.setOnClickListener(this);
		mBtnCancel = (Button) findViewById(R.id.btn_cancel);
		if(category == RepeatCategory.LAST) {
			mBtnCancel.setVisibility(View.VISIBLE);
			mBtnCancel.setOnClickListener(this);
		}
	}
	protected void cancel() {
		Intent intent = new Intent(this,
				NotifyReceiver.class);
		PendingIntent pIntent = PendingIntent.getBroadcast(this,
				mId, intent, 0);
		pIntent.cancel();
	}
	protected void delay() {
		Intent intent = new Intent(this,
				NotifyReceiver.class);
		Bundle bundle = new Bundle();
		bundle.putString(NotifyDatabase.WHAT, strWhat);
		bundle.putLong(NotifyDatabase.WHEN, when);
		bundle.putString(NotifyDatabase.WHERE, strWhere);
		bundle.putString(NotifyDatabase.RING, strUri);
		bundle.putString(NotifyDatabase.BMP, strBmp);
		bundle.putInt(NotifyDatabase.CATEGORY, category.getId());
		bundle.putInt(NotifyDatabase.ID, mId);
		intent.putExtras(bundle);
		PendingIntent pIntent = PendingIntent.getBroadcast(this,
				0, intent, 0);
		AlarmManager am = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		long triggerTime = when + 10*60*1000;
		if(category != RepeatCategory.LAST) {
			am.set(AlarmManager.RTC_WAKEUP, triggerTime, pIntent);			
		}
	}
	@Override
	public void onClick(View v) {
		if (mPlayer != null) {
			mPlayer.stop();
		}
		int nID = v.getId();
		switch(nID){
		case R.id.btn_confirm:
			finish();
			break;
		case R.id.btn_delay:
			delay();
			finish();
			break;
		case R.id.btn_cancel:
			cancel();
			finish();
			break;
		}
		
	}
}
