package com.example.details;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "pims_all.db";

    private final Context mContext;
    private SQLiteDatabase mDatabase;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public void createDatabase() {
        boolean dbExist = checkDatabase();
        if (!dbExist) {
            // If database does not exist, copy it from assets
            new CopyDatabaseTask().execute();
        }
    }

    private boolean checkDatabase() {
        File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDatabase() throws IOException {
        InputStream inputStream = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = mContext.getDatabasePath(DATABASE_NAME).getPath();
        OutputStream outputStream = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public SQLiteDatabase getDatabase() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = SQLiteDatabase.openDatabase(mContext.getDatabasePath(DATABASE_NAME).getPath(), null, SQLiteDatabase.OPEN_READONLY);
        }
        return mDatabase;
    }

    @Override
    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database creation is handled by copying the database file
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }

    private class CopyDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                copyDatabase();
            } catch (IOException e) {
                Log.e(TAG, "Error copying database", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(mContext, "Database copied successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
