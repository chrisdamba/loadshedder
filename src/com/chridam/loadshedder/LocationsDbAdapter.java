package com.chridam.loadshedder;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Windows
 * Date: 4/15/13
 * Time: 7:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class LocationsDbAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CODE = "AreaCode";
    public static final String KEY_NAME = "Name";
    public static final String KEY_REGION = "Region";

    private static final String TAG = "LocationsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_PATH = "/data/data/com.chridam.loadshedder/databases/";
    private static final String DATABASE_NAME = "loadshed.db";
    private static final String SQLITE_TABLE = "Locations";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    KEY_CODE + "," +
                    KEY_NAME + "," +
                    KEY_REGION + "," +
                    " UNIQUE (" + KEY_CODE +"));";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context myContext;
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.myContext = context;
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }

        public void openDataBase() throws SQLException{
            //Open the database
            String myPath = DATABASE_PATH + DATABASE_NAME;
            SQLiteDatabase openDB = null;
            openDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }

        public void createDataBase() throws IOException{
            boolean dbExist = checkDataBase();
            if(dbExist){
                //do nothing - database already exist
            }else{
                //By calling this method an empty database will be created into the default system path
                //of your application so we are gonna be able to overwrite that database with our database.
                this.getReadableDatabase();
                try {
                    copyDataBase();
                } catch (IOException e) {
                    throw new Error("Error copying database");
                }
            }
        }

        private boolean checkDataBase() throws IOException {
            SQLiteDatabase checkDB = null;
            String myPath = DATABASE_PATH + DATABASE_NAME;
            try{
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            }
            catch(SQLiteException e){
                // copy if db doesn't exist
                AssetManager am = myContext.getAssets();
                OutputStream os = new FileOutputStream(myPath);
                byte[] b = new byte[100];
                int r;
                InputStream is = am.open(DATABASE_NAME);
                while ((r = is.read(b)) != -1) {
                    os.write(b, 0, r);
                }
                is.close();
                os.close();
            }

            if(checkDB != null){
                checkDB.close();
            }

            return checkDB != null ? true : false;
        }

        private void copyDataBase() throws IOException{
            //Open your local db as the input stream
            InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

            // Path to the just created empty db
            String outFileName = DATABASE_PATH + DATABASE_NAME;

            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);

            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }
            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }

    public LocationsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public LocationsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        try {
            mDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
          mDbHelper.openDataBase();
        } catch (SQLException sqle) {
          throw sqle;
        }

        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public long createLocation(String code, String name, String region) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CODE, code);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_REGION, region);

        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public boolean deleteAllLocations() {

        int doneDelete = 0;
        doneDelete = mDb.delete(SQLITE_TABLE, null , null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;

    }

    public Cursor fetchLocationsByName(String inputText) throws SQLException {
        Log.w(TAG, inputText);
        Cursor mCursor = null;
        if (inputText == null  ||  inputText.length () == 0)  {
            mCursor = mDb.query(SQLITE_TABLE, new String[] {KEY_ROWID,
                    KEY_CODE, KEY_NAME, KEY_REGION},
                    null, null, null, null, null);

        }
        else {
            mCursor = mDb.query(true, SQLITE_TABLE, new String[] {KEY_ROWID,
                    KEY_CODE, KEY_NAME, KEY_REGION},
                    KEY_NAME + " like '%" + inputText + "%'", null,
                    null, null, null, null);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public Cursor fetchAllLocations() {

        Cursor mCursor = mDb.query(SQLITE_TABLE, new String[] {KEY_REGION, KEY_ROWID,
                KEY_CODE, KEY_NAME}, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
