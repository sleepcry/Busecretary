package com.chaos.sleepcry.busecretary.append;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.BusecretaryActivity;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.canvasedit.CanvasEditActivity;
import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;
import com.chaos.sleepcry.busecretary.notify.NotificationData;
import com.chaos.sleepcry.busecretary.notify.NotifyDatabase;

public class AppendActivity extends Activity {
	PaintBoard mPb = null;
	static final int VIEW_REQUEST = 0;
	TextView mTitle = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.append);
		mPb = (PaintBoard) findViewById(R.id.canvas);
		mTitle = (TextView) findViewById(R.id.title);
		this.registerForContextMenu(mPb);
	}

	public void ok(View v) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.DATE, 1);
		String desc = mTitle.getText().toString();
		NotificationData data = new NotificationData();
		NotifyDatabase db = new NotifyDatabase(this,BusecretaryActivity.DB_VER);
		db.insert(db.getMaxId()+1, cal.getTimeInMillis(), desc, data.getRing(),
				data.getCategory().getId(),mPb.toBitmap());

		mPb.clear();
		mTitle.setText("");
	}

	public void clear(View v) {
		if (v.getId() == R.id.btn_clear) {
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
			Intent intent = new Intent(this, CanvasEditActivity.class);
			intent.putExtra(PaintBoard.BACKGROUND, mPb.toParcel());
			startActivityForResult(intent,VIEW_REQUEST);
			return true;
		case R.id.menuexit:
			return true;

		}
		return false;
	}
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		if(data != null && requestCode == VIEW_REQUEST && resultCode == RESULT_OK){
			Bundle extras = data.getExtras();
			if (extras != null) {
				MyDrawable mydraw = extras.getParcelable("background");
				mPb.clear();
				mPb.add(mydraw);
			}
		}
	}

}
