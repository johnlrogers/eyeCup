package com.ora.android.eyecup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InstructionActivity extends AppCompatActivity {

    private TextView txtInstruction;
    private Globals glob;
    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(btnContinueClickListener);

        txtInstruction = findViewById(R.id.txtInstruction);
        txtInstruction.setText(mstrActTxt);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private View.OnClickListener btnContinueClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.e("Instruction:OnClickListener", "Instruction btnContinueListener");

            try {
                finish();

                if (miCurActId != 999) {                                //Not "no more activities"
                    int iNextActId = alwaysService.setNextActivityIdx();
                    alwaysService.GotoEvtAct(iNextActId);
                }

            } catch (Exception e) {
                Log.e("InstructionActivity:onClick:Finish", e.toString());
                //todo handle
            }
        }
    };

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

}
