package com.chaos.sleepcry.busecretary.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chaos.sleepcry.busecretary.BusecretaryActivity;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.notify.NotificationData;
import com.chaos.sleepcry.busecretary.notify.NotifyDatabase;

public class BsWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			BSRemoteView rvs = new BSRemoteView(context,0);
			Log.d("widget","update context " + context.toString());			
			NotifyDatabase db = new NotifyDatabase(context,BusecretaryActivity.DB_VER);
			NotificationData data = db.queryone(0);
			if(null != data){
				rvs.setTextViewText(R.id.tv_widget_content, data.getWhat());
			}else{
				rvs.setTextViewText(R.id.tv_widget_content, "add something here...");
			}
			appWidgetManager.updateAppWidget(appWidgetId, rvs);
			Log.d("widget","update remote view " + appWidgetId);
		}
	}
}
