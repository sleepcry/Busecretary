package utils;

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
}
