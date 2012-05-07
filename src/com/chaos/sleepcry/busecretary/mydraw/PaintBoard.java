package com.chaos.sleepcry.busecretary.mydraw;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.LOG;
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
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

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
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Bitmap temp = mBitmap;
		Bitmap temptemp = mTempBitmap;
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		if (temp != null) {
			mCanvas.drawBitmap(temp, 0, 0, mPaint);
		}
		mTempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mTempCanvas = new Canvas(mTempBitmap);
		if (temptemp != null) {
			mTempCanvas.drawBitmap(temptemp, 0, 0, mPaint);
		}
	}

	public void setEditable(boolean canEdit) {
		bEditable = canEdit;
	}

	public void drawMyDraw(Mydraw mydraw) {
		if (mCanvas == null && mydraw != null) {
			undraw.add(mydraw);
		} else {
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
		setEditable(false);
	}
	public void clearTemp() {
		mTempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
//		mTempBitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
//		mTempCanvas = new Canvas(mTempBitmap);
	}
	public void drawTemp() {
		if (mTempDraw != null && bDrawTemp) {
			mTempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
			mTempDraw.draw(mTempCanvas);
			invalidate();
		}
	}
	public void commitTemp() {
		setEditable(true);
		bDrawTemp = false;
		mTempCanvas.drawColor(0);
		add(mTempDraw);
//		mTempCanvas = null;
//		mTempBitmap = null;
	}
	public void invalidateAll() {
		mCanvas.drawColor(Color.BLACK);
		for (int i = 0; i < mDrawList.size(); i++) {
			Mydraw mydraw = mDrawList.get(i);
			if (mydraw.isVisible()) {
				mDrawList.get(i).draw(mCanvas);
			}
		}
		invalidate();
	}

	public void onDraw(Canvas canvas) {
		drawMyDraw(null);
		canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		if(bDrawTemp ) {
			canvas.drawBitmap(mTempBitmap,0,0,mPaint);
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
		LOG.D("paintboard", "" + pt_cnt);
		if (pt_cnt >= 2 || mbZoom) {
			mDrawList.remove(mCurPolyline);
			mbDrawLine = false;
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
	List<PointF> mMoveTrack = null;
	private boolean drawLine(MotionEvent e) {
		Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		int action = e.getAction();
		PointF[] pts = null;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mbDrawLine = true;
			mMoveTrack = new ArrayList<PointF>();
			mMoveTrack.add(new PointF((e.getX() - rect.left) / rect.width(), (e
					.getY() - rect.top) / rect.height()));
			return true;
		case MotionEvent.ACTION_CANCEL:
			if (mbDrawLine) {
				mbDrawLine = false;
				mDrawList.remove(mCurPolyline);
				mMoveTrack = null;
				mCurPolyline = null;
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (mbDrawLine) {
				int historySize = e.getHistorySize();
				for (int i = 0; i < historySize; i++) {
					mMoveTrack.add(new PointF((e.getHistoricalX(i) - rect.left)
							/ rect.width(), (e.getHistoricalY(i) - rect.top)
							/ rect.height()));
				}
				mMoveTrack.add(new PointF(
						(e.getX() - rect.left) / rect.width(),
						(e.getY() - rect.top) / rect.height()));
				pts = new PointF[mMoveTrack.size()];
				// remove the previous one
				mDrawList.remove(mCurPolyline);
				// add the updated one
				mCurPolyline = new MyPolyLine(mMoveTrack.toArray(pts),
						mCurColor, mDrawList.size(), mLineWidth, this);
				mDrawList.add(mCurPolyline);
				drawMyDraw(mCurPolyline);
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
				// remove the previous one
				mDrawList.remove(mCurPolyline);
				// add the updated one
				mCurPolyline = new MyPolyLine(mMoveTrack.toArray(pts),
						mCurColor, mDrawList.size(), mLineWidth, this);
				add(mCurPolyline);
				mMoveTrack = null;
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

	public Mydraw add(Mydraw draw) {
		// abandon the tail
		if (mCurOp != null) {
			int opindex = mOperList.indexOf(mCurOp);
			if (opindex != -1 && opindex != mOperList.size() - 1) {
				mOperList = mOperList.subList(0, opindex + 1);
				List<AbstractOperation> temp = mOperList;
				mOperList = new ArrayList<AbstractOperation>();
				for (int i = 0; i < opindex; i++) {
					mOperList.add(temp.get(i));
				}
			}
			Mydraw previousDraw = mCurOp.getDraw();
			if (previousDraw != null) {
				int hisindex = mHistory.indexOf(previousDraw);
				if (hisindex != -1 && hisindex != mHistory.size() - 1) {
					List<Mydraw> temp = mHistory;
					mHistory = new ArrayList<Mydraw>();
					for (int i = 0; i < hisindex; i++) {
						mHistory.add(temp.get(i));
					}
				}
			}
		}
		draw.setView(this);
		drawMyDraw(draw);
		mHistory.add(draw);
		mCurOp = new AppendOperation(draw, mDrawList);
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

	public void redo() {
		if (mCurOp != null && mCurOp.canRedo()) {
			if (mCurOp.redo()) {
				invalidateAll();
				return;
			}
		}
		int index = mOperList.indexOf(mCurOp);
		if (index != -1 && index < mOperList.size() - 1) {
			mCurOp = mOperList.get(index + 1);
		}
		if (mCurOp != null && mCurOp.redo()) {
			invalidateAll();
		}
	}

	public void undo() {
		if (mCurOp != null && mCurOp.undo()) {
			invalidateAll();
			int index = mOperList.indexOf(mCurOp);
			if (index != -1 && index > 0) {
				mCurOp = mOperList.get(index - 1);
			}
		}
	}

	public void clear() {
		mCurOp = new ClearOperation(mDrawList);
		mOperList.add(mCurOp);
		mCurOp.execute();
		invalidateAll();
		Log.d("draw", "clear");
	}

	public void permenantClear() {
		mOperList.clear();
		mDrawList.clear();
		mHistory.clear();
		// TODO:
		mCurOp = null;
		invalidateAll();
	}

	public Mydraw[] getDrawList() {
		Mydraw[] mydraws = new Mydraw[mDrawList.size()];
		return mDrawList.toArray(mydraws);
	}

	public Bitmap toBitmap() {
		Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		onDraw(canvas);
		return bitmap;
	}

	public Parcelable toParcel() {
		MyDrawable.setContext(mContext);
		FileOutputStream output = null;
		try {
			output = mContext.openFileOutput("rawimage.png",
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
		MyDrawable mydraw = new MyDrawable("rawimage.png",
				new RectF(0, 0, 1, 1), 0, null);
		return mydraw;
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
}
