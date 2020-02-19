package com.ora.android.eyecup.workers;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ora.android.eyecup.DatabaseAccess;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class UploadDataWorker extends Worker {

    /**
     * Creates an instance of the {@link Worker}.
     *
     * @param appContext   the application {@link Context}
     * @param workerParams the set of {@link WorkerParameters}
     */
    public UploadDataWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams
    ) {
        super(appContext, workerParams);
    }

    private static final String TAG = UploadDataWorker.class.getSimpleName();

    private static final String TITLE = "Blurred Image";
    private static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault());

    @NonNull
    @Override
    public Worker.Result doWork() {
//        Context applicationContext = getApplicationContext();

        // Makes a notification when the work starts and slows down the work so that it's easier to
        // see each WorkRequest start, even on emulated devices
//        WorkerUtils.makeStatusNotification("Saving image", applicationContext);
//        WorkerUtils.sleep();

//        ContentResolver resolver = applicationContext.getContentResolver();
//        try {
        String strReturn = "fail";
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            long lPatEvtId;
            String strFile;

            dba.open();                                                                 //open db
            String strQry = "SELECT PatEvtId, PatEvtFileName FROM vPatEvtCompNoUpload";
            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate Events
                lPatEvtId = crs.getInt(crs.getColumnIndex("PatEvtId"));         //Get Id
                strFile = crs.getString(crs.getColumnIndex("PatEvtFileName"));  //Get file name
//                UploadParticipantEvent(lPatEvtId, strFile);
//                UploadParticipantEventPictures(lPatEvtId);
                //todo enable catchup loading
            }
            crs.close();
            strReturn = "pass";
        } catch (NullPointerException e) {
            Log.e("AlwaysService:UploadNonUploadedEvents:NPEx", e.toString());
            //todo handle
        } finally {
            dba.close();
        }
        if (strReturn.equals("fail")) {
            return Result.failure();
        } else {
            Data outputData = new Data.Builder()
                    .putString("string", "string")
                    .build();
            return Result.success(outputData);
        }
    }
}
