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
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ora.android.eyecup.json.ActivityResponse;

import java.lang.reflect.Type;
import java.util.Collection;

import androidx.appcompat.app.AppCompatActivity;

public class SingleListActivity extends AppCompatActivity {

    private Global global = Global.Context(this);
    private int selectedIndex = -1;

    private TextView txtInstruction;
    private Globals glob;
    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";

    private int miRspVal1 = 0;
    private String mstrRspTxt1 = "";

    ActivityResponse actRsp;
    String strRsps = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlelist);

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");

        Gson gS = new Gson();
        strRsps = getIntent().getStringExtra("ActRsp");
        Type collectionType = new TypeToken<Collection<ActivityResponse>>(){}.getType();
        Collection<ActivityResponse> mArrActRsp = gS.fromJson(strRsps, collectionType);

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(btnContinueClickListener);

        txtInstruction = findViewById(R.id.txtInstruction);
        txtInstruction.setText(mstrActTxt);

        global.InitInputView(global.<ListView>GetView(R.id.lstList), new String[]{ "Item A", "Item B", "Item C" }, x -> {
            int lastIndex = selectedIndex;
            selectedIndex = x;
            return lastIndex;
        });
    }

    private View.OnClickListener btnContinueClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d("SingleList:OnClickListener", "SingleList btnContinueListener");

//            if (!validateResponse()){       //invalid response
//                return;
//            }
//            saveResponse();

            try {
                finish();

                int iNextActId = alwaysService.setNextActivityIdx();
                alwaysService.GotoEvtAct(iNextActId);

            } catch (Exception e) {
                Log.e("SingleListActivity:onClick:Finish", e.toString());
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

//    @Override
//    public void onBackPressed() {
//        global.Back();
//        super.onBackPressed();
//    }
}