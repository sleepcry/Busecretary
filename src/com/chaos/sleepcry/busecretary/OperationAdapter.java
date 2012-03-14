package com.chaos.sleepcry.busecretary;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;

import com.chaos.sleepcry.busecretary.notify.NotificationData;

public class OperationAdapter implements ListAdapter {

	public static final int WHEN = 0;
	public static final int WHERE = 1;
	public static final int RING = 2;
	public static final int REPEAT = 3;
	public static final int DESC = 4;
	public static final int ABOUT = 5;
	public static final int CONFIGURE = 6;
	public static final int WEATHER = 7;
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
		layer0.add(new InfoObj("when",WHEN));
		layer0.add(new InfoObj("where",WHERE));
		layer0.add(new InfoObj("ring",RING));
		layer0.add(new InfoObj("repeat",REPEAT));
		layer0.add(new InfoObj("desc",DESC));
		lstAllData.add(layer0);
		ArrayList<InfoObj> layer1 = new ArrayList<InfoObj>();
		layer1.add(new InfoObj("weather",WEATHER));
		layer1.add(new InfoObj("about the keyword",ABOUT));
		lstAllData.add(layer1);
		ArrayList<InfoObj> layer2 = new ArrayList<InfoObj>();
		layer2.add(new InfoObj("configure",CONFIGURE));
		lstAllData.add(layer2);

	}

	public void setData(NotificationData data) {
		mData = data;
	}

	private class InfoObj {
		public String content;
		public int id;

		public InfoObj(String c, int i) {
			content = c;
			id = i;
		}
	}

	public void fetchmore() {
		if (mLayer < lstAllData.size()) {
			mLayer++;
			update();
		}
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
		Button btn = new Button(mCtxt);
		if (mData != null) {
			switch (lstData.get(position).id) {
			case WHEN:
				btn.setText(mData.getDay().getString());
				break;
			case WHERE:
				btn.setText(lstData.get(position).content);
				break;
			case RING:
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
				btn.setText("" + mData.getCategory().getDesc());
				break;
			case DESC:
				btn.setHint(R.string.deschint);
				btn.setText(mData.getDesc());
				break;
			case WEATHER:
				btn.setText(lstData.get(position).content);
				break;
			case ABOUT:
				btn.setText(lstData.get(position).content);
				break;
			case CONFIGURE:
				btn.setText(lstData.get(position).content);
				break;
			}
		} else {
			btn.setText(lstData.get(position).content);
		}
		btn.setTextSize(20);
		btn.setBackgroundColor(Color.WHITE);
		btn.setShadowLayer(10, 2, 2, 0xff7fff7f);
		btn.setOnClickListener(mCtxt);
		btn.setId(lstData.get(position).id);
		return btn;
	}

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
		lstData.clear();
	}

	public void collapse() {
		if (mLayer >= 1) {
			mLayer--;
			update();
		}
	}

}
