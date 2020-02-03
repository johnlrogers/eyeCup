package com.ora.android.eyecup;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ora.android.eyecup.restarter.RestartServiceBroadcastReceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private boolean isBound = false;
    private AlwaysService alwaysService;

    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        // Register an observer (mMessageReceiver) to receive Intents with actions named "custom-event-name".
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                new IntentFilter(ALWAYS_SVC_UPDATE_MSG));

        setContentView(R.layout.activity_main);

        btnContinue = findViewById(R.id.btnContinue);    //get id of button 2
        btnContinue.setVisibility(Button.GONE);                     //hide the button

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isBound) {
                    boolean bRet = alwaysService.StartStateMachine();

                    try {
                        finish();

                    } catch (Exception e) {
                        Log.e("MainActivity:onCreate:Finish", e.toString());
                        //todo handle
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        RestartServiceBroadcastReceiver.scheduleJob(getApplicationContext());

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                new IntentFilter(ALWAYS_SVC_UPDATE_MSG));
    }

//    @Override
//    protected void onDestroy() {
//        // Unregister since the activity is about to be closed.
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//        super.onDestroy();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AlwaysService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
//        // Unregister since the activity is about to be closed.
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onStop();

        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named ALWAYS_SVC_UPDATE_MSG is received.
//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            String message = intent.getStringExtra("Status");
//            Log.d("receiver", "Got message: " + message);
////            if (intent.getAction().toString() == ALWAYS_SVC_UPDATE_MSG ) {
//                ProcessAlwaysServiceMsg();
////            }
//        }
//    };

//    private boolean ProcessAlwaysServiceMsg() {
//        boolean bRet = false;
//
//        return bRet;
//    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AlwaysService.LocalBinder binder = (AlwaysService.LocalBinder) service;
            alwaysService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

}
