package com.chaos.sleepcry.busecretary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import utils.LOG;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
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
		Button btn = (Button) LayoutInflater.from(context).inflate(
				R.layout.normalbtn, null);
		btn.setText(R.string.more);
		btn.setOnClickListener(this);
		mList.addFooterView(btn);
		mList.setAdapter(mAdapter);

		/*
		 * @}
		 */
		mAnimation = new PaneAnimation(0);
	}

	public void setData(NotificationData data) {
		mAdapter.setData(data);
		mAdapter.notifyDataSetChanged();
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

//	public String getDesc() {
//		Button mBtnDesc = (Button) this.findViewById(OperationAdapter.WHAT);
//		if (mBtnDesc == null) {
//			return null;
//		}
//		return mBtnDesc.getText().toString();
//	}

	private void setWhat(CharSequence desc) {
		Button mBtnDesc = (Button) this.findViewById(OperationAdapter.WHAT);
		if (mBtnDesc != null) {
			mBtnDesc.setText(desc);
		}
	}

	private void setWhere(CharSequence where) {
		Button mBtnWhere = (Button) this.findViewById(OperationAdapter.WHERE);
		if (mBtnWhere != null) {
			mBtnWhere.setText(where);
		}
	}

	public void reset() {
//		setWhat("");
		mAdapter.reset();
		mList.invalidateViews();
	}

	public void notifyUI(NotificationData data) {
		if (data == null) {
			return;
		}
		final String str1 = OperationAdapter.str1;
		final String str2 = OperationAdapter.str2;
		// when
		Button mBtnDateDesc = (Button) mList
				.findViewById(OperationAdapter.WHEN);
		if (mBtnDateDesc != null) {
			mBtnDateDesc.setText(Html.fromHtml(str1
					+ mMainFrm.getString(R.string.when) + str2
					+ data.getWhen().getString()));
		}
		// where
		setWhere(Html.fromHtml(str1 + mMainFrm.getString(R.string.where) + str2
				+ data.getWhere()));
		// what
		Button mBtnDesc = (Button) this.findViewById(OperationAdapter.WHAT);
		if (mBtnDesc != null) {
			setWhat(Html.fromHtml(str1 + mMainFrm.getString(R.string.what)
					+ str2 + data.getWhat()));
		}
		// notification
		Button mBtnRingDesc = (Button) findViewById(OperationAdapter.NOTIFICATION);
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
		// weather?
		Button btnWeatherButton = (Button) findViewById(OperationAdapter.WEATHER);
		if (btnWeatherButton != null) {
			btnWeatherButton.setText(Html.fromHtml(str1
					+ mMainFrm.getString(R.string.weather) + str2
					+ mMainFrm.getString(android.R.string.unknownName)));
		}
		// search?
		Button btnSearch = (Button) findViewById(OperationAdapter.SEARCH);
		if (btnSearch != null) {
			btnSearch.setText(Html.fromHtml(str1
					+ mMainFrm.getString(R.string.search) + str2
					+ data.getWhat()));
		}
		// notification repeat
		Button mBtnRepeatDesc = (Button) findViewById(OperationAdapter.REPEAT);
		if (mBtnRepeatDesc != null) {
			mBtnRepeatDesc.setText("Category:  "
					+ data.getRepeatCategory().getDesc());
		}
		// image
		if (data.getBmp() == null) {
			LOG.D("memory", "bmp path:" + data.getBmpPath());
			String path = data.getBmpPath();
			if (path != null && path.length() > 0) {
				try {
					System.gc();
					File file = new File(data.getBmpPath());
					FileInputStream fis = new FileInputStream(file);
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = false;
					opts.inSampleSize = 4;
					Bitmap bmp = BitmapFactory.decodeStream(fis, null, opts);
					LOG.D("memory", "bitmap bounds:" + opts.outWidth + ","
							+ opts.outHeight);
					MyDrawable mydraw = new MyDrawable(new BitmapDrawable(bmp),
							new RectF(0, 0, 1, 1), 0, mPb);
					data.setBmp(bmp);
					mPb.clear();
					mPb.add(mydraw);
					mPb.invalidate();
					fis.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException ioe) {

				}

			}
		}
	}

	public void collapse() {
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
		if (mPb != null) {
			Mydraw[] draws = mPb.getDrawList();
			if (draws != null && draws.length > 0
					&& draws[0] instanceof MyDrawable) {
				MyDrawable drawable = (MyDrawable) draws[0];
				return drawable.getBmp();
			}
		}
		return null;
	}

}
