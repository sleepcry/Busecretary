package com.chaos.sleepcry;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BsWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		RemoteViews rvs = new RemoteViews(context.getPackageName(), R.layout.widget_bs);
        Intent intentClick = new Intent(context,BusecretaryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intentClick, 0);
        rvs.setOnClickPendingIntent(R.id.btn_widget_previous, pendingIntent);
        rvs.setOnClickPendingIntent(R.id.btn_widget_next, pendingIntent);
        rvs.setOnClickPendingIntent(R.id.tv_widget_content, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, rvs);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	}
}
