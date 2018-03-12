package com.example.android.pets.database_classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.pets.database_classes.PetContract.PetEntry;

/**
 * Created by trish on 3/4/2018.
 */

public class PetDBHelper extends SQLiteOpenHelper {
    /**
     +     * Database version. If you change the database schema, you must increment the database version.
     +     */
    SQLiteDatabase db;
    private static final int DATABASE_VERSION = 1;
    /** Name of the database file */
    private static final String DATABASE_NAME = "PetReader.db";
    private static final String CREATE_TABLE_PETS = "CREATE TABLE " + PetEntry.TABLE_NAME + "(" +
            PetEntry.COLUMN_PET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL," +
            PetEntry.COLUMN_PET_BREED + " TEXT," +
            PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL," +
            PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
    private static final String DROP_TABLE_COMMAND = "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME + ";";
    /**
     +     * Constructs a new instance of {@link PetDBHelper}.
     +     *
     +     * @param context of the app
     +     */
    public PetDBHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    /**
     +     * This is called when the database is created for the first time.
     +     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PETS);
    }
    /**
     +     * This is called when the database needs to be upgraded.
     +     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_COMMAND);
        onCreate(db);
    }
}
