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

public class DoubleSeekBarActivity extends AppCompatActivity {

    private Global global = Global.Context(this);

    private TextView txtInstruction;
    private TextView txtMin1;
    private TextView txtMax1;
    private TextView txtMin2;
    private TextView txtMax2;

    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";
    private String mstrMinTxt = "";
    private String mstrMaxTxt = "";

    private int miRspVal1 = 0;
    private String mstrRspTxt1 = "";
    private int miRspVal2 = 0;
    private String mstrRspTxt2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doubleseekbar);

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");
        mstrMinTxt =  Long.toString(getIntent().getLongExtra("RspMin", 0));
        mstrMaxTxt =  Long.toString(getIntent().getLongExtra("RspMax", 100));

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(btnContinueClickListener);

        txtInstruction = findViewById(R.id.txtInstruction);
        txtInstruction.setText(mstrActTxt);

        txtMin1 = findViewById(R.id.lblMin1);
        txtMin1.setText(mstrMinTxt);

        txtMax1 = findViewById(R.id.lblMax1);
        txtMax1.setText(mstrMaxTxt);

        txtMin2 = findViewById(R.id.lblMin2);
        txtMin2.setText(mstrMinTxt);

        txtMax2 = findViewById(R.id.lblMax2);
        txtMax2.setText(mstrMaxTxt);

        global.InitInputView(global.<SeekBar>GetView(R.id.skbSliderOne), new String[] { }, x -> {   //slider move

            global.<TextView>GetView(R.id.lblValOne).setText(Integer.toString(x));                      //update view
            miRspVal1 = x;                                                                       //set response value
            return null;
        });
        global.InitInputView(global.<SeekBar>GetView(R.id.skbSliderTwo), new String[] { }, x -> {   //slider move

            global.<TextView>GetView(R.id.lblValTwo).setText(Integer.toString(x));                      //update view
            miRspVal2 = x;                                                                      //set response value
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
            if (miRspVal2 >= 0) {
                bVal = true;
            } else {
                showAlertDialogButtonClicked("Response Required", "Please adjust the bottom slider");
            }
        } else {
            showAlertDialogButtonClicked("Response Required", "Please adjust the top slider");
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
            Log.e("DoubleSeekBar:OnClickListener", "DoubleSeekBar btnContinueListener");

            if (!validateResponse()){       //invalid response
                return;
            }
            saveResponse();

            try {
                finish();

                int iNextActId = alwaysService.setNextActivityIdx();
                alwaysService.GotoEvtAct(iNextActId);

            } catch (Exception e) {
                Log.e("DoubleSeekBarActivity:onClick:Finish", e.toString());
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