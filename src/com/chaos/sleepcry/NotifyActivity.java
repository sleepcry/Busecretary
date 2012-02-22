package com.chaos.sleepcry;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotifyActivity extends Activity implements OnClickListener{
	private MediaPlayer mPlayer = null;	//player to play the notification rings
	private TextView mTvDesc = null;
	private Button mBtnConfirm = null;
	private Button mBtnDelay = null;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.notify);
		Intent intent = getIntent();
		if(null == intent){
			finish();
		}
		String strDesc = intent.getExtras().getString(BusecretaryActivity.DESC);
		String strUri = intent.getExtras().getString(BusecretaryActivity.RING);
		if(strUri == null){
			finish();
		}
		mPlayer = MediaPlayer.create(this, Uri.parse(strUri));
		
		try{
			mPlayer.prepare();
		}catch(Exception e){
			e.printStackTrace();
		}
		mPlayer.setLooping(true);
		mPlayer.start();
		
		/*
		 * initialize the ui
		 */
		mTvDesc = (TextView) this.findViewById(R.id.tv_notify_msg);
		mTvDesc.setText(strDesc);
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
			mPlayer.stop();
			finish();
			break;
		case R.id.btn_delay:
			mPlayer.stop();
			finish();
			break;
		}
		
	}
}
