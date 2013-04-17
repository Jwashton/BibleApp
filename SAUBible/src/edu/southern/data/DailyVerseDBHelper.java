package edu.southern.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Daily Verse Database for storing daily verse references. 
 * Daily verse used by HomeScreen.
 * 
 * @author Nathanael
 */
public class DailyVerseDBHelper {
	
	private DailyVerseDatabase dailyVerseDB;
	
	public DailyVerseDBHelper(Context context) {
		dailyVerseDB = new DailyVerseDatabase(context);
	}
	
	int getDailyVerse() {
		
		
		return -1;
	}
	
	
	
}
