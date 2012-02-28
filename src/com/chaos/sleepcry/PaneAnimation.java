package com.chaos.sleepcry;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class PaneAnimation extends Animation{
	private int dx;
	private int dy;
	private int dz;
	private Camera mCamera = null;
	public PaneAnimation(int x,int y,int z,long duration){
		super();
		dx = x;
		dy = y;
		dz = z;
		this.setDuration(duration);
		this.setFillAfter(true);
		if(duration > 0){
			this.setInterpolator(new AccelerateDecelerateInterpolator());
		}
		mCamera = new Camera();
	}
	public PaneAnimation(int x){
		this(x,0,0,0);
	}
    protected void applyTransformation(float interpolatedTime, Transformation t) {
    	final Camera camera = mCamera;
    	camera.save();
    	camera.translate(dx, dy, dz);
    	Matrix m = t.getMatrix();
    	camera.getMatrix(m);
    	camera.restore();
    }
    public void addX(int x){
    	dx += x;
    }
    public int getX(){
    	return dx;
    }
    public void addY(int y){
    	dy += y;
    }
    public int getY(){
    	return dy;
    }
    public void addZ(int z){
    	dz += z;
    }
    public int getZ(){
    	return dz;
    }
}
