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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SingleSeekBarActivity extends AppCompatActivity {

    private Global global = Global.Context(this);

    private TextView txtInstruction;
    private TextView txtMin;
    private TextView txtMax;

    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";
    private String mstrMinTxt = "";
    private String mstrMaxTxt = "";

    private int miRspVal1 = 0;
    private String mstrRspTxt1 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleseekbar);

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");
        mstrMinTxt =  Long.toString(getIntent().getLongExtra("RspMin", 0));
        mstrMaxTxt =  Long.toString(getIntent().getLongExtra("RspMax", 100));

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(btnContinueClickListener);

        txtInstruction = findViewById(R.id.txtInstruction);
        txtInstruction.setText(mstrActTxt);

        txtMin = findViewById(R.id.lblMin);
        txtMin.setText(mstrMinTxt);

        txtMax = findViewById(R.id.lblMax);
        txtMax.setText(mstrMaxTxt);

        global.InitInputView(global.<SeekBar>GetView(R.id.skbSlider), new String[] { }, x -> {  //slider move

            //todo Warning:(60, 59) Number formatting does not take into account locale settings. Consider using `String.format` instead.
            global.<TextView>GetView(R.id.lblVal).setText(Integer.toString(x));             //update view
            miRspVal1 = x;                                                                  //set response value
            return null;
        });
    }

    private boolean saveResponse() {
        boolean bRet = false;
        //todo save to database
        return bRet;
    }

    private boolean validateResponse() {
        boolean bVal = false;

        if (miRspVal1 >= 0) {
            bVal = true;
        } else {
            showAlertDialogButtonClicked("Response Required", "Please move the slider");
        }
        return bVal;
    }

    //    public void showAlertDialogButtonClicked(View view) {
    public void showAlertDialogButtonClicked(String strTitle, String strMsg) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(strTitle);
        builder.setMessage(strMsg);

        // add a button
        builder.setPositiveButton("OK", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private View.OnClickListener btnContinueClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.e("SingleSeekBar:OnClickListener", "SingleSeekBar btnContinueListener");

            if (!validateResponse()){       //invalid response
                return;
            }
            saveResponse();

            try {
                finish();

                int iNextActId = alwaysService.setNextActivityIdx();
                alwaysService.GotoEvtAct(iNextActId);

            } catch (Exception e) {
                Log.e("SingleSeekBarActivity:onClick:Finish", e.toString());
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