package com.chaos.sleepcry.busecretary.mydraw;

import android.util.Log;

public abstract class AbstractOperation {
	/*
	 * redo & undo flag redo or execute will accelerate this flag undo will
	 * decelerate this flag
	 */
	int mFlag;

	public AbstractOperation() {
		mFlag = 0;
	}

	public boolean redo(){
		Log.d("draw","redo" + hashCode() +" " + mFlag);
		if(canRedo()){
			mFlag ++;
			return true;
		}
		return false;
	}

	public boolean undo(){
		Log.d("draw","undo" + hashCode() +" " + mFlag);
		if(canUndo()){
			mFlag --;
			return true;
		}
		return false;
	}

	public boolean canRedo() {
		return mFlag <= 0;
	}

	public boolean canUndo() {
		return mFlag > 0;
	}

	public int execute(){
		mFlag ++;
		return mFlag;
	}

	public abstract Mydraw getDraw();
}
