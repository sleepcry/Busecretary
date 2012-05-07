package com.chaos.sleepcry.busecretary.canvasedit;

import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.mydraw.Mydraw;

public class DrawElemAdapter extends BaseAdapter {
	CanvasEditActivity mContext;
	Mydraw[] mDraws;

	public DrawElemAdapter(CanvasEditActivity c, Mydraw[] drawList) {
		mContext = c;
		mDraws = drawList;
		mDataMap = new HashMap<View, Mydraw>();
		mGroupMap = new HashMap<View, Mydraw>();
		mSpinnerAdapter = ArrayAdapter.createFromResource(mContext,
				R.array.layer, android.R.layout.simple_spinner_item);
		mSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDraws == null ? 0 : mDraws.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	HashMap<View, Mydraw> mGroupMap;
	HashMap<View, Mydraw> mDataMap;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout groupLayout = null;
		if (convertView != null) {
			groupLayout = (LinearLayout) convertView;
		} else {
			groupLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(
					R.layout.elem_parent, null);
		}
		TextView tView = (TextView) groupLayout.findViewById(R.id.desc);
		tView.setText(mDraws[position].toString());
		tView.setTag(position);
		CheckBox cBox = (CheckBox) groupLayout.findViewById(R.id.bvisual);
		cBox.setChecked(mDraws[position].isVisible());
		cBox.setOnCheckedChangeListener(mOnCheckListener);
		mGroupMap.put(cBox, mDraws[position]);
		Spinner spinner = null;
		spinner = (Spinner) groupLayout.findViewById(R.id.layer);
		spinner.setOnItemSelectedListener(mOnItemListener);
		spinner.setAdapter(mSpinnerAdapter);
		spinner.setSelection(mDraws[position].getLayer());
		mDataMap.put(spinner, mDraws[position]);
		return groupLayout;
	}

}
