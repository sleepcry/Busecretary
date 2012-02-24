package com.chaos.sleepcry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class NotifyDatabase extends SQLiteOpenHelper {

	private static final String TBL_NAME = "bs_db";	//the table name
	private static final String ID = "id";	//the column name of the id
	
	public NotifyDatabase(Context context,int version) {
		super(context, TBL_NAME, null, version);
	}
	
	/*@{
	 * override functions
	 */
	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 * 
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreateTbl = "create table if not exists " + TBL_NAME + "(" + 
		ID + " integer primary key DESC," + 
		BusecretaryActivity.DAY + " integer not null," + 
		BusecretaryActivity.DESC + " text," + 
		BusecretaryActivity.RING + " text," + 
		BusecretaryActivity.CATEGORY + " integer not null" + 
		")";
		db.execSQL(sqlCreateTbl);
		
	}
	public void onOpen(SQLiteDatabase db){
		super.onOpen(db);
		//db.delete(TBL_NAME, null, null);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TBL_NAME);
			onCreate(db);
		}
	}
	
	/*@{
	 * user functions
	 */
	public void insert(int id,long time, String desc, String ring, int category){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(ID, id);
		cv.put(BusecretaryActivity.DAY, time);
		if (desc != null) {
			cv.put(BusecretaryActivity.DESC, desc);
		}
		cv.put(BusecretaryActivity.RING, ring);
		cv.put(BusecretaryActivity.CATEGORY, category);
		//check if records with this id exist
		Cursor c = db.query(TBL_NAME, new String[]{ID}, ID + "=" + id, null, null, null, null);
		
		if(c != null && c.getCount() != 0){
			//if find the same record,update the record
			db.update(TBL_NAME, cv, ID + "=" + id, null);
		}else{
			//if find no record,insert a new one
			db.insert(TBL_NAME, null, cv);
		}
	}
	private void clear(long time){
		//delete the records whose time flag is earlier than the given time
		SQLiteDatabase db = getWritableDatabase();
		db.delete(TBL_NAME, BusecretaryActivity.DAY + "<" + time + " AND " + 
				BusecretaryActivity.CATEGORY + " = " + RepeatCategory.NONE.getId(), null);
	}
	public List<BusecretaryActivity.NotificationData> query(long time){
		clear(time);
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null,
				null, null, null);
		if(c == null || c.getCount() == 0){
			return null;
		}
		c.moveToFirst();
		List<BusecretaryActivity.NotificationData> lstRet = 
			new ArrayList<BusecretaryActivity.NotificationData>();
		int loc = 0;
		do{
			BusecretaryActivity.NotificationData n = new BusecretaryActivity.NotificationData();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(c.getLong(c.getColumnIndex(BusecretaryActivity.DAY)));
			n.day = new Day(cal);
			n.desc = c.getString(c.getColumnIndex(BusecretaryActivity.DESC));
			n.ring = c.getString(c.getColumnIndex(BusecretaryActivity.RING));
			n.category = RepeatCategory.getInstance(
					c.getInt(c.getColumnIndex(BusecretaryActivity.CATEGORY)));
			//update the mId according to the records from the database
			n.mId = c.getInt(c.getColumnIndex(ID));
			n.location = loc ++;
			lstRet.add(n);
		}while(c.moveToNext());
		return lstRet;
	}

}
