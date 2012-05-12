package com.chaos.sleepcry.busecretary.notify;

import java.util.Date;

import utils.LOG;

import com.chaos.sleepcry.busecretary.BusecretaryActivity;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.widget.BSRemoteView;
import com.chaos.sleepcry.busecretary.widget.BsWidget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotifyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent intent) {
		Bundle bundle = intent.getExtras();
		if(null == bundle){
			return;
		}
		String action = intent.getAction();
		if(action != null){
			Log.d("widget",action);
		}
		Log.d("widget","receiver context " + c.toString());
		if(action != null && (action.equals(BSRemoteView.ACTION_NEXT) ||
				action.equals(BSRemoteView.ACTION_PREVIOUS))){
			int id = bundle.getInt(BusecretaryActivity.NOTI_ID,-1);
			if (id != -1) {
				NotifyDatabase db = new NotifyDatabase(c,
						BusecretaryActivity.DB_VER);
				NotificationData data = db.queryone(id);
				if (null == data) {
					return;
				}
				db.setCurRcd(id);
				Log.d("widget","set id into:" + id);
				BSRemoteView view = new BSRemoteView(c, id);
				view.setTextViewText(R.id.tv_widget_content,data.getWhat());
				AppWidgetManager appWidgetManager = AppWidgetManager
						.getInstance(c);
				int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(
						c, BsWidget.class));
				for (int i = 0; i < ids.length; i++) {
					int appWidgetId = ids[i];
					appWidgetManager.updateAppWidget(appWidgetId, view);
					Log.d("widget","update remote view " + ids[i]);
				}
			}
		}else{
			String strWhat = intent.getExtras().getString(NotifyDatabase.WHAT);
			long when = intent.getExtras().getLong(NotifyDatabase.WHEN);
			String strWhere = intent.getExtras().getString(NotifyDatabase.WHERE);
			String strUri = intent.getExtras().getString(NotifyDatabase.RING);
			String strBmp = intent.getExtras().getString(NotifyDatabase.BMP);
			LOG.D("notification", "receiver what?:"+strWhat);
			LOG.D("notification", "receiver when?"+new Date(when).toGMTString());
			LOG.D("notification", "receiver where?"+strWhere);
			LOG.D("notification", "receiver ring?"+strUri);
			LOG.D("notification", "receiver bmp?"+strBmp);
			Intent intent2 = new Intent(c, NotifyActivity.class);
			intent2.putExtras(intent.getExtras());
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			c.startActivity(intent2);
		}
	}

}
