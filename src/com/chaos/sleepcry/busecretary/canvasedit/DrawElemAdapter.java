package com.chaos.sleepcry.busecretary.canvasedit;

import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.mydraw.Mydraw;

public class DrawElemAdapter extends BaseExpandableListAdapter {
	CanvasEditActivity mContext;
	Mydraw[] mDraws;

	public DrawElemAdapter(CanvasEditActivity c, Mydraw[] drawList) {
		mContext = c;
		mDraws = drawList;
		mDataMap = new HashMap<View, Mydraw>();
		mGroupMap = new HashMap<View, Mydraw>();
		mSpinnerAdapter  = ArrayAdapter.createFromResource(
				mContext, R.array.layer, android.R.layout.simple_spinner_item);
		mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	ArrayAdapter<CharSequence> mSpinnerAdapter;
	OnItemSelectedListener mOnItemListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			mDataMap.get(parent).setLayer(position);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	};
	HashMap<View, Mydraw> mDataMap;

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Spinner spinner = null;
		if (convertView != null) {
			spinner = (Spinner) convertView;
		} else {
			spinner = (Spinner) LayoutInflater.from(mContext).inflate(
					R.layout.elem_children, null);
			spinner.setOnItemSelectedListener(mOnItemListener);
			spinner.setAdapter(mSpinnerAdapter);
		}
		spinner.setSelection(mDraws[groupPosition].getLayer());
		mDataMap.put(spinner, mDraws[groupPosition]);
		return spinner;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupPosition < mDraws.length ? mDraws[groupPosition] : null;
	}

	@Override
	public int getGroupCount() {
		return mDraws.length;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	OnCheckedChangeListener mOnCheckListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			mGroupMap.get(buttonView).setVisible(isChecked);
		}

	};
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	HashMap<View, Mydraw> mGroupMap;
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LinearLayout groupLayout = null;
		if (convertView != null) {
			groupLayout = (LinearLayout) convertView;
		} else {
			groupLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(
					R.layout.elem_parent, null);
		}
		TextView tView = (TextView) groupLayout.findViewById(R.id.desc);
		tView.setText(mDraws[groupPosition].toString());
		tView.setTag(groupPosition);
		tView.setOnClickListener(mContext);
		CheckBox cBox = (CheckBox) groupLayout.findViewById(R.id.bvisual);
		cBox.setChecked(mDraws[groupPosition].isVisible());
		cBox.setOnCheckedChangeListener(mOnCheckListener);
		mGroupMap.put(cBox, mDraws[groupPosition]);
		return groupLayout;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
