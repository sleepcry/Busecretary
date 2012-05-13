package utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.util.Log;

public class LOG {
	public static boolean enableLog = true;
	
	public static void D(String tag, String msg) {
		if(enableLog) {
			Log.d(tag,msg);
		}
	}
	public static void I(String tag, String msg) {
		if(enableLog) {
			Log.i(tag,msg);
		}
	}
	public static void W(String tag, String msg) {
		if(enableLog) {
			Log.w(tag,msg);
		}
	}
	public static void logMem(Context a) {
		if (enableLog) {
			MemoryInfo mi = new MemoryInfo();
			ActivityManager activityManager = (ActivityManager) a
					.getSystemService(Context.ACTIVITY_SERVICE);
			activityManager.getMemoryInfo(mi);
			long availableMegs = mi.availMem / 1024L;
			long thresholdMem = mi.threshold/1024L;
			D("memory", "available mem:"+availableMegs);
			D("memory", "threshold mem:"+thresholdMem);
		}
	}
}
