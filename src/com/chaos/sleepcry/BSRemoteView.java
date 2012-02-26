package com.chaos.sleepcry;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class BSRemoteView extends RemoteViews {
	private static final String PACKAGE = "com.chaos.sleepcry";
	public static final String ACTION_NEXT = "com.chaos.sleepcry.NEXT";
	public static final String ACTION_PREVIOUS = "com.chaos.sleepcry.PREVIOUS";
	
	public BSRemoteView(Context context,int curid) {
		super(PACKAGE, R.layout.widget_bs);
		//set the event handler of clicking the middle
		Intent intent = new Intent(context, BusecretaryActivity.class);
		intent.putExtra(BusecretaryActivity.NOTI_ID, curid);
		PendingIntent pendingIntent = PendingIntent.getActivity(context,
				(int)System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		setOnClickPendingIntent(R.id.tv_widget_content, pendingIntent);
		//click the next
		Intent next = new Intent(ACTION_NEXT);
		next.putExtra(BusecretaryActivity.NOTI_ID, curid + 1);
		Log.d("widget","next id is:" + (curid + 1));
		pendingIntent = PendingIntent.getBroadcast(context,
				(int)System.currentTimeMillis(), next, PendingIntent.FLAG_UPDATE_CURRENT);
		setOnClickPendingIntent(R.id.btn_widget_next, pendingIntent);
		//click the previous
		Intent previous = new Intent(ACTION_PREVIOUS);
		previous.putExtra(BusecretaryActivity.NOTI_ID, curid-1);
		Log.d("widget","previous id is:" + (curid - 1));
		pendingIntent = PendingIntent.getBroadcast(context,
				(int)System.currentTimeMillis(), previous, PendingIntent.FLAG_UPDATE_CURRENT);
		setOnClickPendingIntent(R.id.btn_widget_previous, pendingIntent);
	}
	
}
