/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ora.android.eyecup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import android.support.v7.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {

    Camera2BasicFragment fmCamera;
    private TextView txtInstruction;
    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";
    private String mstrPatNumber = "";
    private String mstrActPictureCode = "";

    public String getActTxt() {
        return mstrActTxt;
    }
    public String getPictureCode() { return mstrActPictureCode; }
    public String getPatNumber() { return mstrPatNumber; }
    public int getCurActId() {
        return miCurActId;
    }

    private boolean saveResponse() {
        boolean bRet = false;
        //todo save to database
        return bRet;
    }

//    public void setPictureFileName(String strFile) {
//
//        mstrActTxt = strFile;
//
//    }

    public void MyFinish() {

        try {
            Log.d("CameraActivity:MyFinish", "finish()");
            finish();
            int iNextActId;
            iNextActId = alwaysService.setNextActivityIdx();    //next id

            Log.d("CameraActivity:MyFinish", "alwaysService.GotoEvtAct(iNextActId)");
            alwaysService.GotoEvtAct(iNextActId);

        } catch(Exception e) {
            Log.e("CameraActivity:MyFinish", e.toString());
        }
    }

    public void GotoNextPicture(boolean bAcceptedPicture) {

        try {
            int iNextActId;
//            int iNextActTypeID;
            if (bAcceptedPicture) {
                iNextActId = alwaysService.setNextActivityIdx();    //next id
            } else {
                iNextActId = alwaysService.getCurActivityIdx();     //same id
            }
            Log.d("CameraActivity:GotoNextPicture", "finish()");
            finish();

            Log.d("CameraActivity:GotoNextPicture", "alwaysService.GotoEvtAct(iNextActId)");
            alwaysService.GotoEvtAct(iNextActId);
//            iNextActTypeID = alwaysService.getActivityTypeFromIdx(iNextActId);
//            if (iNextActTypeID == ACTIVITY_TYPE_PICTURE) {
//                miCurActId = iNextActId;
//                mstrActTxt = alwaysService.getActivityTextFromIdx(iNextActId);
//                mstrActPictureCode = alwaysService.getActivityPictureCodeIdx(iNextActId);
//                mstrPatNumber =  alwaysService.getPatNumber();
//                updateFragmentControls();
//
//            } else {
//                MyFinish();
//            }
        } catch(Exception e) {
            Log.e("CameraActivity:MyFinish", e.toString());
        }
    }

    public void AcceptPicture(String strFile) {                                //accept picture

        mstrActTxt = strFile;

        Log.d("CameraActivity:AcceptPicture", "saveResponse()");
        saveResponse();

        Log.d("CameraActivity:AcceptPicture", "MyFinish(true)");
        GotoNextPicture(true);                                               //Next activity
    }

    public void RejectPicture() {                                //reject picture

        Log.d("CameraActivity:RejectPicture", "MyFinish(false)");
        GotoNextPicture(false);                                               //Next activity
    }

//    public void updateFragmentControls() {
//        //todo is fragment_container_view_tag what I want?
//        Camera2BasicFragment frag = (Camera2BasicFragment)getSupportFragmentManager().
//                findFragmentById(R.id.container);
//
//        frag.setActvityData(mstrActTxt, mstrActPictureCode);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        Log.d("CameraActivity:", "OnCreate");
//        if (null == savedInstanceState) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, Camera2BasicFragment.newInstance(), "fmCamera")
//                    .commit();
//        }
        if (null != savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.container))
                    .commit();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, Camera2BasicFragment.newInstance(), "fmCamera")
                .commit();

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");
        mstrPatNumber =  getIntent().getStringExtra("PatNum");
        mstrActPictureCode =  getIntent().getStringExtra("ActPicCode");

//        if (!(alwaysService == null)) {
//            mstrActPictureCode = alwaysService.getActivityPictureCodeIdx(miCurActId);
//            updateFragmentControls();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt = getIntent().getStringExtra("ActTxt");
        mstrPatNumber =  getIntent().getStringExtra("PatNum");
        mstrActPictureCode =  getIntent().getStringExtra("ActPicCode");

//        if (!(alwaysService == null)) {
//            mstrActPictureCode = alwaysService.getActivityPictureCodeIdx(miCurActId);
//            updateFragmentControls();
//        }
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
}
