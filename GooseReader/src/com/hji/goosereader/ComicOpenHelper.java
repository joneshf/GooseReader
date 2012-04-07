package com.hji.goosereader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ComicOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "comics.db";
	private static final String TABLE_COMICS = "comics";
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_NUMBER = "_number";
	private static final String COLUMN_URL = "_url";
	private static final String COLUMN_TITLE = "_title";
	private static final String COLUMN_TEXT = "_text";
	// Statement used to create the database.
	private static final String DATABASE_CREATE = "create table " +
			TABLE_COMICS + "( " +
			COLUMN_ID +	" integer primary key autoincrement, " +
			COLUMN_NUMBER + " text not null, " +
			COLUMN_URL + " text not null, " +
			COLUMN_TITLE + " text not null, " +
			COLUMN_TEXT + " text not null);"; 
	
	public ComicOpenHelper(Context context) {
		// Pass this info to the super class.
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the database.
		db.execSQL(DATABASE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Log the output of this.
		Log.w(ComicOpenHelper.class.getName(), "Upgrading from version " + oldVersion +
				"to version " + newVersion + ".  This will destroy data.");

		// Drop the old table.
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMICS);
		// Make a new table.
		onCreate(db);
	}

}