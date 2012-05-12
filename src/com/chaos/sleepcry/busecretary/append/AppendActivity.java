package com.chaos.sleepcry.busecretary.append;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.sleepcry.busecretary.BusecretaryActivity;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.canvasedit.CanvasEditActivity;
import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard.PaintBoardListener;
import com.chaos.sleepcry.busecretary.mydraw.ShakeShuffle;
import com.chaos.sleepcry.busecretary.mydraw.ShakeShuffle.ShakeShuffleListener;
import com.chaos.sleepcry.busecretary.notify.NotificationData;
import com.chaos.sleepcry.busecretary.notify.NotifyDatabase;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class AppendActivity extends Activity implements ShakeShuffleListener {
	PaintBoard mPb = null;
	static final int VIEW_REQUEST = 0;
	TextView mTitle = null;
	ShakeShuffle mShakeShuffle;
	public static final String COLOR = "c";
	public static final String LINE_WIDTH = "lw";
	public static final String LINE_STYLE = "ls";
	public static final String SHAREPREF = "com.chaos.sleepcry.busecretary.append.AppendActivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.append);
		mPb = (PaintBoard) findViewById(R.id.canvas);
		mPb.setPBListener(new PaintBoardListener() {

			@Override
			public void zoomIn(float x, float y) {
				ok(null);
			}

			@Override
			public void zoomOut(float x, float y) {
				edit();
			}

			@Override
			public void doubleClick(float x, float y) {
				// TODO Auto-generated method stub

			}

		});
		mTitle = (TextView) findViewById(R.id.title);
		this.registerForContextMenu(mPb);
		mAppendPlayer = MediaPlayer.create(this, R.raw.append);
		mClearPlayer = MediaPlayer.create(this, R.raw.clear);
		mAlertPlayer = MediaPlayer.create(this, R.raw.nomatch);
	}

	private void edit() {
		Intent intent = new Intent(this, CanvasEditActivity.class);
		intent.putExtra(PaintBoard.BACKGROUND, mPb.toParcel());
		startActivityForResult(intent, VIEW_REQUEST);
		this.overridePendingTransition(R.anim.zoom_fade_out,
				R.anim.zoom_fade_in);
		mPb.recycle();
	}
	private MediaPlayer mAppendPlayer,mClearPlayer,mAlertPlayer;
	public void ok(View v) {
		String desc = mTitle.getText().toString();
		if(desc == null || desc.length() == 0) {
			mAlertPlayer.start();
			new AlertDialog.Builder(this)
			.setTitle(android.R.string.untitled)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.titleUnspecified)
			.setPositiveButton(android.R.string.ok, null).show();
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DATE, 1);
		NotificationData data = new NotificationData();
		NotifyDatabase db = new NotifyDatabase(this, BusecretaryActivity.DB_VER);
		mAppendPlayer.start();
		db.insert(db.getMaxId() + 1, cal.getTimeInMillis(), desc,
				data.getRing(), data.getRepeatCategory().getId(), mPb.toBitmap(),null);
		Toast.makeText(this, getString(R.string.autosave), Toast.LENGTH_SHORT)
				.show();
		mTitle.setText("");
	}

	public void clear(View v) {
		if (v.getId() == R.id.btn_clear) {
			mClearPlayer.start();
			mPb.clear();
		}
	}

	public void view(View v) {
		if (v.getId() == R.id.btn_view) {
			Intent intent = new Intent(this, BusecretaryActivity.class);
			startActivity(intent);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();
		this.getMenuInflater().inflate(R.menu.simpledit, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.menuredo).setEnabled(mPb.canRedo());
		menu.findItem(R.id.menuundo).setEnabled(mPb.canUndo());
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int id = item.getItemId();
		switch (id) {
		case R.id.menuredo:
			mPb.redo();
			return true;
		case R.id.menuundo:
			mPb.undo();
			return true;
		case R.id.menuedit:
			edit();
			return true;
		}
		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && requestCode == VIEW_REQUEST
				&& resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				MyDrawable mydraw = extras.getParcelable(PaintBoard.BACKGROUND);
				mPb.clear();
				mPb.add(mydraw);
			}
		}
	}

	protected void loadPrefs() {
		SharedPreferences prefs = this.getSharedPreferences(SHAREPREF, 0);
		if (prefs.contains(COLOR)) {
			int color = prefs.getInt(COLOR, Color.WHITE);
			mPb.setColor(color);
		}
		if (prefs.contains(LINE_WIDTH)) {
			int width = prefs.getInt(LINE_WIDTH, 3);
			if (width <= 0) {
				width = 1;
			}
			mPb.setLineWidth(width);
		}
		if (prefs.contains(AppendActivity.LINE_STYLE)) {
			int style = prefs.getInt(AppendActivity.LINE_STYLE, 0);
			mPb.setPaint(style);
		}
	}

	public void onResume() {
		super.onResume();
		loadPrefs();
		mShakeShuffle = new ShakeShuffle(this);
		mShakeShuffle.setShakeShuffleListener(this);
		mShakeShuffle.start();
	}

	public void onPause() {
		mShakeShuffle.pause();
		super.onPause();
	}

	@Override
	public void onShakeLeft() {
		mPb.post(new Runnable() {

			@Override
			public void run() {
				mPb.undo();
			}
		});
	}

	@Override
	public void onShakeRight() {
		mPb.post(new Runnable() {
			
			@Override
			public void run() {
				mPb.redo();
			}
		});
	}

}
