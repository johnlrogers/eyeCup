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
    private Globals glob;
    private boolean isBound = false;
    private AlwaysService alwaysService;
    private int miCurActId = 0;
    private String mstrActTxt = "";

    private String mstrPatNumber = "";

    public String getActTxt() {
        return mstrActTxt;
    }

    public int getCurActId() {
        return miCurActId;
    }

    private boolean saveResponse() {
        boolean bRet = false;
        //todo save to database
        return bRet;
    }

    public String getPatNumber() {
//        return alwaysService.getPatNumber();
        return mstrPatNumber;
    }

    public boolean setPictureFileName(String strFile) {

        mstrActTxt = strFile;

        return true;
    }

    public void MyFinish(boolean bAcceptedPicture) {

        try {

            int iNextActId;
            if (bAcceptedPicture) {
                iNextActId = alwaysService.getNextActivityIdx();    //next id
            } else {
                iNextActId = alwaysService.getCurActivityIdx();     //same id
            }

//            if (miCurActId != 999) {                                //Not "no more activities"
//                iNextActId = alwaysService.getNextActivityIdx();
//            }
            finish();

            alwaysService.GotoEvtAct(iNextActId);

        } catch(Exception e) {
            Log.e("CameraActivity:Finish", e.toString());
        }
    }

    public void AcceptPicture(String strFile) {                                //accept picture

        mstrActTxt = strFile;

        saveResponse();

        MyFinish(true);                                               //finish activity

    }

    public void RejectPicture() {                                //reject picture

//        showAlertDialogButtonClicked("Retake Picture", "Camera is ready to take a new picture.");
        //causes error; call from fragment

        MyFinish(false);                                               //finish activity

    }

//    public void showAlertDialogButtonClicked(String strTitle, String strMsg) {
//
//        // setup the alert builder
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(strTitle);
//        builder.setMessage(strMsg);
//
//        // add a button
//        builder.setPositiveButton("OK", null);
//
//        // create and show the alert dialog
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .replace(R.id.container, Camera2BasicFragment.newInstance(), "fmCamera")
                    .commit();
        }
        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt =  getIntent().getStringExtra("ActTxt");
        mstrPatNumber =  getIntent().getStringExtra("PatNum");

    }

    @Override
    protected void onResume() {
        super.onResume();

        miCurActId = getIntent().getIntExtra("ActIdx", 0);
        mstrActTxt = getIntent().getStringExtra("ActTxt");
        mstrPatNumber =  getIntent().getStringExtra("PatNum");
    }
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        int action = event.getAction();
//        int keyCode = event.getKeyCode();
//
//        FragmentManager fm = getSupportFragmentManager();
//        Camera2BasicFragment fragment = (Camera2BasicFragment)fm.findFragmentByTag("fmCamera");
//
//        return true;
////        switch (keyCode) {
////            case KeyEvent.KEYCODE_VOLUME_UP:
////                if (action == KeyEvent.ACTION_DOWN) {
////                    if(fragment != null) {
//////                        fragment.takePicture();
////                    }
////                }
////                return true;
////            case KeyEvent.KEYCODE_VOLUME_DOWN:
////                if (action == KeyEvent.ACTION_DOWN) {
////                    if(fragment != null) {
//////                        fragment.takePicture();
////                    }
////                }
////                return true;
////            default:
////                return super.dispatchKeyEvent(event);
////        }
//    }

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
