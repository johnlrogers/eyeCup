package com.ora.android.eyecup.restarter;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.ora.android.eyecup.Globals;
import com.ora.android.eyecup.ProcessMainClass;

import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobService extends android.app.job.JobService {
    private static String TAG= JobService.class.getSimpleName();
    private static RestartServiceBroadcastReceiver restartSensorServiceReceiver;
    private static JobService instance;
    private static JobParameters jobParameters;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        ProcessMainClass bck = new ProcessMainClass();
        bck.launchService(this);
        registerRestarterReceiver();
        instance= this;
        JobService.jobParameters= jobParameters;

        return false;
    }

    private void registerRestarterReceiver() {

        if (restartSensorServiceReceiver == null)
            restartSensorServiceReceiver = new RestartServiceBroadcastReceiver();
        else try {
            unregisterReceiver(restartSensorServiceReceiver);
        } catch (Exception e){
            Log.e("JobSvc:registerRestarterReceiver:Ex", e.toString());
            //todo handle current error
            //E/JobSvc:registerRestarterReceiver:Ex: java.lang.IllegalArgumentException: Receiver not registered: com.ora.android.eyecup.restarter.RestartServiceBroadcastReceiver@95a815e
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // we register the  receiver that will restart the background service if it is killed
                IntentFilter filter = new IntentFilter();                   //new intent
                filter.addAction(Globals.INTENT_SVC_RESTART);               //restart intent

                // if called just after install form restartSensorService the context can be null
                // If it is called from installation of new version use context.registerReceiver.
                try {
                   registerReceiver(restartSensorServiceReceiver, filter);      //register the receiver
                } catch (Exception e) {                                             //failed?
                    Log.e("JobSvc:restartSensorServiceReceiver:Ex", e.toString());
                    try {
                        getApplicationContext().registerReceiver(restartSensorServiceReceiver, filter); //use context.getApplicationContext
                    } catch (Exception e2) {
                        Log.e("JobSvc:registerReceiver:Ex", e2.toString());
                        //todo handle
                    }
                }
            }
        }, 1000);       //delayed start
    }

    /** called if Android kills the job service */
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i(TAG, "onStopJob(JobParameters jobParameters)");
        Intent broadcastIntent = new Intent(Globals.INTENT_SVC_RESTART);
        sendBroadcast(broadcastIntent);
        // give the time to run
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                unregisterReceiver(restartSensorServiceReceiver);
            }
        }, 1000);       //delayed start

        return false;
    }

    /** called when the tracker is stopped for whatever reason */
    public static void stopJob(Context context) {
        if (instance!=null && jobParameters!=null) {
            try{
                instance.unregisterReceiver(restartSensorServiceReceiver);
            } catch (Exception e){
                Log.e("JobSvc:stopJob:Ex", e.toString());
                //todo handle
            }
            Log.i(TAG, "Finishing job");
            instance.jobFinished(jobParameters, true);
        }
    }
}