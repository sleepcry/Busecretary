package utils;

import android.graphics.PointF;

public class MathUtils {
	public static double PIRADIAN = Math.PI/180.0;
	/*
	 * return the angle in the range of [0,2PI], unit radian
	 */
	public static double getAngle(PointF start, PointF end) {
		PointF pt = new PointF(end.x - start.x,end.y-start.y);
		double dst = modular(pt);
		if(pt.x >=0 && pt.y >= 0) {
			//the first quadrant
			return Math.asin(pt.x/dst);
		}else if(pt.x <0 && pt.y >= 0) {
			//the second quadrant
			return Math.PI-Math.asin(pt.x/dst);
		}else if(pt.x <0 && pt.y < 0) {
			//the third quadrant
			return Math.PI + Math.atan(pt.y/pt.x);
		}else if(pt.x >= 0 && pt.y < 0) {
			//the fourth quadrant
			return 2*Math.PI - Math.asin(pt.x/dst);
		}else {
			//should never happen
			assert(false);
			return 0;
		}
	}
	public static double dst(PointF start, PointF end) {
		return Math.sqrt(Math.pow(end.x-start.x, 2) + Math.pow(end.y-start.y, 2));
	}
	public static double modular(PointF pt) {
		return Math.sqrt(Math.pow(pt.x, 2) + Math.pow(pt.y, 2));
	}
}
