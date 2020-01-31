package com.ora.android.eyecup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;

import static com.ora.android.eyecup.Globals.APP_DATA_DBNAME;

public class DatabaseOpenHelper extends SQLiteAssetHelper {
//    private static final String DATABASE_NAME = "ORADb.db";
    private static final String DATABASE_NAME = APP_DATA_DBNAME;
//    private static final int DATABASE_VERSION = 1;                //emulator
//    private static final int DATABASE_VERSION = 2;                  //new device install
    private static final int DATABASE_VERSION = 3;                  //new device install

//jlr add
    private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
    private static String DB_NAME ="YourDbName"; // Database name
    private static int DB_VERSION = 1; // Database version
    private final File DB_FILE;
    private SQLiteDatabase mDataBase;
    private final Context mContext;


    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//jlr add
        DB_FILE = context.getDatabasePath(DB_NAME);
        this.mContext = context;
    }

    @Override
    public synchronized void close() {
        if(mDataBase != null) {
            mDataBase.close();
        }
        super.close();
    }
}
