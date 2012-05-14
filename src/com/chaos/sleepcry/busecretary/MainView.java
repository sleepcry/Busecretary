package com.chaos.sleepcry.busecretary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.Mydraw;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;
import com.chaos.sleepcry.busecretary.notify.NotificationData;
import com.chaos.sleepcry.busecretary.utils.LOG;
import com.chaos.sleepcry.busecretary.utils.SmartMediaPlayer;
import com.chaos.sleepcry.busecretary.utils.TimeUtils;

public class MainView extends LinearLayout {
	/*
	 * @{ define the UI elements
	 */
	private ListView mList = null;
	PaintBoard mPb = null;
	View mEmptyView = null;
	TimeUtils mMyUtils = null;
	/*
	 * @}
	 */
	private PaneAnimation mAnimation = null;
	private BusecretaryActivity mMainFrm = null;
	private OperationAdapter mAdapter = null;
	private Button mMore;

	public MainView(BusecretaryActivity context) {
		super(context);
		mMainFrm = context;
		mMyUtils = new TimeUtils(context);
		LayoutInflater.from(context).inflate(R.layout.mainview, this, true);
		/*
		 * @{ initialize all view components
		 */
		mList = (ListView) findViewById(R.id.listView1);
		mPb = (PaintBoard) findViewById(R.id.mainsurface);
		mEmptyView = findViewById(R.id.empty);
		mPb.setEditable(false);
		mAdapter = new OperationAdapter(context);
		mMore = (Button) LayoutInflater.from(context).inflate(
				R.layout.normalbtn, null);
		mMore.setText(R.string.more);
		mMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapter.fetchmore()) {
					mList.invalidateViews();
					mExpandPlayer.start();
					if (!mAdapter.hasMore()) {
						mList.removeFooterView(mMore);
					}
				}
			}
		});
		mList.addFooterView(mMore);
		mList.setAdapter(mAdapter);

		/*
		 * @}
		 */
		mAnimation = new PaneAnimation(0);
		mCollapsePlayer = SmartMediaPlayer.create(context, R.raw.waterdrop);
		mExpandPlayer = SmartMediaPlayer.create(context, R.raw.waterdrop);
		mDockPlayer = SmartMediaPlayer.create(context, R.raw.dock);
	}

	public void setData(NotificationData data) {
		if (null == data) {
			mList.setVisibility(View.GONE);
			mPb.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
		} else {
			mList.setVisibility(View.VISIBLE);
			mPb.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
			mAdapter.setData(data);
		}
	}
	public NotificationData getData() {
		return mAdapter.getData();
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

	AnimationListener mCommitListener = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation animation) {
			mDockPlayer.start();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub

		}

	};

	public void translate(int x, long duration, boolean effect) {
		PaneAnimation anim = new PaneAnimation(mAnimation.getX(), 0, 0,
				duration);
		mAnimation.addX(x);
		anim.addX(x);
		if (effect) {
			anim.setAnimationListener(mCommitListener);
		}
		anim.setInterpolator(new DecelerateInterpolator());
		anim.setDuration(duration);
		startAnimation(anim);
	}

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
		mAdapter.reset();
		mList.invalidateViews();
		mList.removeFooterView(mMore);
		mList.addFooterView(mMore);
	}
	public void destroy() {
		mList.setAdapter(null);
		mAdapter.reset();
		mPb.destroy();
	}
	public String getDescription(NotificationData data) {
		String ret = new TimeUtils(mMainFrm).generalString(data.getWhen());
		if(data.getWhere() != null) {
			ret += "\n" + data.getWhere();
		}
		if(data.getWhat() != null) {
			ret += "\n" + data.getWhat();
		}
		return ret;
	}
	public void notifyUI(NotificationData data) {
		setData(data);
		if (data == null) {
			return;
		}
		LOG.D("NotificationData",data.toString());
		final String str1 = OperationAdapter.str1;
		final String str2 = OperationAdapter.str2;
		// when
		Button mBtnDateDesc = (Button) mList
				.findViewById(OperationAdapter.WHEN);
		if (mBtnDateDesc != null) {
			mBtnDateDesc.setText(Html.fromHtml(str1
					+ mMainFrm.getString(R.string.when) + str2
					+ mMyUtils.timeString(data.getWhen())));
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
			mPb.clear();
			LOG.D("memory", "bmp path:" + data.getBmpPath());
			String path = data.getBmpPath();
			if (path != null && path.length() > 0) {
				try {
					System.gc();
					File file = new File(data.getBmpPath());
					FileInputStream fis = new FileInputStream(file);
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = false;
//					opts.inSampleSize = 4;
					Bitmap bmp = BitmapFactory.decodeStream(fis, null, opts);
					LOG.D("memory", "bitmap bounds:" + opts.outWidth + ","
							+ opts.outHeight);
					MyDrawable mydraw = new MyDrawable(new BitmapDrawable(bmp),
							new RectF(0, 0, 1, 1), 0, mPb);
					data.setBmp(bmp);
					mPb.add(mydraw);
					fis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}else {
				mPb.invalidateAll();
			}
		}
	}

	private SmartMediaPlayer mCollapsePlayer, mExpandPlayer, mDockPlayer;

	public void collapse() {
		if (mAdapter.collapse()) {
			mList.invalidateViews();
			mCollapsePlayer.start();
			mList.removeFooterView(mMore);	
			if (mAdapter.hasMore()) {
				mList.addFooterView(mMore);
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
