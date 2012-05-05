package com.chaos.sleepcry.busecretary;

import java.util.Calendar;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class DatePickerView extends LinearLayout {
	Calendar mDate = null;
	WheelView wv_hour = null;
	WheelView wv_minute = null;
	WheelView wv_day = null;
	WheelView wv_month = null;
	int currentMaxDay = 0;

	public DatePickerView(Context context, long timeInMillis) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.datepicker, this);
		mDate = Calendar.getInstance();
		mDate.setTimeInMillis(timeInMillis);
		wv_hour = (WheelView) findViewById(R.id.hour);
		setAdapter(wv_hour, context, 0, 23);
		if (mDate.get(Calendar.AM_PM) == Calendar.PM) {
			wv_hour.setCurrentItem(mDate.get(Calendar.HOUR)+12);
		}else {
			wv_hour.setCurrentItem(mDate.get(Calendar.HOUR));
		}

		wv_minute = (WheelView) findViewById(R.id.minute);
		setAdapter(wv_minute, context, 0, 59);
		wv_minute.setCurrentItem(mDate.get(Calendar.MINUTE));

		wv_month = (WheelView) findViewById(R.id.month);
		setAdapter(wv_month, context, 1, 12);
		wv_month.setCurrentItem(mDate.get(Calendar.MONTH));
		wv_month.addScrollingListener(new OnWheelScrollListener() {

			@Override
			public void onScrollingStarted(WheelView wheel) {
				mDate.set(Calendar.DAY_OF_MONTH, wv_day.getCurrentItem());
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				mDate.set(Calendar.MONTH, wv_month.getCurrentItem());
				if (currentMaxDay != mDate
						.getActualMaximum(Calendar.DAY_OF_MONTH)) {
					currentMaxDay = mDate
							.getActualMaximum(Calendar.DAY_OF_MONTH);
					setAdapter(wv_day, getContext(), 1, currentMaxDay);
					if (wv_day.getCurrentItem() >= currentMaxDay) {
						wv_day.setCurrentItem(currentMaxDay - 1);
					}
				}
			}

		});

		wv_day = (WheelView) findViewById(R.id.day);
		currentMaxDay = mDate.getActualMaximum(Calendar.DAY_OF_MONTH);
		setAdapter(wv_day, context, 1, currentMaxDay);
		wv_day.setCurrentItem(mDate.get(Calendar.DAY_OF_MONTH) - 1);
	}

	private void setAdapter(WheelView wv, Context context, int min, int max) {
		NumericWheelAdapter adapter = new NumericWheelAdapter(context, min, max);
		adapter.setTextSize(30);
		wv.setViewAdapter(adapter);
		wv.setVisibleItems(3);
	}

	public long getTime() {
		mDate.set(Calendar.MONTH, wv_month.getCurrentItem());
		mDate.set(Calendar.DAY_OF_MONTH, wv_day.getCurrentItem() + 1);
		mDate.set(Calendar.HOUR_OF_DAY, wv_hour.getCurrentItem());
		mDate.set(Calendar.MINUTE, wv_minute.getCurrentItem());
		return mDate.getTimeInMillis();
	}
}
