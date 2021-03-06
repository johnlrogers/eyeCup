package com.ora.android.eyecup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ora.android.eyecup.json.ActivityResponse;

import java.lang.reflect.Type;
import java.util.Collection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.ora.android.eyecup.Globals.RDB_INVERSE_SEP_FACTOR_VERT;
import static com.ora.android.eyecup.Globals.RDB_TEXT_SIZE;

public class SingleRadioBtnsActivity extends AppCompatActivity {

    private Global global = Global.Context(this);

    private TextView txtInstruction;
    private Globals glob;
    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";

    private RadioGroup rdg1;
    private int miRspVal1 = 0;
    private String mstrRspTxt = "";

    ActivityResponse actRsp;
    String strRsps = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleradiobtns);

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");

        Gson gS = new Gson();
        strRsps = getIntent().getStringExtra("ActRsp");
        Type collectionType = new TypeToken<Collection<ActivityResponse>>(){}.getType();
        Collection<ActivityResponse> mArrActRsp = gS.fromJson(strRsps, collectionType);

        rdg1 = new RadioGroup(this);
        PopRDB(mArrActRsp);

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(btnContinueClickListener);

        txtInstruction = findViewById(R.id.txtInstruction);
        txtInstruction.setText(mstrActTxt);
    }

    /* populate response objects in group */
    private void PopRDB(Collection<ActivityResponse> mArrActRsp) {

        rdg1.setOrientation(LinearLayout.VERTICAL);

        int iSepSize = RDB_INVERSE_SEP_FACTOR_VERT / mArrActRsp.size();     //vertical separation
        int i = 0;
        for (ActivityResponse actRsp : mArrActRsp) {
            if (i > 0) {
                // add a divider with height of 1 iSepSize pixels
                View v1 = new View(this);
                v1.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, iSepSize));
//                v1.setBackgroundColor(Color.GRAY);
                rdg1.addView(v1);
            }
            i++;

            RadioButton btn1 = new RadioButton(this);
            btn1.setId(actRsp.getActRspValue().intValue());
            btn1.setText(actRsp.getActRspText());
            btn1.setTextSize(RDB_TEXT_SIZE);
            rdg1.addView(btn1);
        }
        ((ViewGroup) findViewById(R.id.rdgButtons)).addView(rdg1);
    }

    private boolean saveResponse() {
        boolean bRet;
        bRet = alwaysService.SaveActivityResult(miCurActId, miRspVal1, mstrRspTxt, "N");
        return bRet;
    }

    private boolean validateResponse() {
        boolean bVal = false;
        int iVal;

        iVal = rdg1.getCheckedRadioButtonId();

        if (iVal != -1) {
            miRspVal1 = iVal;

            View radioButton = rdg1.findViewById(iVal);
            int radioId = rdg1.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rdg1.getChildAt(radioId);
            mstrRspTxt =  (String) btn.getText();

            bVal = true;
        } else {
            showAlertDialogButtonClicked("Response Required", "Please select one of the choices.");
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
            Log.e("SingleRadioBtns:OnClickListener", "SingleRadioBtns btnContinueListener");

            if (!validateResponse()){       //invalid response
             return;
            }
            saveResponse();

            try {
                finish();

                int iNextActId = alwaysService.setNextActivityIdx();
                alwaysService.GotoEvtAct(iNextActId);

            } catch (Exception e) {
                Log.e("SingleRadioBtnActivity:onClick:Finish", e.toString());
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
