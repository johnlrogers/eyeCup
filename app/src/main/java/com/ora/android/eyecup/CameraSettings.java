package com.ora.android.eyecup;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class CameraSettings {

    private int mPicDELAY_SECONDS = 10;
    private float mPicFOCUS_CM = 10f;
    private float mPicAPERTURE = 2.4f;
    private int mPicSHUTTER_FACTOR = 1500;
    private int mPicSENS_SENSITIVITY = 100;
    private int mPicFRAME_DURATION_MS = 25000;
    private int mPicCROP_W_FACTOR = 10;
    private int mPicCROP_H_FACTOR = 20;
    private int mPicZOOM_DIGITAL = 1;
    private int mPicZOOM_OPTICAL = 1;

    /* default constructor */
    public CameraSettings() {
        mPicDELAY_SECONDS = 10;
        mPicFOCUS_CM = 10f;
        mPicAPERTURE = 2.4f;
        mPicSHUTTER_FACTOR = 1500;
        mPicSENS_SENSITIVITY = 100;
        mPicFRAME_DURATION_MS = 25000;
        mPicCROP_W_FACTOR = 10;
        mPicCROP_H_FACTOR = 20;
        mPicZOOM_DIGITAL = 1;
        mPicZOOM_OPTICAL = 1;
    }

    public CameraSettings(Context context) {
        GetSettingsFromDB(context);
    }

    public int getPicDELAY_SECONDS(){
        return mPicDELAY_SECONDS;
    }
    public void setPicDELAY_SECONDS(int iDELAY_SECONDS){
        this.mPicDELAY_SECONDS = iDELAY_SECONDS;
    }

    public float getPicFOCUS_CM(){
        return mPicFOCUS_CM;
    }
    public void setPicFOCUS_CM(float fFOCUS_CM){
        this.mPicFOCUS_CM = fFOCUS_CM;
    }

    public float getPicAPERTURE(){
        return mPicAPERTURE;
    }
    public void setPicAPERTURE(float fAPERTURE){
        this.mPicAPERTURE = fAPERTURE;
    }

    public int getPicSHUTTER_FACTOR(){
        return mPicSHUTTER_FACTOR;
    }
    public void setPicSHUTTER_FACTOR(int iSHUTTER_FACTOR){ this.mPicSHUTTER_FACTOR = iSHUTTER_FACTOR; }

    public int getPicSENS_SENSITIVITY(){
        return mPicSENS_SENSITIVITY;
    }
    public void setPicSENS_SENSITIVITY(int iSENS_SENSITIVITY){ this.mPicSENS_SENSITIVITY = iSENS_SENSITIVITY; }

    public int getPicFRAME_DURATION_MS(){
        return mPicFRAME_DURATION_MS;
    }
    public void setPicFRAME_DURATION_MS(int iFRAME_DURATION_MS){ this.mPicFRAME_DURATION_MS = iFRAME_DURATION_MS; }

    public int getPicCROP_W_FACTOR(){
        return mPicCROP_W_FACTOR;
    }
    public void setPicCROP_W_FACTOR(int iCROP_W_FACTOR){
        this.mPicCROP_W_FACTOR = iCROP_W_FACTOR;
    }

    public int getPicCROP_H_FACTOR(){
        return mPicCROP_H_FACTOR;
    }
    public void setPicCROP_H_FACTOR(int iCROP_H_FACTOR){
        this.mPicCROP_H_FACTOR = iCROP_H_FACTOR;
    }

    public int getPicZOOM_DIGITAL(){
        return mPicZOOM_DIGITAL;
    }
    public void setPicZOOM_DIGITAL(int iZOOM_DIGITAL){
        this.mPicZOOM_DIGITAL = iZOOM_DIGITAL;
    }

    public int getPicZOOM_OPTICAL(){
        return mPicZOOM_OPTICAL;
    }
    public void setPicZOOM_OPTICAL(int iZOOM_OPTICAL){
        this.mPicZOOM_OPTICAL = iZOOM_OPTICAL;
    }

    //20200405 Get or Refresh AppSettings from Db
    public boolean GetSettingsFromDB(Context context) {
        boolean bRet = false;
        String strQry;
        Cursor crs;

        DatabaseAccess dba = DatabaseAccess.getInstance(context);       //get db access
        try {
            dba.open();                                                     //open db

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_DELAY_SECONDS'";   //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicDELAY_SECONDS(crs.getInt(crs.getColumnIndex("AppSetInt")));           //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetReal FROM tAppSettings WHERE AppSetName = 'PIC_FOCUS_CM'";       //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicFOCUS_CM(crs.getFloat(crs.getColumnIndex("AppSetReal")));             //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetReal FROM tAppSettings WHERE AppSetName = 'PIC_APERTURE'";       //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicAPERTURE(crs.getFloat(crs.getColumnIndex("AppSetReal")));             //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_SHUTTER_FACTOR'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicSHUTTER_FACTOR(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_SENS_SENSITIVITY'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicSENS_SENSITIVITY(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_FRAME_DURATION_MS'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicFRAME_DURATION_MS(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_CROP_W_FACTOR'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicCROP_W_FACTOR(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_CROP_H_FACTOR'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicCROP_H_FACTOR(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_ZOOM_DIGITAL'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicZOOM_DIGITAL(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_ZOOM_OPTICAL'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicZOOM_OPTICAL(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
            }
            crs.close();                                                                            //close cursor

            dba.close();                                                    //close db
            bRet = true;                                                    //set success

        } catch (NullPointerException e) {
            Log.e("AlwaysService:GetAppSettingsFromDB:NPEx", e.toString());
            //todo handle
        }
        return bRet;
    }
}
