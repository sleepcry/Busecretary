package com.chaos.sleepcry;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

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
				rvs.setTextViewText(R.id.tv_widget_content, data.getDesc());
			}else{
				rvs.setTextViewText(R.id.tv_widget_content, "add something here...");
			}
			appWidgetManager.updateAppWidget(appWidgetId, rvs);
			Log.d("widget","update remote view " + appWidgetId);
		}
	}
}
