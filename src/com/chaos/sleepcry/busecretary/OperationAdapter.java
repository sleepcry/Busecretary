package com.chaos.sleepcry.busecretary;

import java.util.ArrayList;
import java.util.List;

import utils.LOG;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.chaos.sleepcry.busecretary.notify.NotificationData;

public class OperationAdapter extends BaseAdapter {

	public static final int WHEN = 0;
	public static final int WHERE = 1;
	public static final int NOTIFICATION = 2;
	public static final int REPEAT = 3;
	public static final int WHAT = 4;
	public static final int SEARCH = 5;
	private int mLayer;
	NotificationData mData;
	BusecretaryActivity mCtxt;
	List<InfoObj> lstData = null;
	ArrayList<ArrayList<InfoObj>> lstAllData = null;
	

	public OperationAdapter(BusecretaryActivity context) {
		mLayer = 0;
		mCtxt = context;
		lstData = new ArrayList<InfoObj>();
		
		lstAllData = new ArrayList<ArrayList<InfoObj>>();
		ArrayList<InfoObj> layer0 = new ArrayList<InfoObj>();
		layer0.add(new InfoObj("when?",WHEN));
		layer0.add(new InfoObj("where?",WHERE));
		layer0.add(new InfoObj("what?",WHAT));
		lstAllData.add(layer0);
		ArrayList<InfoObj> layer1 = new ArrayList<InfoObj>();
		layer1.add(new InfoObj("notify?",NOTIFICATION));
		layer1.add(new InfoObj("repeat?",REPEAT));
		layer1.add(new InfoObj("search?",SEARCH));
		lstAllData.add(layer1);
	}

	public void setData(NotificationData data) {
		mData = data;
		notifyDataSetChanged();
	}
	public NotificationData getData() {
		return mData;
	}
	private class InfoObj {
		public String content;
		public int id;

		public InfoObj(String c, int i) {
			content = c;
			id = i;
		}
	}

	public boolean fetchmore() {
		if (mLayer < lstAllData.size()) {
			mLayer++;
			update();
			return true;
		}
		return false;
	}
	public boolean hasMore() {
		return mLayer<lstAllData.size();
	}
	public void update() {
		lstData.clear();
		for (int i = 0; i < mLayer; i++) {
			for (int j = 0; j < lstAllData.get(i).size(); j++) {
				lstData.add(lstAllData.get(i).get(j));
			}
		}
	}

	@Override
	public int getCount() {
		return lstData.size();
	}

	@Override
	public Object getItem(int position) {
		return lstData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return lstData.get(position).id;
	}

	public int getItemId(String text) {
		for (int i = 0; i < lstData.size(); i++) {
			InfoObj obj = lstData.get(i);
			if (obj.content.equals(text)) {
				return obj.id;
			}
		}
		return -1;
	}

	@Override
	public int getItemViewType(int position) {
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Button btn = null;
		if(convertView != null) {
			btn = (Button) convertView;
		}
		else{
			btn = new Button(mCtxt);
		}
		if (mData != null) {
			LOG.D("NotificationData","list " +lstData.get(position).toString());
			switch (lstData.get(position).id) {
			case WHEN:
				btn.setText(Html.fromHtml(str1+mCtxt.getString(R.string.when)+str2 + mData.getWhen().getString()));
				break;
			case WHERE:
				btn.setText(Html.fromHtml(str1+mCtxt.getString(R.string.where)+str2 + mData.getWhere()));
				LOG.D("where",""+mData.getWhere());
				break;
			case WHAT:
				btn.setHint(R.string.where);
				btn.setText(Html.fromHtml(str1 + mCtxt.getString(R.string.what) +str2+ mData.getWhat()));
				break;
			case NOTIFICATION:
				if (mData.getRing() != null) {
					Uri ring = Uri.parse(mData.getRing());
					Cursor cursor = mCtxt.getContentResolver().query(ring,
							new String[] { MediaStore.Audio.Media.TITLE },
							null, null, null);
					if (cursor != null){
						cursor.moveToFirst();
						if(!cursor.isNull(0)) {
							btn.setText("" + cursor.getString(0));
						}else{
							btn.setText(R.string.ring);
						}
							
					}else{
						btn.setText(R.string.sdcardmis);
					}
				} else {
					btn.setText(R.string.ring);
				}
				break;
			case REPEAT:
				btn.setText("" + mData.getRepeatCategory().getDesc());
				break;		
			case SEARCH:
				btn.setText(Html.fromHtml(str1+mCtxt.getString(R.string.search)+str2+mData.getWhat()));
				break;
			}
		} else {
			btn.setText(mCtxt.getString(android.R.string.unknownName));
		}
		btn.setTextSize(20);
		btn.setTextColor(Color.WHITE);
		btn.setShadowLayer(10, 2, 2, Color.RED);
		btn.setOnClickListener(mCtxt);
		btn.setBackgroundResource(R.drawable.transluent);
		btn.setId(lstData.get(position).id);
		return btn;
	}

	public static final String str1 = "<span><font color=\"#ff0000\">";
	public static final String str2 = "?   </font></span>";
	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return lstData.size() == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	public void reset() {
		mLayer = 0;
		mData = null;
		lstData.clear();
	}

	public boolean collapse() {
		if (mLayer >= 1) {
			mLayer--;
			update();
			return true;
		}
		return false;
	}

	

}
