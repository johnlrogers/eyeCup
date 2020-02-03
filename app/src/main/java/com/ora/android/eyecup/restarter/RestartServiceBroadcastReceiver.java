package com.ora.android.eyecup.restarter;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.ora.android.eyecup.Globals;

import androidx.annotation.RequiresApi;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = RestartServiceBroadcastReceiver.class.getSimpleName();
    private static JobScheduler jobScheduler;
    private RestartServiceBroadcastReceiver restartSensorServiceReceiver;

    /** Get version code for context */
//    public static long getVersionCode(Context context) {
//        PackageInfo pInfo;
//        try {
//            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            long versionCode = System.currentTimeMillis();  //PackageInfoCompat.getLongVersionCode(pInfo);
//            return versionCode;
//
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
//        return 0;
//    }

    //todo Warning:(40, 17) This broadcast receiver declares an intent-filter for a protected broadcast action string,
    // which can only be sent by the system, not third-party applications. However, the receiver's onReceive method does not appear to call getAction
    // to ensure that the received Intent's action string matches the expected value, potentially
    // making it possible for another actor to send a spoofed intent with no action string or a different action string and cause undesired behavior.
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "RestartServiceBR.OnReceive " + context.toString());

        scheduleJob(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        Log.d(TAG, "RestartServiceBR.scheduleJob " + context.toString());

        if (jobScheduler == null) {
            jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        }
        ComponentName componentName = new ComponentName(context, JobService.class);

        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setOverrideDeadline(0)         //run immediately
                .setPersisted(true).build();
        jobScheduler.schedule(jobInfo);
    }


    public static void reStartTracker(Context context) {
        Log.i(TAG, "reStartTracker");

        Intent broadcastIntent = new Intent(Globals.INTENT_SVC_RESTART);
        context.sendBroadcast(broadcastIntent);
    }


    private void registerRestarterReceiver(final Context context) {

        if (restartSensorServiceReceiver == null)
            restartSensorServiceReceiver = new RestartServiceBroadcastReceiver();
        else try{
            context.unregisterReceiver(restartSensorServiceReceiver);
        } catch (Exception e){
            // not registered
            Log.e("JobSvc:unregisterReceiver:Ex", e.toString());
            //todo handle
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //register the  receiver that will restart the background service if it is killed
                IntentFilter filter = new IntentFilter();                       //new intent
                filter.addAction(Globals.INTENT_SVC_RESTART);                   //restart intent

                // if called just after install form restartSensorService the context can be null
                // If it is called from installation of new version use context.registerReceiver.
                try {
                    context.registerReceiver(restartSensorServiceReceiver, filter); //register the receiver
                } catch (Exception e) {                                             //failed?
                    Log.e("RSBR:run:registerReceiver:Ex", e.toString());
                    //todo handle
                    try {
                        context.getApplicationContext().registerReceiver(restartSensorServiceReceiver, filter); //use context.getApplicationContext
                    } catch (Exception e2) {
                        Log.e("RSBR:run:getApplicationContext:Ex", e2.toString());
                        //todo handle
                    }
                }
            }
        }, 1000);       //delayed start

    }

}
