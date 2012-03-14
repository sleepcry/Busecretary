package com.chaos.sleepcry.busecretary.canvasedit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.chaos.sleepcry.busecretary.BusecretaryActivity;
import com.chaos.sleepcry.busecretary.PaneAnimation;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.Mydraw;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;

public class CanvasEditActivity extends Activity implements OnTouchListener {
	PaintBoard mPb = null;
	ListView mList = null;
	PaneAnimation mAnim = null;
	int mHeight;
	// the point when pressed
	private Point mPosDown = null;
	long mDownTime;
	LayoutParams mParams = null;
	LinearLayout mBaseView = null;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTheme(android.R.style.Theme_Light_NoTitleBar);
		mBaseView = new LinearLayout(this);
		LayoutInflater.from(this).inflate(R.layout.canvas, mBaseView);
		this.setContentView(mBaseView);
		mPb = (PaintBoard) findViewById(R.id.surfaceView1);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				MyDrawable mydraw = extras.getParcelable("background");
				mPb.add(mydraw);
			}
		}
		mList = new ListView(this);
		mHeight = this.getWindowManager().getDefaultDisplay().getHeight();
		mAnim = new PaneAnimation(0, 0, 0, 500);
		mAnim.setInterpolator(new AccelerateInterpolator());
		mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		this.addContentView(mList, mParams);
		mList.setBackgroundColor(0x2f7fff7f);
		mList.setAdapter(new ArrayAdapter<Mydraw>(this,
				android.R.layout.simple_list_item_multiple_choice, mPb
						.getDrawList()));
		mAnim.addY(-mHeight);
		mAnim.setAnimationListener(listener2);
		mList.startAnimation(mAnim);
		mList.setOnTouchListener(this);
		mPosDown = new Point(-1, -1);
	}

	private AnimationListener listener1 = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mList.setVisibility(View.VISIBLE);
			mList.setOnTouchListener(CanvasEditActivity.this);
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			mList.setOnTouchListener(null);
		}

	};
	private AnimationListener listener2 = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mList.setVisibility(View.GONE);
			mList.setOnTouchListener(CanvasEditActivity.this);
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			mList.setOnTouchListener(null);
		}

	};

	public void viewElements(View v) {
		mAnim.addY(mHeight);
		mAnim.setAnimationListener(listener1);
		mList.setAdapter(new ArrayAdapter<Mydraw>(this,
				android.R.layout.simple_list_item_multiple_choice, mPb
						.getDrawList()));
		mList.invalidateViews();
		mList.startAnimation(mAnim);
	}

	public void redo(View v) {
		mPb.redo();
	}

	public void undo(View v) {
		mPb.undo();
	}

	@Override
	public boolean onTouch(View v, MotionEvent motion) {
		if (mList.getVisibility() == View.GONE) {
			mBaseView.dispatchTouchEvent(motion);
		} else {
			mList.onTouchEvent(motion);
			int action = motion.getAction();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mPosDown.set((int) motion.getX(), (int) motion.getY());
				break;

			case MotionEvent.ACTION_UP:
				if (motion.getEventTime() - motion.getDownTime() <= 200) {
					Point p = new Point((int) motion.getX(),
							(int) motion.getY());
					double orientation = Math.abs(BusecretaryActivity
							.getOrientation(mPosDown, p));
					int half = mHeight / 2;
					if (orientation >= Math.PI / 4 && p.y > half
							&& mPosDown.y < half) {
						mAnim.addY(-mHeight);
						mAnim.setAnimationListener(listener2);
						mList.startAnimation(mAnim);
					}
				}
				mPosDown.set(-1, -1);
				break;
			}
			//mList.invalidateViews();
		}
		return true;
	}
}
