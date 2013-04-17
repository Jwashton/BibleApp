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
public class DailyVerseDatabase extends SQLiteOpenHelper {

	static final String dbName="DailyVerseDB";
	static final int dbDailyVerseVersion = 1; // Incrementing database version will trigger the onUpdate() method
	static final String dailyVerseTable="DailyVerse";
	static final String colDayNumber="DayNumber";
	static final String colRefNo="ReferenceNumber";
	static final String colLastSeenDate = "LastSeenDate";
	
	/**
	 * Constructor for database instance calling super 
	 * with name, default cursor, and version number.
	 * 
	 */
	public DailyVerseDatabase(Context context) {
		super(context, dbName, null, dbDailyVerseVersion);
	}

	/**
	 * Required method that is called on database creation. Sets up 
	 * table schema.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create Daily Verse Table with schema:
		//
		// dailyVerseTable(_colDayNumber_, ReferenceNumber, LastSeenDate)
		 db.execSQL("CREATE TABLE "+ dailyVerseTable +" ("+ colDayNumber + " INTEGER PRIMARY KEY , "+
			    colRefNo + " INTEGER ," + colLastSeenDate + "TEXT )");
	}
	
	/**
	 * Required method that is called on version change in database. Calls
	 * onCreate to re-create table schema.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+ dailyVerseTable);
		onCreate(db);
	}
}
