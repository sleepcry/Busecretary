package com.chaos.sleepcry;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class NotifyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent intent) {
		//Intent intent2 = new Intent(c, NotifyService.class);
		Intent intent2 = new Intent(c, NotifyActivity.class);
		intent2.putExtras(intent.getExtras());
		//ComponentName name = c.startService(intent2);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		c.startActivity(intent2);
	}

}
