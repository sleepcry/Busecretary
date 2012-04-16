package com.chaos.sleepcry.busecretary.canvasedit;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.chaos.sleepcry.busecretary.BusecretaryActivity;
import com.chaos.sleepcry.busecretary.PaneAnimation;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.colorpalette.ColorPalette;
import com.chaos.sleepcry.busecretary.colorpalette.ColorPalette.OnColorChangedListener;
import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.MyPolyLine;
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
	ColorPalette mColorPal = null;
	LineChooseView mLine = null;
	SeekBar mSeekBar = null;
	TextView mTempView = null;
	EditText mTempText = null;
	Button mLoad, mText;
	ContentStatus mStatus;
	Bitmap mTempBmp = null;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTheme(android.R.style.Theme_Light_NoTitleBar);
		mBaseView = new LinearLayout(this);
		LayoutInflater.from(this).inflate(R.layout.canvas, mBaseView);
		this.setContentView(mBaseView);
		mPb = (PaintBoard) findViewById(R.id.surfaceView1);
		mColorPal = (ColorPalette) findViewById(R.id.cp);
		mLine = (LineChooseView) findViewById(R.id.lcv);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar1);
		mSeekBar.setVisibility(View.GONE);
		mTempView = (TextView) findViewById(R.id.tvtemp);
		mStatus = new ContentStatus();
		mTempView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mStatus.beginPutContent();
			}

		});
		mTempText = (EditText) findViewById(R.id.ettemp);
		mTempText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int id, KeyEvent event) {
				updateContent(mTempText.getText().toString());
				mTempText.setVisibility(View.GONE);
				mTempView.setVisibility(View.VISIBLE);
				return true;
			}

		});
		mTempView.setVisibility(View.GONE);
		mLoad = (Button) findViewById(R.id.load);
		mText = (Button) findViewById(R.id.text);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar sb, int progress,
					boolean fromUser) {
				if (fromUser) {
					mLine.setLineWidth(progress);
					mPb.setLineWidth(progress);
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar sb) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar sb) {
				mSeekBar.setVisibility(View.GONE);
			}

		});
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				MyDrawable mydraw = extras.getParcelable(PaintBoard.BACKGROUND);
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
		mList.setBackgroundColor(0x7f7fff7f);
		mList.setAdapter(new ArrayAdapter<Mydraw>(this,
				android.R.layout.simple_list_item_checked, mPb.getDrawList()));
		mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		for (int i = 0; i < mList.getCount(); i++) {
			mList.setItemChecked(i,
					((Mydraw) mList.getItemAtPosition(i)).isVisible());
		}
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mPb.changeVisibility(position);
				// mList.setItemChecked(position,!mList.isItemChecked(position));
				mList.invalidateViews();
			}

		});

		mAnim.addY(-mHeight);
		mAnim.setAnimationListener(listener2);
		mList.startAnimation(mAnim);
		mList.setOnTouchListener(this);
		mPosDown = new Point(-1, -1);
		mColorPal.setOnColorChangedListener(new OnColorChangedListener() {

			@Override
			public void onColorChange(int color) {
				mPb.setColor(color);
				mLine.setColor(color);
			}

		});
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

	public void load(View v) {
		AlertDialog dlg = new AlertDialog.Builder(this).setItems(
				R.array.loadsource, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						switch (which) {
						case 0:
							// load from contact
							intent.setAction(Intent.ACTION_PICK);
							intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
							break;
						case 1:
							// load from camera
							intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							break;
						case 2:
							// load from image
							intent.setAction(Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType("image/*");
							break;
						}
						dialog.dismiss();
						startActivityForResult(intent, which);

					}
				}).create();
		dlg.show();
	}

	public void addText(View v) {
		if (mStatus.isNormalDrawing()) {
			mTempText.setVisibility(View.VISIBLE);
			mTempView.setVisibility(View.GONE);
			mTempText.requestFocus();
		} else {
			mStatus.endPutContent();
		}
	}

	public void changeLine(View v) {
		mSeekBar.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onTouch(View v, MotionEvent motion) {
		int action = motion.getAction();
		if (mList.getVisibility() == View.GONE) {
			boolean ret = mBaseView.dispatchTouchEvent(motion);
			if (!mStatus.isNormalDrawing() && !ret) {
				MyDrawable draw = null;
				Rect rect = null;
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mPosDown.set((int) motion.getX(), (int) motion.getY());
					rect = new Rect(mPosDown.x, mPosDown.y, mPosDown.x + 1,
							mPosDown.y + 1);
					draw = new MyDrawable(new BitmapDrawable(mTempBmp),
							getRelativeRect(rect), 1);
					mPb.undo();
					mPb.add(draw);
					break;
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_UP:
					rect = new Rect(Math.min(mPosDown.x, (int) motion.getX()),
							Math.min(mPosDown.y, (int) motion.getY()),
							Math.max(mPosDown.x, (int) motion.getX()),
							Math.max(mPosDown.y, (int) motion.getY()));
					RectF rectf = getRelativeRect(rect);
					draw = new MyDrawable(new BitmapDrawable(mTempBmp), rectf,
							1);
					mPb.undo();
					mPb.add(draw);
					break;
				}
			}
		} else {
			mList.onTouchEvent(motion);
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
		}
		return true;
	}

	protected RectF getRelativeRect(Rect rect) {
		RectF baserect = new RectF(mPb.getLeft(), mPb.getTop(), mPb.getRight(),
				mPb.getBottom());
		return new RectF((rect.left - baserect.left) / baserect.width(),
				(rect.top - baserect.top) / baserect.height(),
				(rect.right - baserect.left) / baserect.width(),
				(rect.bottom - baserect.top) / baserect.height());
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();
		this.getMenuInflater().inflate(R.menu.totaledit, menu);
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
		case R.id.menuclear:
			mPb.clear();
			return true;
		case R.id.menushare:
			return true;
		case R.id.menuelements:
			mAnim.addY(mHeight);
			mAnim.setAnimationListener(listener1);
			mList.setAdapter(new ArrayAdapter<Mydraw>(this,
					android.R.layout.simple_list_item_multiple_choice, mPb
							.getDrawList()));
			for (int i = 0; i < mList.getCount(); i++) {
				mList.setItemChecked(i,
						((Mydraw) mList.getItemAtPosition(i)).isVisible());
			}
			mList.invalidateViews();
			mList.startAnimation(mAnim);
			return true;

		}
		return false;
	}

	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra(PaintBoard.BACKGROUND, mPb.toParcel());
		setResult(RESULT_OK, intent);
		finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			Bitmap bitmap = null;
			switch (requestCode) {
			case 0:
				// return from contact
				// original
				String name = null;
				ContentResolver cr = getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);

				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					name = cursor
							.getString(cursor
									.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
					int contactid = cursor.getInt(cursor
							.getColumnIndex(ContactsContract.Data.CONTACT_ID));
					Uri contactUri = ContentUris.withAppendedId(
							ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts
							.openContactPhotoInputStream(cr, contactUri);
					if (input != null) {
						bitmap = BitmapFactory.decodeStream(input);
						break;
					}
					try {
						int photoid = cursor
								.getInt(cursor
										.getColumnIndex(ContactsContract.Data.PHOTO_ID));
						Uri photoUri = ContentUris.withAppendedId(
								ContactsContract.Data.CONTENT_URI, photoid);
						Cursor c = cr.query(photoUri, null, null, null, null);
						if (c != null && c.getCount() > 0) {
							byte[] bmpdata = c
									.getBlob(c
											.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
							bitmap = BitmapFactory
									.decodeStream(new ByteArrayInputStream(
											bmpdata));
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
						break;
					} finally {
						cursor.close();
					}
					updateContent(name);
				}
				break;
			case 1:
				// return from camera
			case 2:
				// return from image picker
				AssetFileDescriptor afd;
				try {
					afd = getContentResolver()
							.openAssetFileDescriptor(uri, "r");
					bitmap = BitmapFactory
							.decodeStream(afd.createInputStream());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			updateContent(bitmap);
		}
	}

	public void updateContent(Bitmap bitmap) {
		if (bitmap != null) {
			Drawable draw = mTempView.getBackground();
			if(draw != null && draw instanceof BitmapDrawable){
				Bitmap bmp = ((BitmapDrawable)draw).getBitmap();
				if(bmp != null){
					bmp.recycle();
				}
			}
			mTempView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			mTempView.setVisibility(View.VISIBLE);
			mTempView.setText("");
		}
	}

	public void updateContent(String name) {
		if (name != null) {
			mTempView.setText(name);
			mTempView.setTextColor(Color.BLACK);
			mTempView.setVisibility(View.VISIBLE);
			mTempView.setBackgroundColor(0x00ffffff);
		}

	}

	private class ContentStatus {
		private int flags;
		private static final int PUT_IMAGE = 1;
		private static final int NORMAL_DRAWING = 2;

		public ContentStatus() {
			flags = NORMAL_DRAWING;
		}

		public boolean isNormalDrawing() {
			return flags == NORMAL_DRAWING;
		}

		public void beginPutContent() {
			if (isNormalDrawing()) {
				flags = PUT_IMAGE;
				mLoad.setVisibility(View.GONE);
				mText.setText(R.string.ok);
				mTempBmp = Bitmap.createBitmap(mTempView.getWidth(),
						mTempView.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(mTempBmp);
				mTempView.draw(canvas);
				mPb.setEditable(false);
				mPb.add(new MyPolyLine());
			}
		}

		public void endPutContent() {
			flags = NORMAL_DRAWING;
			mLoad.setVisibility(View.VISIBLE);
			mText.setText(R.string.text);
			mTempView.setVisibility(View.GONE);
			mTempText.setVisibility(View.GONE);
			mPb.setEditable(true);
		}
	}
}
