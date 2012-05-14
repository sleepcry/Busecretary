package com.chaos.sleepcry.busecretary.mydraw;

import com.chaos.sleepcry.busecretary.utils.LOG;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeShuffle implements SensorEventListener {
	private SensorManager sensorMgr;
	private long lastUpdate = -1;
	private static final int SHAKE_THRESHOLD_LEFT = 800;
	private static final int SHAKE_THRESHOLD_RIGHT = -600;
	Sensor mAccSensor = null;

	public ShakeShuffle(Context c) {
		sensorMgr = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		mAccSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	public void pause() {
		if (sensorMgr != null) {
			sensorMgr.unregisterListener(this);
		}
	}

	public void start() {
		if (!sensorMgr.registerListener(this, mAccSensor,
				SensorManager.SENSOR_DELAY_GAME)) {
			sensorMgr.unregisterListener(this);
		}
		LOG.D("music","start");
	}

	public static interface ShakeShuffleListener {
		public void onShakeLeft();

		public void onShakeRight();
	}

	private ShakeShuffleListener mListener;

	public void setShakeShuffleListener(ShakeShuffleListener l) {
		mListener = l;
	}

	boolean bShaking = false;
	private int free_frame_cnt = 0;

	@Override
	public void onSensorChanged(SensorEvent event) {
		final float[] values = event.values;
		final Sensor sensor = event.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > 100) {
				lastUpdate = curTime;
				// must be x-oriented
				if ((Math.abs(values[0]) < Math.abs(values[1]) || (Math
						.abs(values[0]) < Math.abs(values[2])))) {
					free_frame_cnt++;
					// clear status every 0.3 seconds
					if (free_frame_cnt >= 3) {
						mDirection = Direction.UNDETERMINED;
						free_frame_cnt = 0;
					}
					return;
				}
				float speed = values[0] * 100;
//				LOG.D("shake", " speed:" + speed);
				if (speed < SHAKE_THRESHOLD_RIGHT && mListener != null) {
					if (mDirection == Direction.UNDETERMINED) {
						mDirection = Direction.RIGHT;
						mListener.onShakeRight();
//						LOG.D("shake", " enter right");
						free_frame_cnt = 0;
					}
				} else if (speed > SHAKE_THRESHOLD_LEFT && mListener != null) {
					if (mDirection == Direction.UNDETERMINED) {
						mListener.onShakeLeft();
						mDirection = Direction.LEFT;
//						LOG.D("shake", "enter left");
						free_frame_cnt = 0;
					}
				} 
			}
		}
	}

	public enum Direction {
		LEFT, RIGHT, UNDETERMINED
	}

	private Direction mDirection = Direction.UNDETERMINED;

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
}
