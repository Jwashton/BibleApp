package edu.southern.data;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Daily Verse Database for storing daily verse references. 
 * Daily verse used by HomeScreen.
 * 
 * @author Nathanael
 */
public class DailyVerseDBHelper {
	
	private SQLiteDatabase dailyVerseDB;
	private static String tableName = "DailyVerse";

	/**
	 * Constructor for database. Creates and opens
	 * it for use.
	 */
	public DailyVerseDBHelper(Context context) {
		
		DailyVerseDB dvDB = new DailyVerseDB(context);
		
		// Create database (does nothing if database exists)
		try {
			dvDB.createDataBase();
		} 
		catch (IOException ioe) {
			throw new Error("Unable to create database");
		}

		// Attempt to open database
		try {
			dvDB.openDataBase();
		} 
		catch(SQLException sqle){
			throw sqle;
		}
		
		dailyVerseDB = dvDB.getWritableDatabase();
	}
	
	/**
	 * Gets the next verse that has not been seen from
	 * daily verse table. Checks the DateLastSeen attribute.
	 */
	public int getNextDailyVerse() {
		// Get cursor from query
		Cursor c = dailyVerseDB.rawQuery("SELECT _id, referenceNumber, dateLastSeen FROM DailyVerse ORDER BY dateLastSeen", new String[] {});
		c.moveToFirst();
		int dailyVerseReference = c.getInt(1);

		Log.d("Database", Integer.toString(dailyVerseReference));
		updateVerseLastSeen(dailyVerseReference);
		
		c.close();
		return dailyVerseReference;
	}

	/**
	 * Sets dateLastSeen on row given reference number
	 */
	public void updateVerseLastSeen(int refNumber) {
		dailyVerseDB.execSQL("UPDATE DailyVerse SET dateLastSeen=date('now') WHERE referenceNumber="
				.concat(Integer.toString(refNumber)));
	}
}
