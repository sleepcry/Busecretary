package com.chaos.sleepcry.busecretary.notify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import utils.LOG;
import android.app.Activity;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.R;

public class NotifyActivity extends Activity implements OnClickListener{
	private MediaPlayer mPlayer = null;	//player to play the notification rings
	private TextView mTvDesc = null;
	private Button mBtnConfirm = null;
	private Button mBtnDelay = null;
	PowerManager.WakeLock mScreenLock = null;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.notify);
		Intent intent = getIntent();
		if(null == intent){
			finish();
		}
		PowerManager pmManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mScreenLock = pmManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "NotifyActivity");
		mScreenLock.acquire();
		String strWhat = intent.getExtras().getString(NotifyDatabase.WHAT);
		long when = intent.getExtras().getLong(NotifyDatabase.WHEN);
		String strWhere = intent.getExtras().getString(NotifyDatabase.WHERE);
		String strUri = intent.getExtras().getString(NotifyDatabase.RING);
		String strBmp = intent.getExtras().getString(NotifyDatabase.BMP);
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
	}
	@Override
	public void onClick(View v) {
		int nID = v.getId();
		switch(nID){
		case R.id.btn_confirm:
			if (mPlayer != null) {
				mPlayer.stop();
			}
			mScreenLock.release();
			finish();
			break;
		case R.id.btn_delay:
			if (mPlayer != null) {
				mPlayer.stop();
			}
			mScreenLock.release();
			finish();
			break;
		}
		
	}
}
