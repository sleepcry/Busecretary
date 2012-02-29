package com.chaos.sleepcry;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainView extends LinearLayout {
	/*
	 * @{ define the UI elements
	 */
	private Button mBtnDateDesc = null; // describe the date
	private Button mBtnTimeDesc = null; // describe the time on the day
	private Button mBtnRingDesc = null; // describe the notification ring
	private Button mBtnRepeatDesc = null; // describe the repeat pattern
	private EditText mEtDesc = null; // describe the notification content
	/*
	 * @}
	 */
	private PaneAnimation mAnimation = null;
	private BusecretaryActivity mMainFrm = null;

	public MainView(BusecretaryActivity context) {
		super(context);
		mMainFrm = context;
		LayoutInflater.from(context).inflate(R.layout.mainview, this, true);
		/*
		 * @{ initialize all view components
		 */
		mBtnDateDesc = (Button) this.findViewById(R.id.btn_date_desc);
		mBtnDateDesc.setOnClickListener(context);
		mBtnTimeDesc = (Button) this.findViewById(R.id.btn_time_desc);
		mBtnTimeDesc.setOnClickListener(context);
		mBtnRingDesc = (Button) this.findViewById(R.id.btn_ring_desc);
		mBtnRingDesc.setOnClickListener(context);
		mBtnRepeatDesc = (Button) this.findViewById(R.id.btn_repeat_desc);
		mBtnRepeatDesc.setOnClickListener(context);
		mEtDesc = (EditText) this.findViewById(R.id.et_desc);
		/*
		 * @}
		 */
		mAnimation = new PaneAnimation(0);
	}

	public void translate(int x) {
		mAnimation.addX(x);
		// for(int i=0;i<mComponent.size();i++){
		// mComponent.get(i).startAnimation(mAnimation);
		// }
		startAnimation(mAnimation);
	}

	public void translate(int x, long duration) {
		mAnimation.addX(x);
		PaneAnimation anim = new PaneAnimation(mAnimation.getX(), 0, 0,
				duration);
		anim.setInterpolator(new BounceInterpolator());
		anim.setDuration(duration);
		startAnimation(anim);
	}

	public String getDesc() {
		return mEtDesc.getText().toString();
	}

	public void setDesc(String desc) {
		mEtDesc.setText(desc);
	}

	public void notifyUI(NotificationData data) {
		if (data == null) {
			return;
		}
		mBtnDateDesc.setText(data.getDay().getDateString());
		mBtnTimeDesc.setText(data.getDay().getTimeString());
		if (data.getRing() != null) {
			Uri ring = Uri.parse(data.getRing());
			Cursor cursor = mMainFrm.getContentResolver().query(ring,
					new String[] { MediaStore.Audio.Media.TITLE }, null, null,
					null);
			cursor.moveToFirst();
			if (!cursor.isNull(0)) {
				mBtnRingDesc.setText("NotifyRing:" + cursor.getString(0));
				Log.d("ui",mBtnRingDesc.getText().toString());
			}
		} else {
			mBtnRingDesc.setText("choose a ring here...");
		}
		mBtnRepeatDesc.setText("Category:  " + data.getCategory().getDesc());
	}

}
