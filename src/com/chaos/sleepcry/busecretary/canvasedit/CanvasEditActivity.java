package com.chaos.sleepcry.busecretary.canvasedit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import utils.LOG;
import utils.MathUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.PaneAnimation;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.append.AppendActivity;
import com.chaos.sleepcry.busecretary.colorpalette.ColorItem;
import com.chaos.sleepcry.busecretary.colorpalette.ColorPalette;
import com.chaos.sleepcry.busecretary.colorpalette.ColorPalette.ColorProvider;
import com.chaos.sleepcry.busecretary.colorpalette.ColorPalette.OnColorChangedListener;
import com.chaos.sleepcry.busecretary.colorpalette.ColorPickerDialog;
import com.chaos.sleepcry.busecretary.mydraw.MyDrawable;
import com.chaos.sleepcry.busecretary.mydraw.MyText;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard;
import com.chaos.sleepcry.busecretary.mydraw.PaintBoard.PaintBoardListener;
import com.chaos.sleepcry.busecretary.mydraw.ShakeShuffle;
import com.chaos.sleepcry.busecretary.mydraw.ShakeShuffle.ShakeShuffleListener;

public class CanvasEditActivity extends Activity implements OnTouchListener,
		ColorPickerDialog.OnColorChangedListener, ColorProvider,
		ShakeShuffleListener {
	PaintBoard mPb = null;
	ListView mList = null;
	boolean mbAnimating = false;
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
	Button mLoad;
	ContentStatus mStatus;
	ShakeShuffle mShakeShuffle;
	Bitmap mTempBmp = null;
	ImageView mStatusImage = null;
	int mTextSize = 18;
	float density;

	public static final int GET_IMAGE = 0;
	public static final int GET_CAMERA = 1;
	public static final int GET_CONTACT = 2;
	public static final int SETTINGS = 3;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setTheme(android.R.style.Theme_Light_NoTitleBar);
		mBaseView = new LinearLayout(this);
		LayoutInflater.from(this).inflate(R.layout.canvas, mBaseView);
		this.setContentView(mBaseView);
		mPb = (PaintBoard) findViewById(R.id.surfaceView1);
		mStatusImage = (ImageView) findViewById(R.id.status);
		mPb.setPBListener(new PaintBoardListener() {

			@Override
			public void zoomIn(float x, float y) {
				if (mStatus.isNormalDrawing()) {
					complete();
				} else {
				}
			}

			@Override
			public void zoomOut(float x, float y) {
				showElem();
			}

			@Override
			public void doubleClick(float x, float y) {
				// TODO Auto-generated method stub

			}

		});
		mColorPal = (ColorPalette) findViewById(R.id.cp);
		mLine = (LineChooseView) findViewById(R.id.lcv);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar1);
		mSeekBar.setVisibility(View.GONE);
		mTempView = (TextView) findViewById(R.id.tvtemp);
		mStatus = new ContentStatus();
		mTempView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (mStatus.flags == ContentStatus.PUT_IMAGE) {
					mStatus.beginPutImage();
				} else if (mStatus.flags == ContentStatus.PUT_TEXT) {
					mStatus.beginPutText();
				}
			}

		});
		mTempText = (EditText) findViewById(R.id.ettemp);

		mTempView.setVisibility(View.GONE);
		mLoad = (Button) findViewById(R.id.load);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar sb, int progress,
					boolean fromUser) {
				if (fromUser) {
					if (progress <= 0) {
						progress = 1;
					}
					mLine.setLineWidth(progress);
					mPb.setLineWidth(progress);
					mTextSize = (int) (Math.sqrt(progress) * density * 5);
					mTempText.setTextSize(mTextSize);
					mTempView.setTextSize(mTextSize);
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
		mList.setAdapter(new DrawElemAdapter(this, mPb.getDrawList()));
		mList.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		mAnim.addY(-mHeight);
		mAnim.setAnimationListener(listener2);
		mList.startAnimation(mAnim);
		mList.setOnTouchListener(this);
		mPosDown = new Point(-1, -1);
		mColorPal.setOnColorChangedListener(new OnColorChangedListener() {

			@Override
			public void onColorChange(int color) {
				colorChanged(color);
			}

		});

		mList.setCacheColorHint(0);
		DisplayMetrics outMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		density = outMetrics.density;
		mLoadPlayer = MediaPlayer.create(this, R.raw.load);
	}

	private AnimationListener listener1 = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mbAnimating = false;
			mList.setVisibility(View.VISIBLE);
			mList.setOnTouchListener(CanvasEditActivity.this);
			mList.setClickable(true);
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			mList.setOnTouchListener(null);
			mbAnimating = true;
		}

	};
	private AnimationListener listener2 = new AnimationListener() {

		@Override
		public void onAnimationEnd(Animation arg0) {
			mList.setVisibility(View.GONE);
			mList.setAdapter(null);
			mList.setOnTouchListener(CanvasEditActivity.this);
			mbAnimating = false;
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			mList.setOnTouchListener(null);
			mbAnimating = true;
		}

	};
	private MediaPlayer mLoadPlayer;

	public void load(View v) {
		if (!mStatus.isNormalDrawing()) {
			mStatus.endPutContent();
		} else {
			mLoadPlayer.start();
			new AlertDialog.Builder(this)
					.setItems(R.array.loadsource,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent();
									switch (which) {
									case 0:
										// load from contact
										intent.setAction(Intent.ACTION_PICK);
										intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
										startActivityForResult(intent,
												GET_CONTACT);
										break;
									case 1:
										// load from camera
										intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
										startActivityForResult(intent,
												GET_CAMERA);
										break;
									case 2:
										// load from image
										intent.setAction(Intent.ACTION_GET_CONTENT);
										intent.addCategory(Intent.CATEGORY_DEFAULT);
										intent.addCategory(Intent.CATEGORY_OPENABLE);
										intent.setType("image/*");
										startActivityForResult(intent,
												GET_IMAGE);
										break;
									case 3:
										// edit text
										mTempText.setVisibility(View.VISIBLE);
										mTempView.setVisibility(View.GONE);
										mTempText.bringToFront();
										mTempText.requestFocus();
										mTempText.setText(null);
										InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

										imm.showSoftInput(
												mTempText,
												InputMethodManager.SHOW_IMPLICIT);
										mPb.setEditable(false);
										mStatusImage
												.setImageResource(R.drawable.pencil);
										mStatus.flags = ContentStatus.PUT_TEXT;
										break;
									}
									dialog.dismiss();

								}
							}).setTitle(R.string.loadtitle)
					.setIcon(android.R.drawable.ic_input_get).show();
		}
	}

	public void changeLine(View v) {
		mSeekBar.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onTouch(View v, MotionEvent motion) {
		if (mList.getVisibility() == View.GONE) {
			return assignEvent2Control(motion);
		} else {
			return assignEvent2List(motion);
		}
	}

	/*
	 * assign the motion event to overlay to process overlay is a image loaded
	 * or a text just edit this function will transport the data in edit text to
	 * text view which will be used to append a new element to the paint board
	 */
	protected boolean assign2Overlay(MotionEvent motion) {
		if (mTempText.getVisibility() == View.VISIBLE) {
			mTempText.setVisibility(View.GONE);
			mTempView.setVisibility(View.VISIBLE);
			mTempView.setText(mTempText.getText());
			mTempView.setBackgroundResource(R.drawable.transluent);
			return true;
		}
		return false;
	}

	/*
	 * move the image to put onto the paint board this must be called in the
	 * ContentStatus.PUT_IMAGE state
	 */
	MyDrawable mCurrentDrawable;

	protected boolean putImage(MotionEvent motion) {
		int action = motion.getAction();
		if (mCurrentDrawable == null) {
			return false;
		}
		Rect rect = null;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mPosDown.set((int) motion.getX(), (int) motion.getY());
			rect = new Rect(mPosDown.x, mPosDown.y, mPosDown.x + 1,
					mPosDown.y + 1);
			mCurrentDrawable.setBounds(getRelativeRect(rect));
			mPb.clearTemp();
			mPb.drawTemp();
			break;
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			rect = new Rect(Math.min(mPosDown.x, (int) motion.getX()),
					Math.min(mPosDown.y, (int) motion.getY()), Math.max(
							mPosDown.x, (int) motion.getX()), Math.max(
							mPosDown.y, (int) motion.getY()));
			mCurrentDrawable.setBounds(getRelativeRect(rect));
			mPb.drawTemp();
			break;
		}
		return true;
	}

	/*
	 * move the text to put onto the paint board this must be called in the
	 * ContentStatus.PUT_TEXT state
	 */
	MyText mCurrentText;
	ArrayList<PointF> mMoveTrack = new ArrayList<PointF>();

	protected boolean putText(MotionEvent motion) {
		if (mCurrentText == null) {
			return false;
		}
		int action = motion.getAction();
		float x = motion.getX();
		float y = motion.getY();
		if (action == MotionEvent.ACTION_DOWN) {
			mPb.clearTemp();
			mMoveTrack.clear();
		}
		mMoveTrack.add(new PointF((x - mPb.getLeft()) / mPb.getWidth(),
				(y - mPb.getTop()) / mPb.getHeight()));
		PointF[] pts = new PointF[mMoveTrack.size()];
		mMoveTrack.toArray(pts);
		mCurrentText.setPath(pts);
		mPb.drawTemp();
		return true;
	}

	/*
	 * change the layer of the selected element on paint board this should be
	 * called only if there is an element selected
	 */
	protected boolean editLayer(MotionEvent motion) {
		return false;
	}

	/*
	 * assign the motion event to the actions include put image, put text and
	 * edit layer
	 */
	protected boolean assign2Actions(MotionEvent motion) {
		// using the motion event according to the current state
		switch (mStatus.getFlag()) {
		case ContentStatus.PUT_IMAGE:
			return putImage(motion);
		case ContentStatus.PUT_TEXT:
			return putText(motion);
		case ContentStatus.EDIT_LAYER:
			return editLayer(motion);
		}
		return false;
	}

	/*
	 * consume the motion event by this class, comparing with the event consumed
	 * by the list view
	 */
	protected boolean assignEvent2Control(MotionEvent motion) {
		// let the widgets and menus to consume the event first
		if (!mBaseView.dispatchTouchEvent(motion)) {
			// then check if there is any widget need to consume motion event
			// that is not inside them.
			if (!assign2Overlay(motion)) {
				// then we consume the motion event ourselved
				return assign2Actions(motion);
			}
		}
		return true;
	}

	/*
	 * dispatch the motion event to the list view
	 */
	protected boolean assignEvent2List(MotionEvent motion) {
		int action = motion.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mPosDown.set((int) motion.getX(), (int) motion.getY());
			break;

		case MotionEvent.ACTION_UP:
			if (motion.getEventTime() - motion.getDownTime() <= 200) {
				Point p = new Point((int) motion.getX(), (int) motion.getY());
				double orientation = Math.abs(MathUtils.getOrientation(
						mPosDown, p));
				int h = Math.min(mHeight / 3, 200);
				if (orientation >= Math.PI / 4 && p.y > h && mPosDown.y < h) {
					hideElem();
					return true;
				}
			}
			break;
		}
		return mList.onTouchEvent(motion);
	}

	private void hideElem() {
		mAnim.addY(-mHeight);
		mAnim.setAnimationListener(listener2);
		mList.startAnimation(mAnim);
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
			File file = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			String path = file.getAbsolutePath();
			if (!file.exists()) {
				file.mkdirs();
			}
			file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"temp.png");
			if (file != null && file.exists()) {
				file.delete();
				file = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						"temp.png");
			}
			try {
				FileOutputStream output = new FileOutputStream(file);
				mPb.toBitmap().compress(Bitmap.CompressFormat.PNG, 0, output);
				output.flush();
				output.close();
			} catch (IOException e) {
				LOG.W("ExternalStorage", "Error writing " + file);
			}
			try {
				String url = Media.insertImage(getContentResolver(),
						file.getAbsolutePath(), file.getName(), file.getName());
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				LOG.D("output", file.getAbsolutePath());
				Uri screenshotUri = Uri.parse(url);
				sharingIntent.setType("*/*");
				// sharingIntent.putExtra(Intent.EXTRA_STREAM,
				// file.getAbsolutePath());
				sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); 
				sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
				sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "DrawingToDo");
				sharingIntent.putExtra(Intent.EXTRA_TEXT, "DrawingToDo");
				sharingIntent.putExtra(Intent.EXTRA_TITLE, "DrawingToDo");
				sharingIntent
						.putExtra(Intent.EXTRA_PHONE_NUMBER, "13851403984");
				sharingIntent.putExtra(Intent.EXTRA_EMAIL,
						new String[] { "liufengyunchina@gmail.com" });
				// sharingIntent
				// .putExtra(android.content.Intent.EXTRA_SUBJECT, "test");
				// sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				// "test send plain text");
				startActivity(Intent.createChooser(sharingIntent,
						"Share image using"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return true;
		case R.id.menulayer:
			showElem();
			return true;
		case R.id.menurefresh:
			mPb.invalidateAll();
			return true;
		case R.id.menusetting:
			Intent intent = new Intent(this, Settings.class);
			intent.putExtra(Settings.COLOR, mPb.getColor());
			intent.putExtra(Settings.EXTRAS, mPb.getPaint());
			startActivityForResult(intent, SETTINGS);
			return true;
		case R.id.menucolor:
			int width = Math.min(this.getWindowManager().getDefaultDisplay()
					.getWidth(), this.getWindowManager().getDefaultDisplay()
					.getHeight()) / 2 - 30;
			new ColorPickerDialog(this, this, mPb.getColor(), width).show();
			return true;

		}
		return false;
	}

	private void showElem() {
		mAnim.addY(mHeight);
		mAnim.setAnimationListener(listener1);
		mList.setAdapter(new DrawElemAdapter(this, mPb.getDrawList()));
		// mList.invalidateViews();
		mList.startAnimation(mAnim);
	}

	private void complete() {
		Intent intent = new Intent();
		intent.putExtra(PaintBoard.BACKGROUND, mPb.toParcel());
		setResult(RESULT_OK, intent);
		finish();
		this.overridePendingTransition(R.anim.zoom_fade_out,
				R.anim.zoom_fade_in);
		mPb.recycle();
	}

	public void onBackPressed() {
		if (mList.getVisibility() == View.GONE) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.alert)
					.setMessage(R.string.alert_exit_edit)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									complete();
								}
							}).setNegativeButton(android.R.string.no, null)
					.show();
		} else if (!mbAnimating) {
			hideElem();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			Bitmap bitmap = null;
			switch (requestCode) {
			case GET_CONTACT:
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
			case GET_CAMERA:
				// return from camera
				bitmap = (Bitmap) data.getExtras().get("data");
				updateContent(bitmap);
				break;
			case GET_IMAGE:
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
				updateContent(bitmap);
				break;
			case SETTINGS:
				mPb.setPaint(data.getIntExtra(Settings.EXTRAS, 0));
				colorChanged(data.getIntExtra(Settings.COLOR, mPb.getColor()));
				savePref();
				break;
			}
		}
	}

	public void updateContent(Bitmap bitmap) {
		if (bitmap != null) {
			mPb.setEditable(false);
			Drawable draw = mTempView.getBackground();
			if (draw != null && draw instanceof BitmapDrawable) {
				Bitmap bmp = ((BitmapDrawable) draw).getBitmap();
				if (bmp != null) {
					bmp.recycle();
				}
			}
			mTempView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			mTempView.setVisibility(View.VISIBLE);
			mTempText.setVisibility(View.GONE);
			mTempView.setText("");
			mStatusImage.setImageResource(R.drawable.images);
			mStatus.flags = ContentStatus.PUT_IMAGE;
		}
	}

	public void updateContent(String name) {
		if (name != null) {
			mPb.setEditable(false);
			mTempView.setText(name);
			mTempView.setVisibility(View.VISIBLE);
			mTempText.setVisibility(View.GONE);
			mTempView.setBackgroundResource(R.drawable.transluent);
			mStatusImage.setImageResource(R.drawable.pencil);
			mStatus.flags = ContentStatus.PUT_TEXT;
		}

	}

	private class ContentStatus {
		private int flags;
		private static final int PUT_IMAGE = 1;
		private static final int NORMAL_DRAWING = 2;
		private static final int PUT_TEXT = 3;
		private static final int EDIT_LAYER = 4;

		public ContentStatus() {
			flags = NORMAL_DRAWING;
		}

		public boolean isNormalDrawing() {
			return flags == NORMAL_DRAWING;
		}

		public int getFlag() {
			return flags;
		}

		public void beginPutImage() {
			mLoad.setText(android.R.string.ok);
			mTempBmp = Bitmap.createBitmap(mTempView.getWidth(),
					mTempView.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mTempBmp);
			mTempView.draw(canvas);
			Rect rect = new Rect(mTempView.getLeft(), mTempView.getTop(),
					mTempView.getRight(), mTempView.getBottom());
			RectF rectf = getRelativeRect(rect);
			MyDrawable draw = new MyDrawable(new BitmapDrawable(mTempBmp),
					rectf, 5, mPb);
			mCurrentDrawable = draw;
			mPb.startDrawTemp(draw);
			mTempView.setVisibility(View.GONE);
		}

		public void beginPutText() {
			mLoad.setText(android.R.string.ok);
			MyText draw = new MyText(mTempView.getText().toString(),
					new PointF(0, 0), mPb.getColor(), 15, mTextSize, mPb);
			mCurrentText = draw;
			mPb.startDrawTemp(draw);
			mTempView.setVisibility(View.GONE);
		}

		public void endPutContent() {
			flags = NORMAL_DRAWING;
			mLoad.setText(R.string.load);
			mTempView.setVisibility(View.GONE);
			mTempText.setVisibility(View.GONE);
			mPb.commitTemp();
			mPb.setEditable(true);
			mCurrentText = null;
			mCurrentDrawable = null;
			mStatusImage.setImageResource(R.drawable.paint);
		}
	}

	protected void loadPrefs() {
		SharedPreferences prefs = this.getSharedPreferences(
				AppendActivity.SHAREPREF, 0);
		if (prefs.contains(AppendActivity.COLOR)) {
			int color = prefs.getInt(AppendActivity.COLOR, Color.WHITE);
			mPb.setColor(color);
			mLine.setColor(color);
			mTempView.setTextColor(color);
			mTempText.setTextColor(color);
			mTempText.setBackgroundColor((~color) | 0xff101010);
		}
		if (prefs.contains(AppendActivity.LINE_WIDTH)) {
			int width = prefs.getInt(AppendActivity.LINE_WIDTH, 3);
			if (width <= 0) {
				width = 1;
			}
			mPb.setLineWidth(width);
			mLine.setLineWidth(width);
			mTextSize = (int) (Math.sqrt(width) * density * 5);
			mTempText.setTextSize(mTextSize);
			mTempView.setTextSize(mTextSize);
			mSeekBar.setProgress(width);
		}
		if (prefs.contains(AppendActivity.LINE_STYLE)) {
			int style = prefs.getInt(AppendActivity.LINE_STYLE, 0);
			mPb.setPaint(style);
		}
	}

	protected void savePref() {
		Editor editor = getSharedPreferences(AppendActivity.SHAREPREF, 0)
				.edit();
		editor.putInt(AppendActivity.COLOR, mPb.getColor());
		editor.putInt(AppendActivity.LINE_WIDTH, mPb.getLineWidth());
		editor.putInt(AppendActivity.LINE_STYLE, mPb.getPaint());
		editor.commit();
	}

	public void onResume() {
		super.onResume();
		loadPrefs();
		mShakeShuffle = new ShakeShuffle(this);
		mShakeShuffle.setShakeShuffleListener(this);
		mShakeShuffle.start();
	}

	public void onPause() {
		super.onPause();
		savePref();
		super.onPause();
	}

	@Override
	public void colorChanged(int color) {
		LOG.D("color", "" + color);
		mPb.setColor(color);
		mLine.setColor(color);
		mTempView.setTextColor(color);
		mTempText.setTextColor(color);
		mTempText.setBackgroundColor((~color) | 0xff101010);
		mColorPal.changeColor(color);
	}

	ArrayList<ColorItem> mPreferColors = new ArrayList<ColorItem>();

	public int getCount() {
		return mPreferColors.size();
	}

	public int getColor(int index) {
		if (index < 0 || index >= mPreferColors.size()) {
			return Color.WHITE;
		}
		return mPreferColors.get(index).getColor();
	}

	public void prepare() {
		Collections.sort(mPreferColors);
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
