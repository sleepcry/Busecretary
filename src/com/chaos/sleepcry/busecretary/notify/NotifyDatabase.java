package com.chaos.sleepcry.busecretary.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import android.provider.MediaStore.Images.Media;

import com.chaos.sleepcry.busecretary.Day;
import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.RepeatCategory;

public class NotifyDatabase extends SQLiteOpenHelper {

	private static final String TBL_NAME = "bs_db"; // the table name
	private static final String CURRENT_RECORD = "cur_rcd"; // the table name
	private static final String ID = "id"; // the column name of the id
	public static final int MODE_NEXT = 1;
	public static final int MODE_PREVIOUS = -1;
	public static final String WHAT = "desc";
	public static final String RING = "ring";
	public static final String CATEGORY = "category";
	public static final String WHEN = "day";
	public static final String BMP = "bmp";
	public static final String WHERE = "wer";

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
				+ ID + " integer primary key DESC," + WHEN
				+ " integer not null," + WHAT + " text," + RING + " text,"
				+ CATEGORY + " integer not null," + BMP + " text," + WHERE
				+ " text" + ")";
		db.execSQL(sqlCreateTbl);
		sqlCreateTbl = "create table if not exists " + CURRENT_RECORD + "("
				+ ID + " integer primary key DESC" + ")";
		db.execSQL(sqlCreateTbl);

	}

	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// db.delete(TBL_NAME, null, null);
	}

	/*
	 * this function need to be confirm/rewrite each time upgrade database
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			// get current exist ones
			List<NotificationData> existent = query(System.currentTimeMillis(),db);
			// create new table
			db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
			onCreate(db);
			// write old data to new one
			for (int i = 0; existent!= null&&i < existent.size(); i++) {
				NotificationData data = existent.get(i);
				insert(data.getId(), data.getWhen().getCalendar()
						.getTimeInMillis(), data.getWhat(), data.getRing(),
						data.getRepeatCategory().getId(), null, data.getWhere(),db);
				ContentValues cv = new ContentValues();
				cv.put(BMP, data.getBmpPath());
				db.update(TBL_NAME, cv, ID + "=" + data.getId(), null);
			}
		}
	}
	public void insert(int id, long time, String desc, String ring,
			int category, Bitmap bmp, String where) {
		SQLiteDatabase db = getWritableDatabase();
		insert(id,time,desc,ring,category,bmp,where,db);
		db.close();
	}
	/*
	 * @{ user functions
	 */
	public void insert(int id, long time, String desc, String ring,
			int category, Bitmap bmp, String where,SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put(ID, id);
		cv.put(WHEN, time);
		if (desc != null) {
			cv.put(WHAT, desc);
		}
		if (where != null) {
			cv.put(WHERE, where);
		}
		cv.put(RING, ring);
		cv.put(CATEGORY, category);
		if (bmp != null) {
			File file = new File(mContext.getExternalFilesDir(null), "img" + id
					+ ".png");
			if (file != null && file.exists()) {
				file.delete();
				file = new File(mContext.getExternalFilesDir(null), "img" + id
						+ ".png");
			}
			try {
				FileOutputStream output = new FileOutputStream(file);
				bmp.compress(Bitmap.CompressFormat.PNG, 0, output);
				output.close();
			} catch (IOException e) {
				LOG.W("ExternalStorage", "Error writing " + file);
			}
			try {
				Media.insertImage(mContext.getContentResolver(),
						file.getAbsolutePath(), file.getName(), file.getName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			cv.put(BMP, file.getAbsolutePath());
		}
		// check if records with this id exist
		Cursor c = db.query(TBL_NAME, new String[] { ID }, ID + "=" + id, null,
				null, null, null);

		if (c != null && c.getCount() != 0) {
			// if find the same record,update the record
			db.update(TBL_NAME, cv, ID + "=" + id, null);
		} else {
			// if find no record,insert a new one
			db.insert(TBL_NAME, null, cv);
		}
	}

	private void clear(long time,SQLiteDatabase db) {
		// delete the records whose time flag is earlier than the given time
		db.delete(TBL_NAME, WHEN + "<" + time + " AND " + CATEGORY + " = "
				+ RepeatCategory.NONE.getId(), null);
	}
	public List<NotificationData> query(long time){
		SQLiteDatabase db = this.getReadableDatabase();
		List<NotificationData> lstDatas = query(time,db);
		db.close();
		return lstDatas;
	}
	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TBL_NAME, ID+"="+id, null);
		db.close();
		
	}
	public List<NotificationData> query(long time,SQLiteDatabase db) {
		clear(time,db);
		Cursor c = db.query(TBL_NAME, null, null, null, null, null, null);
		if (c == null || c.getCount() == 0) {
			return null;
		}
		c.moveToFirst();
		List<NotificationData> lstRet = new ArrayList<NotificationData>();
		int loc = 0;
		do {
			NotificationData n = new NotificationData();
			Calendar cal = Calendar.getInstance();
			int index = c.getColumnIndex(WHEN);
			if (index != -1) {
				cal.setTimeInMillis(c.getLong(index));
				n.setWhen(new Day(cal));
			}
			index = c.getColumnIndex(WHAT);
			if (index != -1) {
				n.setWhat(c.getString(index));
			}
			index = c.getColumnIndex(RING);
			if (index != -1) {
				n.setRing(c.getString(index));
			}
			index = c.getColumnIndex(CATEGORY);
			if (index != -1) {
				n.setRepeatCategory(RepeatCategory.getInstance(c.getInt(index)));
			}
			// update the mId according to the records from the database
			index = c.getColumnIndex(ID);
			if (index != -1) {
				n.setId(c.getInt(index));
			}
			n.setLocation(loc++);
			index = c.getColumnIndex(BMP);
			if (index != -1) {
				n.setBmpPath(c.getString(c.getColumnIndex(BMP)));
			}
			index = c.getColumnIndex(WHERE);
			if (index != -1) {
				n.setWhere(c.getString(index));
			}
			lstRet.add(n);
		} while (c.moveToNext());
		return lstRet;
	}

	public NotificationData queryone(int id) {
		String query_condition = ID + "=" + id;
		SQLiteDatabase db = this.getReadableDatabase();
		clear(System.currentTimeMillis(),db);
		Cursor c = db.query(TBL_NAME, null, query_condition, null, null, null,
				null);
		if (c == null || c.getCount() == 0) {
			db.close();
			return null;
		}
		c.moveToFirst();
		NotificationData n = new NotificationData();
		int index = c.getColumnIndex(WHEN);
		if (index != -1) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(c.getLong(index));
			n.setWhen(new Day(cal));
		}
		index = c.getColumnIndex(WHAT);
		if (index != -1) {
			n.setWhat(c.getString(index));
		}
		index = c.getColumnIndex(RING);
		if (index != -1) {
			n.setRing(c.getString(index));
		}
		index = c.getColumnIndex(CATEGORY);
		if (index != -1) {
			n.setRepeatCategory(RepeatCategory.getInstance(c.getInt(index)));
		}
		// update the mId according to the records from the database
		n.setId(id);
		index = c.getColumnIndex(BMP);
		if (index != -1) {
			n.setBmpPath(c.getString(c.getColumnIndex(BMP)));
		}
		index = c.getColumnIndex(WHERE);
		if (index != -1) {
			n.setWhere(c.getString(index));
		}
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
