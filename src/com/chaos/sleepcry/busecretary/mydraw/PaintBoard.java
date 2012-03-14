package com.chaos.sleepcry.busecretary.mydraw;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
	MyPolyLine mCurPolyline = null;
	// current color for new stuffs
	int mCurColor;
	AbstractOperation mCurOp = null;
	boolean bEditable;
	List<PointF> mMoveTrack = null;
	Context mContext = null;

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
		this.setBackgroundColor(Color.WHITE);
		mCurColor = Color.BLACK;
		bEditable = true;
	}

	public void setEditable(boolean canEdit) {
		bEditable = canEdit;
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw border
		Paint paint = new Paint();
		paint.setColor(0x7f7fff7f);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);
		canvas.drawRect(new Rect(getLeft(), getTop(), getRight(), getBottom()),
				paint);
		for (int i = 0; i < mDrawList.size(); i++) {
			mDrawList.get(i).draw(canvas);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		if (!bEditable) {
			return false;
		}
		Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		int action = e.getAction();
		PointF[] pts = null;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mMoveTrack = new ArrayList<PointF>();
			mMoveTrack.add(new PointF((e.getX()-rect.left)/rect.width(), (e.getY()-rect.top)/rect.height()));
			return true;
		case MotionEvent.ACTION_CANCEL:
			mDrawList.remove(mCurPolyline);
			mMoveTrack = null;
			mCurPolyline = null;
			return true;
		case MotionEvent.ACTION_MOVE:
			int historySize = e.getHistorySize();
			for (int i = 0; i < historySize; i++) {
				mMoveTrack.add(new PointF((e.getHistoricalX(i)-rect.left)/rect.width(), (e
						.getHistoricalY(i)-rect.top)/rect.height()));
			}
			mMoveTrack.add(new PointF((e.getX()-rect.left)/rect.width(), (e.getY()-rect.top)/rect.height()));
			pts = new PointF[mMoveTrack.size()];
			// remove the previous one
			mDrawList.remove(mCurPolyline);
			// add the updated one
			mCurPolyline = new MyPolyLine(mMoveTrack.toArray(pts), mCurColor,
					mDrawList.size());
			mCurPolyline.setView(this);
			mDrawList.add(mCurPolyline);
			invalidate();
			return true;
		case MotionEvent.ACTION_UP:
			mMoveTrack.add(new PointF((e.getX()-rect.left)/rect.width(), (e.getY()-rect.top)/rect.height()));
			pts = new PointF[mMoveTrack.size()];
			// remove the previous one
			mDrawList.remove(mCurPolyline);
			// add the updated one
			mCurPolyline = new MyPolyLine(mMoveTrack.toArray(pts), mCurColor,
					mDrawList.size());
			add(mCurPolyline);
			mMoveTrack = null;
			mCurPolyline = null;
			return true;
		}
		return false;
	}

	public void add(Mydraw draw) {
		// abandon the tail
		if (mCurOp != null) {
			int opindex = mOperList.indexOf(mCurOp);
			if (opindex != -1 && opindex != mOperList.size() - 1) {
				mOperList = mOperList.subList(0, opindex + 1);
			}
			Mydraw previousDraw = mCurOp.getDraw();
			if (previousDraw != null) {
				int hisindex = mHistory.indexOf(previousDraw);
				if (hisindex != -1 && hisindex != mHistory.size() - 1) {
					mHistory = mHistory.subList(0, hisindex + 1);
				}
			}
		}
		draw.setView(this);
		mHistory.add(draw);
		mCurOp = new AppendOperation(draw, mDrawList);
		mOperList.add(mCurOp);
		mCurOp.execute();
		
		invalidate();
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
				invalidate();
				return;
			}
		}
		int index = mOperList.indexOf(mCurOp);
		if (index != -1 && index < mOperList.size() - 1) {
			mCurOp = mOperList.get(index + 1);
		}
		if (mCurOp != null && mCurOp.redo()) {
			invalidate();
		}
	}

	public void undo() {
		if (mCurOp != null && mCurOp.undo()) {
			invalidate();
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
		invalidate();
		Log.d("draw", "clear");
	}

	public Mydraw[] getDrawList() {
		Mydraw[] mydraws = new Mydraw[mDrawList.size()];
		return mDrawList.toArray(mydraws);
	}
	public Bitmap toBitmap(){
		Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		onDraw(canvas);
		return bitmap;
	}
	public Parcelable toParcel() {
		Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		onDraw(canvas);
		MyDrawable.setContext(mContext);
		FileOutputStream output = null;
		try {
			output = mContext.openFileOutput("rawimage.png", Context.MODE_WORLD_WRITEABLE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		if( bitmap.compress(CompressFormat.PNG, 0, output)){
			try {
				output.close();
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		MyDrawable mydraw = new MyDrawable("rawimage.png",new RectF(0,0,1,1),0);
		return mydraw;
	}
}
