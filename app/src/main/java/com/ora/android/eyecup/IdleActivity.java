package com.ora.android.eyecup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class IdleActivity extends AppCompatActivity {

    private TextView txtInstruction;
    private TextView txtDbVer;
    private Globals glob;
    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";
    private String mstrActDbVer = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//20200212
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        setContentView(R.layout.activity_idle);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");
        mstrActDbVer =  getIntent().getStringExtra("ActDbVerTxt");

        txtInstruction = findViewById(R.id.txtInstruction);
        txtInstruction.setOnClickListener(idleClickListenerTop);
        txtInstruction.setText(mstrActTxt);

        txtDbVer = findViewById(R.id.txtDbVersion);
        txtDbVer.setOnClickListener(idleClickListenerBottom);
        txtDbVer.setText(mstrActDbVer);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AlwaysService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

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

    private View.OnClickListener idleClickListenerTop = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("Idle:OnClickListener", "Idle txtInstructions");
            alwaysService.SetAdminUnlockState(true);
        }
    };

    private View.OnClickListener idleClickListenerBottom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("Idle:OnClickListener", "Idle txtDbVersion");
            alwaysService.SetAdminUnlockState(false);
        }
    };
}
