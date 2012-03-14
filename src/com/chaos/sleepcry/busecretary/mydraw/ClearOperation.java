package com.chaos.sleepcry.busecretary.mydraw;

import java.util.ArrayList;
import java.util.List;

public class ClearOperation extends AbstractOperation {
	// the operation will act on
	List<Mydraw> mOpList = null;
	List<Mydraw> mSaveList = null;

	public ClearOperation(List<Mydraw> lstOp) {
		mSaveList = new ArrayList<Mydraw>();
		mOpList = lstOp;
	}

	@Override
	public boolean redo() {
		if (canRedo()) {
			execute();
			return true;
		}
		return false;
	}

	@Override
	public boolean undo() {
		if (!super.undo()) {
			return false;
		}
		if (mOpList != null) {
			mOpList.addAll(mSaveList);
			return true;
		}
		return false;

	}

	@Override
	public int execute() {
		if (mOpList != null) {
			mSaveList.clear();
			mSaveList.addAll(mOpList);
			mOpList.clear();
		}
		return super.execute();
	}

	@Override
	public Mydraw getDraw() {
		if (mSaveList != null && mSaveList.size() > 0) {
			return mSaveList.get(mSaveList.size() - 1);
		}
		return null;
	}
}
