package com.chaos.sleepcry.busecretary;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.Mydraw;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;
import com.chaos.sleepcry.busecretary.notify.NotificationData;

public class MainView extends LinearLayout implements OnClickListener {
	/*
	 * @{ define the UI elements
	 */
	private ListView mList = null;
	PaintBoard mPb = null;
	/*
	 * @}
	 */
	private PaneAnimation mAnimation = null;
	private BusecretaryActivity mMainFrm = null;
	private OperationAdapter mAdapter = null;

	public MainView(BusecretaryActivity context) {
		super(context);
		mMainFrm = context;
		LayoutInflater.from(context).inflate(R.layout.mainview, this, true);
		/*
		 * @{ initialize all view components
		 */
		mList = (ListView) findViewById(R.id.listView1);
		mPb = (PaintBoard) findViewById(R.id.mainsurface);
		mPb.setEditable(false);
		mAdapter = new OperationAdapter(context);
		Button btn = (Button) LayoutInflater.from(context).inflate(R.layout.normalbtn, null);
		btn.setText("more...");
		btn.setOnClickListener(this);
		mList.addFooterView(btn);
		mList.setAdapter(mAdapter);

		/*
		 * @}
		 */
		mAnimation = new PaneAnimation(0);
	}
	public void setData(NotificationData data){
		mAdapter.setData(data);
		mList.invalidateViews();
	}
	public PaintBoard getPaintBoard() {
		return mPb;
	}

	public void translate(int x) {
		mAnimation.addX(x);
		// for(int i=0;i<mComponent.size();i++){
		// mComponent.get(i).startAnimation(mAnimation);
		// }
		startAnimation(mAnimation);
	}

	public void translate(int x, long duration) {
		PaneAnimation anim = new PaneAnimation(mAnimation.getX(), 0, 0,
				duration);
		mAnimation.addX(x);
		anim.addX(x);
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setDuration(duration);
		startAnimation(anim);
	}

	public String getDesc() {
		Button mBtnDesc = (Button) this.findViewById(OperationAdapter.DESC);
		if (mBtnDesc == null) {
			return null;
		}
		return mBtnDesc.getText().toString();
	}

	public void setDesc(String desc) {
		Button mBtnDesc = (Button) this.findViewById(OperationAdapter.DESC);
		if (mBtnDesc != null) {
			mBtnDesc.setText(desc);
		}
	}

	public void reset() {
		setDesc("");
		mAdapter.reset();
		mList.invalidateViews();
	}

	public void notifyUI(NotificationData data) {
		if (data == null) {
			return;
		}
		Button mBtnDateDesc = (Button) mList.findViewById(OperationAdapter.WHEN);
		Button mBtnRingDesc = (Button) this.findViewById(OperationAdapter.RING);
		Button mBtnRepeatDesc = (Button) this
				.findViewById(OperationAdapter.REPEAT);
		Button mBtnDesc = (Button) this.findViewById(OperationAdapter.DESC);
		if (mBtnDateDesc != null) {
			mBtnDateDesc.setText(data.getDay().getString());
		}
		// mBtnTimeDesc.setText(data.getDay().getTimeString());
		if (mBtnRingDesc != null) {
			if (data.getRing() != null) {
				Uri ring = Uri.parse(data.getRing());
				Cursor cursor = mMainFrm.getContentResolver().query(ring,
						new String[] { MediaStore.Audio.Media.TITLE }, null,
						null, null);
				cursor.moveToFirst();
				if (!cursor.isNull(0)) {
					mBtnRingDesc.setText("NotifyRing:" + cursor.getString(0));
					Log.d("ui", mBtnRingDesc.getText().toString());
				}
			} else {
				mBtnRingDesc.setText("choose a ring here...");
			}
		}
		if (mBtnDesc != null) {
			String text = getDesc();
			if (text.equals("")) {
				text = data.getDesc();
			}
			setDesc(text);
		}
		if (mBtnRepeatDesc != null) {
			mBtnRepeatDesc
					.setText("Category:  " + data.getCategory().getDesc());
		}
		if (data.getBmp() == null) {
			MyDrawable mydraw = new MyDrawable(new BitmapDrawable(
					data.getBmpPath()), new RectF(0, 0, 1, 1), 0);
			data.setBmp(mydraw.getBmp());
			mPb.permenantClear();
			mPb.add(mydraw);
			mPb.invalidate();
		}
	}
	public void collapse(){
		mAdapter.collapse();
		mList.invalidateViews();
	}
	@Override
	public void onClick(View v) {
		if (v instanceof Button) {
			Button btn = (Button) v;
			String content = btn.getText().toString();
			if (content.equals("more...")) {
				mAdapter.fetchmore();
				mList.invalidateViews();
			}
		}

	}

	public void onMeasure(int width, int height) {
		super.onMeasure(width, height);
		int h = View.MeasureSpec.getSize(height);
		int minHeight = h / 2;
		if (mPb.getMeasuredHeight() < minHeight) {
			mPb.measure(width, View.MeasureSpec.makeMeasureSpec(
					View.MeasureSpec.AT_MOST, minHeight));
			mList.measure(width, View.MeasureSpec.makeMeasureSpec(
					View.MeasureSpec.AT_MOST, h - minHeight));
		}
	}

	public void resume() {
	}

	public void pause() {
	}
	public Bitmap getBmp() {
		if(mPb != null){
			Mydraw[] draws= mPb.getDrawList();
			if(draws != null && draws.length > 0 && draws[0] instanceof MyDrawable) {
				MyDrawable drawable =(MyDrawable)draws[0]; 
				return drawable.getBmp();
			}
		}
		return null;
	}

}
