package com.chaos.sleepcry.busecretary.mydraw;

import java.util.List;

public class AppendOperation extends AbstractOperation {
	// the operation will act on
	List<Mydraw> mOperObj = null;
	Mydraw mDraw = null;

	public AppendOperation(Mydraw draw, List<Mydraw> oplst) {
		mOperObj = oplst;
		mDraw = draw;
		mFlag = 0;
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
		mOperObj.remove(mDraw);
		return true;
	}

	@Override
	public int execute() {
		if (mOperObj != null && mDraw != null) {
			mOperObj.add(mDraw);
		}
		return super.execute();

	}

	@Override
	public Mydraw getDraw() {
		return mDraw;
	}
}
