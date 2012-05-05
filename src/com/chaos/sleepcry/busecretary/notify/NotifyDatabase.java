package com.chaos.sleepcry.busecretary.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import utils.LOG;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.chaos.sleepcry.busecretary.Day;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.RepeatCategory;

public class NotifyDatabase extends SQLiteOpenHelper {

	private static final String TBL_NAME = "bs_db"; // the table name
	private static final String CURRENT_RECORD = "cur_rcd"; // the table name
	private static final String ID = "id"; // the column name of the id
	public static final int MODE_NEXT = 1;
	public static final int MODE_PREVIOUS = -1;
	public static final String DESC = "desc";
	public static final String RING = "ring";
	public static final String CATEGORY = "category";
	public static final String DAY = "day";
	public static final String BMP = "bmp";

	Context mContext = null;

	public NotifyDatabase(Context context, int version) {
		super(context, TBL_NAME, null, version);
		mContext = context;
	}

	/*
	 * @{ override functions
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreateTbl = "create table if not exists " + TBL_NAME + "("
				+ ID + " integer primary key DESC," + DAY
				+ " integer not null," + DESC + " text," + RING + " text,"
				+ CATEGORY + " integer not null," + BMP + " text" + ")";
		db.execSQL(sqlCreateTbl);
		sqlCreateTbl = "create table if not exists " + CURRENT_RECORD + "("
				+ ID + " integer primary key DESC" + ")";
		db.execSQL(sqlCreateTbl);

	}

	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// db.delete(TBL_NAME, null, null);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
			onCreate(db);
		}
	}

	/*
	 * @{ user functions
	 */
	public void insert(int id,long time, String desc, String ring, int category,Bitmap bmp){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ID, id);
		cv.put(DAY, time);
		if (desc != null) {
			cv.put(DESC, desc);
		}
		cv.put(RING, ring);
		cv.put(CATEGORY, category);
		if (bmp != null) {
			File file = new File(mContext.getExternalFilesDir(null), "img" + id
					+ ".png");
			try {
				FileOutputStream output = new FileOutputStream(file);
				bmp.compress(Bitmap.CompressFormat.PNG, 0, output);
				output.close();
			} catch (IOException e) {
				LOG.W("ExternalStorage", "Error writing " + file);
			}

			cv.put(BMP, file.getAbsolutePath());
		}else {
			cv.put(BMP, "");			
		}
		//check if records with this id exist
		Cursor c = db.query(TBL_NAME, new String[]{ID}, ID + "=" + id, null, null, null, null);
		
		if(c != null && c.getCount() != 0){
			//if find the same record,update the record
			db.update(TBL_NAME, cv, ID + "=" + id, null);
		}else{
			//if find no record,insert a new one
			db.insert(TBL_NAME, null, cv);
		}
		db.close();
	}

	private void clear(long time) {
		// delete the records whose time flag is earlier than the given time
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TBL_NAME, DAY + "<" + time + " AND " + CATEGORY + " = "
				+ RepeatCategory.NONE.getId(), null);
		db.close();
	}

	public List<NotificationData> query(long time) {
		clear(time);
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null, null, null, null);
		if (c == null || c.getCount() == 0) {
			db.close();
			return null;
		}
		c.moveToFirst();
		List<NotificationData> lstRet = new ArrayList<NotificationData>();
		int loc = 0;
		do {
			NotificationData n = new NotificationData();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(c.getLong(c.getColumnIndex(DAY)));
			n.setDay(new Day(cal));
			n.setDesc(c.getString(c.getColumnIndex(DESC)));
			n.setRing(c.getString(c.getColumnIndex(RING)));
			n.setCategory(RepeatCategory.getInstance(c.getInt(c
					.getColumnIndex(CATEGORY))));
			// update the mId according to the records from the database
			n.setId(c.getInt(c.getColumnIndex(ID)));
			n.setLocation(loc++);
//			ByteArrayInputStream input = new ByteArrayInputStream(c.getBlob(c
//					.getColumnIndex(BMP)));
//			Bitmap bmp = null;
//			try {
//				bmp = BitmapFactory.decodeStream(input);
//			} catch (Exception e) {
//				bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
//			}
			n.setBmpPath(c.getString(c.getColumnIndex(BMP)));
			lstRet.add(n);
		} while (c.moveToNext());
		db.close();
		return lstRet;
	}

	public NotificationData queryone(int id) {
		clear(System.currentTimeMillis());
		String query_condition = ID + "=" + id;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TBL_NAME, null, query_condition, null, null, null,
				null);
		if (c == null || c.getCount() == 0) {
			db.close();
			return null;
		}
		c.moveToFirst();
		NotificationData n = new NotificationData();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(c.getLong(c.getColumnIndex(DAY)));
		n.setDay(new Day(cal));
		n.setDesc(c.getString(c.getColumnIndex(DESC)));
		n.setRing(c.getString(c.getColumnIndex(RING)));
		n.setCategory(RepeatCategory.getInstance(c.getInt(c
				.getColumnIndex(CATEGORY))));
		// update the mId according to the records from the database
		n.setId(c.getInt(c.getColumnIndex(ID)));
		n.setBmpPath(c.getString(c.getColumnIndex(BMP)));
		db.close();
		return n;
	}

	public int getCurRcd() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(CURRENT_RECORD, null, null, null, null, null, null);
		if (c == null || c.getCount() == 0) {
			db.close();
			return -1;
		}
		c.moveToFirst();
		db.close();
		return c.getInt(c.getColumnIndex(ID));
	}

	public void setCurRcd(int id) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ID, id);
		db.update(CURRENT_RECORD, cv, null, null);
		db.close();
	}

	public int getMaxId() {
		String selection = "max(" + ID + ")";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TBL_NAME, new String[] { selection }, null, null,
				null, null, null);
		if (c == null || c.getCount() == 0) {
			db.close();
			return -1;
		}
		c.moveToFirst();
		db.close();
		return c.getInt(c.getColumnIndex(selection));
	}
}
