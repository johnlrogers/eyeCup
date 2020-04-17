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
import android.widget.EditText;

import com.ora.android.eyecup.oradb.TAppSetting;

import androidx.appcompat.app.AppCompatActivity;

public class AdminCameraActivity extends AppCompatActivity {
    private Global global = Global.Context(this);

    private boolean isBound = false;
    private AlwaysService alwaysService;

    private CameraSettings mCamSet;

    private EditText tbxPicDelay;
    private EditText tbxPicFocus;
    private EditText tbxPicAperture;
    private EditText tbxPicShutter;
    private EditText tbxPicSensitivity;
    private EditText tbxPicFrameDur;
    private EditText tbxPicCropW;
    private EditText tbxPicCropH;
    private EditText tbxPicZoomDigital;
    private EditText tbxPicZoomOptical;
    private EditText tbxPicWBTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_camera);

        mCamSet = new CameraSettings(getApplicationContext());

        tbxPicDelay = global.GetView(R.id.tbxPicDelay);
        tbxPicFocus = global.GetView(R.id.tbxPicFocus);
        tbxPicAperture = global.GetView(R.id.tbxPicAperture);
        tbxPicShutter = global.GetView(R.id.tbxPicShutter);
        tbxPicSensitivity = global.GetView(R.id.tbxPicSensitivity);
        tbxPicFrameDur = global.GetView(R.id.tbxPicFrameDur);
        tbxPicCropW = global.GetView(R.id.tbxPicCropW);
        tbxPicCropH = global.GetView(R.id.tbxPicCropH);
        tbxPicZoomDigital = global.GetView(R.id.tbxPicZoomDigital);
        tbxPicZoomOptical = global.GetView(R.id.tbxPicZoomOptical);
        tbxPicWBTemp = global.GetView(R.id.tbxPicWBTemp);

        tbxPicDelay.setText(String.valueOf(mCamSet.getPicDELAY_SECONDS()));
        tbxPicFocus.setText(String.valueOf(mCamSet.getPicFOCUS_CM()));
        tbxPicAperture.setText(String.valueOf(mCamSet.getPicAPERTURE()));
        tbxPicShutter.setText(String.valueOf(mCamSet.getPicSHUTTER_FACTOR()));
        tbxPicSensitivity.setText(String.valueOf(mCamSet.getPicSENS_SENSITIVITY()));
        tbxPicFrameDur.setText(String.valueOf(mCamSet.getPicFRAME_DURATION_MS()));
        tbxPicCropW.setText(String.valueOf(mCamSet.getPicCROP_W_FACTOR()));
        tbxPicCropH.setText(String.valueOf(mCamSet.getPicCROP_H_FACTOR()));
        tbxPicZoomDigital.setText(String.valueOf(mCamSet.getPicZOOM_DIGITAL()));
        tbxPicZoomOptical.setText(String.valueOf(mCamSet.getPicZOOM_OPTICAL()));
        tbxPicWBTemp.setText(String.valueOf(mCamSet.getPicWHITE_BALANCE_TEMP()));

        final Button button = findViewById(R.id.btnSave);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                SaveSettings();
                try {
                    finish();

                } catch (Exception e) {
                    Log.e("AdminCameraActivity:onClick:Finish", e.toString());
                    //todo handle
                }

            }
        });
    }

    public boolean SaveSettings() {
        boolean bSuccess = true;
        TAppSetting appSet;
        float fCur;
        float fSet;
        int iCur;
        int iSet;
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());
        try {
            dba.open();

            iCur = mCamSet.getPicDELAY_SECONDS();
            iSet = Integer.parseInt(tbxPicDelay.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_DELAY_SECONDS");
                appSet.setAppSetInt(Integer.parseInt(tbxPicDelay.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            fCur = mCamSet.getPicFOCUS_CM();
            fSet = Float.parseFloat(tbxPicFocus.getText().toString());
            if (fCur != fSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_FOCUS_CM");
                appSet.setAppSetReal(Float.parseFloat(tbxPicFocus.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            fCur = mCamSet.getPicAPERTURE();
            fSet = Float.parseFloat(tbxPicAperture.getText().toString());
            if (fCur != fSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_APERTURE");
                appSet.setAppSetReal(Float.parseFloat(tbxPicAperture.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicSHUTTER_FACTOR();
            iSet = Integer.parseInt(tbxPicShutter.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_SHUTTER_FACTOR");
                appSet.setAppSetInt(Integer.parseInt(tbxPicShutter.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicSENS_SENSITIVITY();
            iSet = Integer.parseInt(tbxPicSensitivity.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_SENS_SENSITIVITY");
                appSet.setAppSetInt(Integer.parseInt(tbxPicSensitivity.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicFRAME_DURATION_MS();
            iSet = Integer.parseInt(tbxPicFrameDur.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_FRAME_DURATION_MS");
                appSet.setAppSetInt(Integer.parseInt(tbxPicFrameDur.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicCROP_W_FACTOR();
            iSet = Integer.parseInt(tbxPicCropW.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_CROP_W_FACTOR");
                appSet.setAppSetInt(Integer.parseInt(tbxPicCropW.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicCROP_H_FACTOR();
            iSet = Integer.parseInt(tbxPicCropH.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_CROP_H_FACTOR");
                appSet.setAppSetInt(Integer.parseInt(tbxPicCropH.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicZOOM_DIGITAL();
            iSet = Integer.parseInt(tbxPicZoomDigital.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_ZOOM_DIGITAL");
                appSet.setAppSetInt(Integer.parseInt(tbxPicZoomDigital.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicZOOM_OPTICAL();
            iSet = Integer.parseInt(tbxPicZoomOptical.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_ZOOM_OPTICAL");
                appSet.setAppSetInt(Integer.parseInt(tbxPicZoomOptical.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            iCur = mCamSet.getPicWHITE_BALANCE_TEMP();
            iSet = Integer.parseInt(tbxPicWBTemp.getText().toString());
            if (iCur != iSet) {
                appSet = new TAppSetting();
                appSet.setAppSetName("PIC_WHITE_BALANCE_TEMP");
                appSet.setAppSetInt(Integer.parseInt(tbxPicWBTemp.getText().toString()));
                bSuccess = dba.SetAppSettingValue(appSet);
            }

            if (isBound) {                                  //bound?
                alwaysService.GetCameraSettingsFromDb();        //repop service camera settings
                alwaysService.LogMsg("Save Camera Settings");
            }
        } catch (NullPointerException e){
            Log.e("AdminActivity:SetPatient.SetParticipantInfo:NPEx", e.toString());
            //todo handle
        } finally {
            dba.close();
        }

        return bSuccess;
    }

    @Override
    public void onBackPressed() {
        global.Back();
        super.onBackPressed();
//20200302
        if (isBound) {
            alwaysService.CancelAdminChanges();       //cancel
            try {
                finish();
            } catch (Exception e) {
                Log.e("MainActivity:onCreate:Finish", e.toString());
                //todo handle
            }
        }
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
