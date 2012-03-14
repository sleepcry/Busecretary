package com.chaos.sleepcry.busecretary;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class PaneAnimation extends Animation {
	private int mCurX, mAimX;
	private int mCurY, mAimY;
	private int mCurZ, mAimZ;
	private Camera mCamera = null;

	public PaneAnimation(int x, int y, int z, long duration) {
		super();
		mCurX = mAimX = x;
		mCurY = mAimY = y;
		mCurZ = mAimZ = z;
		this.setDuration(duration);
		this.setFillAfter(true);
		mCamera = new Camera();
	}

	public PaneAnimation(int x) {
		this(x, 0, 0, 0);
	}

	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final Camera camera = mCamera;
		camera.save();
		camera.translate(mAimX - (1-interpolatedTime)*(mAimX - mCurX),
				mAimY - (1-interpolatedTime)*(mAimY - mCurY),
				mAimZ - (1-interpolatedTime)*(mAimZ - mCurZ));
		// camera.rotateZ(angle);
		Matrix m = t.getMatrix();
		camera.getMatrix(m);
		camera.restore();
	}

	public void addX(int x) {
		mCurX = mAimX;
		mAimX += x;
	}

	public int getX() {
		return mAimX;
	}

	public void addY(int y) {
		mCurY = mAimY;
		mAimY += y;
	}

	public int getY() {
		return mAimY;
	}

	public void addZ(int z) {
		mCurZ = mAimZ;
		mAimZ += z;
	}

	public int getZ() {
		return mAimZ;
	}
}
