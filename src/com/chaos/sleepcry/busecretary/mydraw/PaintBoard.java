package com.chaos.sleepcry.busecretary.mydraw;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.chaos.sleepcry.busecretary.R;

public class PaintBoard extends View implements OnTouchListener {
	// all things need to draw on the canvas
	List<Mydraw> mDrawList = null;
	// all the things once ocur
	List<Mydraw> mHistory = null;
	List<AbstractOperation> mOperList = null;
	AbstractOperation mCurOp = null;
	boolean bEditable;
	Context mContext = null;
	Paint mPaint = null;
	int mLineFlag = 0;
	Bitmap mBitmap, mTempBitmap;
	Canvas mCanvas, mTempCanvas;
	ArrayList<Mydraw> undraw = new ArrayList<Mydraw>();
	public static final String BACKGROUND = "background";

	public PaintBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();

	}

	public PaintBoard(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		mDrawList = new ArrayList<Mydraw>();
		mHistory = new ArrayList<Mydraw>();
		mOperList = new ArrayList<AbstractOperation>();
		this.setOnTouchListener(this);
		mCurColor = Color.WHITE;
		bEditable = true;
		mLineWidth = 3;
		mPaint = new Paint(Paint.DITHER_FLAG);
		mTouchPlayer = MediaPlayer.create(mContext, R.raw.waterdrop);
		mRedoPlayer = MediaPlayer.create(mContext, R.raw.redo);
		mUndoPlayer = MediaPlayer.create(mContext, R.raw.undo);
		mAlertPlayer = MediaPlayer.create(mContext, R.raw.nomatch);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
//		LOG.logMem(mContext);
		Bitmap temp = mBitmap;
		Bitmap temptemp = mTempBitmap;
		mBitmap = Bitmap.createBitmap((int) (w * Mydraw.DIV),
				(int) (h * Mydraw.DIV), Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		if (temp != null) {
			mCanvas.drawBitmap(temp,
					new Rect(0, 0, temp.getWidth(), temp.getHeight()),
					new Rect(0, 0, w, h), mPaint);
			// mCanvas.drawBitmap(temp, 0, 0, mPaint);
			temp.recycle();
		}
		mTempBitmap = Bitmap.createBitmap((int) (w * Mydraw.DIV),
				(int) (h * Mydraw.DIV), Bitmap.Config.ARGB_8888);
		mTempCanvas = new Canvas(mTempBitmap);
		if (temptemp != null) {
			mTempCanvas.drawBitmap(temptemp, new Rect(0, 0,
					temptemp.getWidth(), temptemp.getHeight()), new Rect(0, 0,
					w, h), mPaint);
			temptemp.recycle();
		}
	}

	public void setEditable(boolean canEdit) {
		bEditable = canEdit;
	}

	public void drawMyDraw(Mydraw mydraw) {
		if (mCanvas == null && mydraw != null) {
			undraw.add(mydraw);
		} else if (mCanvas != null) {
			for (int i = 0; i < undraw.size(); i++) {
				undraw.get(i).draw(mCanvas);
			}
			undraw.clear();
			if (mydraw != null) {
				mydraw.draw(mCanvas);
			}
		}
	}

	private boolean bDrawTemp = false;
	private Mydraw mTempDraw;

	public void startDrawTemp(Mydraw mydraw) {
		clearTemp();
		mTempDraw = mydraw;
		bDrawTemp = true;
	}

	public void clearTemp() {
		if (mTempCanvas != null) {
			mTempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		}
	}

	public void drawTemp() {
		if (mTempDraw != null && bDrawTemp && mTempCanvas != null) {
			mTempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
			mTempDraw.draw(mTempCanvas);
			invalidate();
		}
	}

	public void commitTemp() {
		bDrawTemp = false;
		add(mTempDraw);
		mTempDraw = null;
	}

	public void cancelTemp() {
		bDrawTemp = false;
		invalidate();
		mTempDraw = null;
	}

	public void postInvalidateAll() {

	}

	public void invalidateAll() {
		if (mCanvas == null || mDrawList == null)
			return;
		mCanvas.drawColor(Color.BLACK);
		Collections.sort(mDrawList);
		for (int i = 0; i < mDrawList.size(); i++) {
			Mydraw mydraw = mDrawList.get(i);
			if (mydraw.isVisible()) {
				mDrawList.get(i).draw(mCanvas);
			}
		}
		invalidate();
	}
	public void setPaint(int flag) {
		mLineFlag = flag;
	}
	public void onDraw(Canvas canvas) {
		drawMyDraw(null);
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(),
					mBitmap.getHeight()), new Rect(0, 0, getWidth(),
					getHeight()), mPaint);
		}
		if (bDrawTemp && mTempBitmap != null) {
			canvas.drawBitmap(
					mTempBitmap,
					new Rect(0, 0, mTempBitmap.getWidth(), mTempBitmap
							.getHeight()), new Rect(0, 0, getWidth(),
							getHeight()), mPaint);
		}
		// draw border
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);
		canvas.drawRect(new Rect(getLeft(), getTop(), getRight(), getBottom()),
				paint);
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (!bEditable) {
			return false;
		}
		int pt_cnt = e.getPointerCount();
