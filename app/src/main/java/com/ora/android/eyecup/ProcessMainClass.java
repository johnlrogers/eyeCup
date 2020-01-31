package com.ora.android.eyecup;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ProcessMainClass {
    private static final String TAG = ProcessMainClass.class.getSimpleName();
    private static Intent serviceIntent = null;

    /** Create Class */
    public ProcessMainClass() {
    }

    /** set Service Intent Class */
    private void setServiceIntent(Context context) {
        Log.d(TAG, "setServiceIntent()");
        if (serviceIntent == null) {
            serviceIntent = new Intent(context, AlwaysService.class);   //AlwaysService class
        }
    }

    /** Launch Service */
    public void launchService(Context context) {
        if (context == null) {
            return;
        }
        setServiceIntent(context);                              //set intent

        context.startForegroundService(serviceIntent);
        Log.d(TAG, "launchService()");
    }
}

