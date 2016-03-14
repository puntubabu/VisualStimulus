
package com.glassrehab.mainM1M2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class sqlAdapter {

	sqlHelper sqlHelper;
	public static Context context;

	public sqlAdapter(Context context) {
		this.context = context;
		sqlHelper = new sqlHelper(context);
	}

	/**
	 * Adds a user to the database.
	 *
	 * @param user The user's name.
	 * @param m1 Data for module 1.
	 * @param m2 Date for module 2.
	 * @param m3 Data for module 3.
	 */
	public void addUser(String user, String m1, String m2, String m3) {
		SQLiteDatabase database = sqlHelper.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(sqlHelper.NAME, user);
		contentValues.put(sqlHelper.m1, m1);
		contentValues.put(sqlHelper.m2, m2);
		contentValues.put(sqlHelper.m3, m3);
		long id = database.insert(sqlHelper.TABLE_NAME, null, contentValues);
		if (id < 0) {
			Toast.makeText(context, "Unsuccessully User Inserted ", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, this.getAllData(), Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * Retrieve all data from the database.
	 * 
	 * @return All the data form the databse in string format.
	 */
	public String getAllData() {
		SQLiteDatabase db = sqlHelper.getWritableDatabase();
		String [] col = {sqlHelper.UID, sqlHelper.NAME, sqlHelper.m1, sqlHelper.m2, sqlHelper.m3} ;

		Cursor cursor=db.query(sqlHelper.TABLE_NAME, col, null, null, null, null, null);
		StringBuffer buffer=new StringBuffer();
		while (cursor.moveToNext()) {
			int cid=cursor.getInt(0);
			String name= cursor.getString(1);
			String m1=cursor.getString(2);
			String m2=cursor.getString(3);
			String m3=cursor.getString(4);
			buffer.append(cid + " "+ name+ "\n"+ "m1:"+ m1+ "\n"+ "m2:"+ m2+ "\n"+ "m3:"+ m3+"\n");
		}
		return buffer.toString();
	}

	/**
	 * Select data according to the user's name.
	 *
	 * @param name The name of the user.
	 * @return Data for the user.
	 */
	public String selectData(String name) {
		SQLiteDatabase db = sqlHelper.getWritableDatabase();
		String [] col = {sqlHelper.NAME, sqlHelper.m1, sqlHelper.m2, sqlHelper.m3} ;
		String [] selectArg={name};
		Cursor cursor=db.query(sqlHelper.TABLE_NAME, col, sqlHelper.NAME + " =?",selectArg, null, null, null, null);
		StringBuffer buffer=new StringBuffer();

		// while the cursor can move from row to row
		while (cursor.moveToNext()) {
			int cid=cursor.getInt(0);
			int un=cursor.getColumnIndex(sqlHelper.NAME);
			int a=cursor.getColumnIndex(sqlHelper.m1);
			int b=cursor.getColumnIndex(sqlHelper.m2);
			int c=cursor.getColumnIndex(sqlHelper.m3);
			String uname= cursor.getString(un);
			String m1=cursor.getString(a);
			String m2=cursor.getString(b);
			String m3=cursor.getString(c);
			buffer.append(cid + " "+ name+ "\n"+ "m1:"+ m1+ "\n"+ "m2:"+ m2+ "\n"+ "m3:"+ m3+"\n");
		}
		return buffer.toString();
	}

	/**
	 * Update module 1 data for a specific user.
	 * 
	 * @param name The patient's name.
	 * @param m1 Module 1 data.
	 * @return The number of rows updated.
	 */
	public int updateM1(String name, String m1) {
		SQLiteDatabase db =sqlHelper.getWritableDatabase();
		ContentValues contentValues=new ContentValues();
		contentValues.put(sqlHelper.m1, m1);
		String [] whereArgs={name};
		int count= db.update(sqlHelper.TABLE_NAME, contentValues, sqlHelper.NAME+" =?", whereArgs);

		return count;
	}
	
	/**
	 * Updated module 2 data for a specific user.
	 * 
	 * @param name The patient's name.
	 * @param m2 Module 2 data.
	 * @return The number of rows updated.
	 */
	public int updateM2(String name, String m2) {
		SQLiteDatabase db =sqlHelper.getWritableDatabase();
		ContentValues contentValues=new ContentValues();
		contentValues.put(sqlHelper.m2, m2);
		String [] whereArgs={name};
		int count= db.update(sqlHelper.TABLE_NAME, contentValues, sqlHelper.NAME+" =?", whereArgs);

		return count;
	}
	
	/**
	 * Updated module 3 data for a specific user.
	 * 
	 * @param name The name of the patient.
	 * @param m1 Module 1 data.
	 * @return The number of rows udpated.
	 */
	public int updateM3(String name, String m1) {
		SQLiteDatabase db =sqlHelper.getWritableDatabase();
		ContentValues contentValues=new ContentValues();
		contentValues.put(sqlHelper.m3, m1);
		String [] whereArgs={name};
		int count= db.update(sqlHelper.TABLE_NAME, contentValues, sqlHelper.NAME+" =?", whereArgs);

		return count;
	}
	
	/**
	 * Delete rows regarding a patient name.
	 * 
	 * @param name The name of hte patient.
	 * @return The number of rows deleted.
	 */
	public int deleteRow(String name) {
		SQLiteDatabase db =sqlHelper.getWritableDatabase();
		ContentValues contentValues=new ContentValues();
		String [] whereArgs={name};
		int count= db.delete(sqlHelper.TABLE_NAME, sqlHelper.NAME+" =?", whereArgs);
		if (count<=0) {
			Toast.makeText(context, "No Name is deleted ", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, getAllData(), Toast.LENGTH_LONG).show();
		}
		return count;

	}
	
	/**
	 * sqlHelper helper class
	 */
	static class sqlHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "VSDATABASE";
		private static final String UID = "_id";
		private static final String NAME = "Name";
		private static final String SETTING = "Setting";
		private static final String TABLE_NAME = "VSTABLE";
		private static final int DATABASE_VERSION = 1;
		private static final String m1 = "m1";
		private static final String m2 = "m2";
		private static final String m3 = "m3";
		private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " VARCHAR(255), " + SETTING + " VARCHAR(255), "+ m1+  " VARCHAR(255), " + m2 + " VARCHAR(255), "+ m3 +" VARCHAR(255));";
		private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME ;

		public sqlHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			try {
				db.execSQL(this.CREATE_TABLE);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "OnCreate is called", Toast.LENGTH_LONG).show();
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			try {
				Toast.makeText(context, "onUpdate is called", Toast.LENGTH_LONG).show();
				db.execSQL(DROP_TABLE);
				onCreate(db);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "" + e, Toast.LENGTH_LONG).show();
				;
			}
		}

	}

}