//		LOG.D("paintboard", "" + pt_cnt);
		if (pt_cnt >= 2 || mbZoom) {
			cancelTemp();
			mCurPolyline = null;
			mbDrawLine = false;
			mMoveTrack.clear();
			mAlertPlayer.start();
			return zoom(e);
		} else {
			return drawLine(e);
		}
	}

	boolean mbDrawLine = false;
	// current color for new stuffs
	int mCurColor;
	int mLineWidth;
	MyPolyLine mCurPolyline = null;
	List<PointF> mMoveTrack = new ArrayList<PointF>();
	float mX, mY;
	public static final float MIN_GAP = 5;
	int test = 0;

	private boolean drawLine(MotionEvent e) {
		Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		int action = e.getAction();
		PointF[] pts = null;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			test = 0;
			mbDrawLine = true;
			mTouchPlayer.start();
			assert (mMoveTrack.size() == 0);
			mX = e.getX() - rect.left;
			mY = e.getY() - rect.top;
			mMoveTrack.add(new PointF(mX / rect.width(), mY / rect.height()));
			mCurPolyline = new MyPolyLine(null, mCurColor, 11, mLineWidth, this);
			mCurPolyline.setPaint(mLineFlag, mCurColor, 10);
			startDrawTemp(mCurPolyline);
			return true;
		case MotionEvent.ACTION_CANCEL:
			if (mbDrawLine) {
				mbDrawLine = false;
				// mDrawList.remove(mCurPolyline);
				mMoveTrack.clear();
				mCurPolyline = null;
				cancelTemp();
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (mbDrawLine) {
				int historySize = e.getHistorySize();
				float x, y;
				for (int i = 0; i < historySize; i++) {
					x = e.getHistoricalX(i) - rect.left;
					y = e.getHistoricalY(i) - rect.top;
					if (Math.sqrt((mX - x) * (mX - x) + (mY - y) * (mY - y)) > MIN_GAP) {
						mMoveTrack.add(new PointF(x / rect.width(), y
								/ rect.height()));
						mX = x;
						mY = y;
					} else {
						test++;
					}

				}
				x = e.getX() - rect.left;
				y = e.getY() - rect.top;
				if (Math.sqrt((mX - x) * (mX - x) + (mY - y) * (mY - y)) > MIN_GAP) {
					mMoveTrack.add(new PointF(x / rect.width(), y
							/ rect.height()));
					mX = x;
					mY = y;
				} else {
					test++;
				}

				pts = new PointF[mMoveTrack.size()];
				mMoveTrack.toArray(pts);
				mCurPolyline.genPatn(pts);
				drawTemp();
				invalidate();
				return true;
			}
		case MotionEvent.ACTION_UP:
			if (mbDrawLine) {
				mbDrawLine = false;
				mMoveTrack.add(new PointF(
						(e.getX() - rect.left) / rect.width(),
						(e.getY() - rect.top) / rect.height()));
				pts = new PointF[mMoveTrack.size()];
				mMoveTrack.toArray(pts);
				mCurPolyline.genPatn(pts);
				drawTemp();
				commitTemp();
//				LOG.D("memory", "ignored point:" + test);
//				LOG.D("memory", "total point:" + mMoveTrack.size());
				mMoveTrack.clear();
				mCurPolyline = null;
				return true;
			}
		}
		return false;
	}

	boolean mbZoom = false;
	Point mptZoomDown1 = new Point();
	Point mptZoomDown2 = new Point();

	private boolean zoom(MotionEvent e) {
		int pt_cnt = e.getPointerCount();
		if (pt_cnt < 2 || mListener == null) {
			mbZoom = false;
			return false;
		}
		switch (e.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			mbZoom = true;
			mptZoomDown1.set((int) e.getX(0), (int) e.getY(0));
			mptZoomDown2.set((int) e.getX(1), (int) e.getY(1));
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mbZoom) {
				mbZoom = false;
			}
			mAlertPlayer.start();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (mbZoom) {
				mbZoom = false;
				float x1 = e.getX(0);
				float y1 = e.getY(0);
				float x2 = e.getX(1);
				float y2 = e.getY(1);
				if (Math.abs(x1 - x2) + Math.abs(y1 - y2) < Math
						.abs(mptZoomDown1.x - mptZoomDown2.x)
						+ Math.abs(mptZoomDown1.y - mptZoomDown2.y)) {
					mListener.zoomIn((x1 + x2) / 2, (y1 + y2) / 2);
				} else {
					mListener.zoomOut((x1 + x2) / 2, (y1 + y2) / 2);
				}
			}
			break;
		}
		return false;
	}

	public static int MAX_HISTORY = 64;

	public Mydraw add(Mydraw draw) {
		if(draw == null)return null;
		// abandon the tail
		//TODO:
		if (mCurOp != null) {
			int opindex = mOperList.indexOf(mCurOp);
			if (opindex != -1 && opindex != mOperList.size() - 1) {
				List<AbstractOperation> temp = mOperList;
				mOperList = new ArrayList<AbstractOperation>();
				for (int i = 0; i < opindex; i++) {
					mOperList.add(temp.get(i));
				}
				temp.clear();
			}
			//clear the unreachable draws
			//because a draw must be bound to a history
			Mydraw previousDraw = mCurOp.getDraw();
			if (previousDraw != null) {
				int hisindex = mHistory.indexOf(previousDraw);
				if (hisindex != -1 && hisindex != mHistory.size() - 1) {
					List<Mydraw> temp = mHistory;
					mHistory = new ArrayList<Mydraw>();
					for (int i = 0; i < hisindex; i++) {
						mHistory.add(temp.get(i));
					}
					temp.clear();
				}
			}
		}
		//if the count of draws is greater than MAX_HISTORY, then combine half of them
		if (mDrawList.size() >= MAX_HISTORY) {
			//combine to a MyDrawable
			Bitmap bmp = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			for(int i=0;i<MAX_HISTORY;i++) {
				mDrawList.get(i).draw(canvas);
			}
			List<Mydraw> tempDraw = mDrawList;
			List<AbstractOperation> tempOp = mOperList;
			List<Mydraw> tempHis = mHistory;
			mDrawList = new ArrayList<Mydraw>();
			mOperList = new ArrayList<AbstractOperation>();
			mHistory = new ArrayList<Mydraw>();
			for(int i=MAX_HISTORY;i<tempDraw.size();i++) {
				mDrawList.add(tempDraw.get(i));
				mOperList.add(tempOp.get(i));
				mHistory.add(tempHis.get(i));
			}
			tempDraw.clear();
			tempOp.clear();
			tempHis.clear();
			MyDrawable combineDrawable = new MyDrawable(new BitmapDrawable(bmp), new RectF(0,0,1,1),
					0, this);
			AppendOperation operation = new AppendOperation(combineDrawable, mDrawList);
			mHistory.add(0,combineDrawable);
			mDrawList.add(0,combineDrawable);
			mOperList.add(0,operation);
//			LOG.D("history", "history raise too large, combine it!");
		}
		//now , time to add the current one
		draw.setView(this);
		drawMyDraw(draw);
		mHistory.add(draw);
		mCurOp = new AppendOperation(draw, mDrawList);
//		mDrawList.add(draw);
		mOperList.add(mCurOp);
		mCurOp.execute();
		
		invalidate();
		return draw;
	}

	public boolean canRedo() {
		if (mCurOp != null && mCurOp.canRedo()) {
			return true;
		}
		int index = mOperList.indexOf(mCurOp);
		if (index != -1 && index < mOperList.size() - 1) {
			AbstractOperation op = mOperList.get(index + 1);
			if (op != null && op.canRedo()) {
				return true;
			}
		}
		return false;
	}

	public boolean canUndo() {
		if (mCurOp != null && mCurOp.canUndo()) {
			return true;
		}
		return false;
	}
	private MediaPlayer mRedoPlayer,mUndoPlayer,mTouchPlayer,mAlertPlayer;
	public void redo() {
		if (mCurOp != null && mCurOp.canRedo()) {
			if (mCurOp.redo()) {
				mRedoPlayer.start();
				invalidateAll();
				return;
			}
		}
		int index = mOperList.indexOf(mCurOp);
		if (index != -1 && index < mOperList.size() - 1) {
			mCurOp = mOperList.get(index + 1);
		}
		if (mCurOp != null && mCurOp.redo()) {
			mRedoPlayer.start();
			invalidateAll();
		}
	}
	
	public void undo() {
		if (mCurOp != null && mCurOp.undo()) {
			mUndoPlayer.start();
			invalidateAll();
			int index = mOperList.indexOf(mCurOp);
			if (index != -1 && index > 0) {
				mCurOp = mOperList.get(index - 1);
			}
		}
	}

	public void clear() {
		mOperList.clear();
		mDrawList.clear();
		mHistory.clear();
		undraw.clear();
		// TODO:
		mCurOp = null;
		invalidateAll();
	}

	public Mydraw[] getDrawList() {
		Mydraw[] mydraws = new Mydraw[mDrawList.size()];
		return mDrawList.toArray(mydraws);
	}

	public Bitmap toBitmap() {
		return mBitmap;
	}

	public Parcelable toParcel() {
		assert (mBitmap != null);
		String filePath = "rawimage.png";
//		LOG.D("memory", filePath);
		MyDrawable.setContext(mContext);
		FileOutputStream output = null;
		try {
			output = mContext.openFileOutput(filePath,
					Context.MODE_WORLD_WRITEABLE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		if (mBitmap.compress(CompressFormat.PNG, 0, output)) {
			try {
				output.close();
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new MyDrawable(filePath, 0, null);
	}

	public void setColor(int color) {
		mCurColor = color;
	}

	public void setLineWidth(int progress) {
		mLineWidth = progress;
	}

	public void changeVisibility(int position) {
		if (position >= mDrawList.size() || position < 0) {
			return;
		}
		Mydraw mydraw = mDrawList.get(position);
		mydraw.setVisible(!mydraw.isVisible());
		invalidate();
	}

	public static interface PaintBoardListener {
		public void zoomIn(float x, float y);

		public void zoomOut(float x, float y);

		public void doubleClick(float x, float y);
	}

	PaintBoardListener mListener = null;

	public void setPBListener(PaintBoardListener listener) {
		mListener = listener;
	}

	public int getColor() {
		return mCurColor;
	}

	public int getLineWidth() {
		return mLineWidth;
	}

	public void recycle() {
		System.gc();
	}

	public int getPaint() {
		return mLineFlag;
	}
}
