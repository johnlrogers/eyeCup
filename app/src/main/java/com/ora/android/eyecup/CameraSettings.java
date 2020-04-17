package com.ora.android.eyecup;

import android.content.Context;
import android.database.Cursor;
import android.hardware.camera2.params.RggbChannelVector;
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
    private int mPicWHITE_BALANCE_TEMP = 4800;

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
        mPicWHITE_BALANCE_TEMP = 4800;
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

    public int getPicWHITE_BALANCE_TEMP(){
        return mPicWHITE_BALANCE_TEMP;
    }
    public void setPicWHITE_BALANCE_TEMP(int iWHITE_BALANCE_TEMP){
        this.mPicWHITE_BALANCE_TEMP = iWHITE_BALANCE_TEMP;
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

            strQry = "SELECT AppSetInt FROM tAppSettings WHERE AppSetName = 'PIC_WHITE_BALANCE_TEMP'";  //get Setting
            crs = dba.db.rawQuery(strQry, null);                                        //get cursor to view
            while (crs.moveToNext()) {                                                              //open cursor
                setPicWHITE_BALANCE_TEMP(crs.getInt(crs.getColumnIndex("AppSetInt")));          //set
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
/****** following from OpenCamera project *******/

    /** Converts a white balance temperature to red, green even, green odd and blue components.
     */
    public RggbChannelVector convertTemperatureToRggb(int temperature_kelvin) {
        float temperature = temperature_kelvin / 100.0f;
        float red;
        float green;
        float blue;

        if( temperature <= 66 ) {
            red = 255;
        }
        else {
            red = temperature - 60;
            red = (float)(329.698727446 * (Math.pow((double) red, -0.1332047592)));
            if( red < 0 )
                red = 0;
            if( red > 255 )
                red = 255;
        }

        if( temperature <= 66 ) {
            green = temperature;
            green = (float)(99.4708025861 * Math.log(green) - 161.1195681661);
            if( green < 0 )
                green = 0;
            if( green > 255 )
                green = 255;
        }
        else {
            green = temperature - 60;
            green = (float)(288.1221695283 * (Math.pow((double) green, -0.0755148492)));
            if (green < 0)
                green = 0;
            if (green > 255)
                green = 255;
        }

        if( temperature >= 66 )
            blue = 255;
        else if( temperature <= 19 )
            blue = 0;
        else {
            blue = temperature - 10;
            blue = (float)(138.5177312231 * Math.log(blue) - 305.0447927307);
            if( blue < 0 )
                blue = 0;
            if( blue > 255 )
                blue = 255;
        }

//        if( MyDebug.LOG ) {
//            Log.d(TAG, "red: " + red);
//            Log.d(TAG, "green: " + green);
//            Log.d(TAG, "blue: " + blue);
//        }
        return new RggbChannelVector((red/255)*2,(green/255),(green/255),(blue/255)*2);
    }

    private final static int min_white_balance_temperature_c = 1000;
    private final static int max_white_balance_temperature_c = 15000;
    /** Converts a red, green even, green odd and blue components to a white balance temperature.
     *  Note that this is not necessarily an inverse of convertTemperatureToRggb, since many rggb
     *  values can map to the same temperature.
     */
    private int convertRggbToTemperature(RggbChannelVector rggbChannelVector) {
//        if( MyDebug.LOG ) {
//            Log.d(TAG, "temperature:");
//            Log.d(TAG, "    red: " + rggbChannelVector.getRed());
//            Log.d(TAG, "    green even: " + rggbChannelVector.getGreenEven());
//            Log.d(TAG, "    green odd: " + rggbChannelVector.getGreenOdd());
//            Log.d(TAG, "    blue: " + rggbChannelVector.getBlue());
//        }

        float red = rggbChannelVector.getRed();
        float green_even = rggbChannelVector.getGreenEven();
        float green_odd = rggbChannelVector.getGreenOdd();
        float blue = rggbChannelVector.getBlue();
        float green = 0.5f*(green_even + green_odd);

        float max = Math.max(red, blue);
        if( green > max )
            green = max;

        float scale = 255.0f/max;
        red *= scale;
        green *= scale;
        blue *= scale;

        int red_i = (int)red;
        int green_i = (int)green;
        int blue_i = (int)blue;
        int temperature;
        if( red_i == blue_i ) {
            temperature = 6600;
        }
        else if( red_i > blue_i ) {
            // temperature <= 6600
            int t_g = (int)( 100 * Math.exp((green_i + 161.1195681661) / 99.4708025861) );
            if( blue_i == 0 ) {
                temperature = t_g;
            }
            else {
                int t_b = (int)( 100 * (Math.exp((blue_i + 305.0447927307) / 138.5177312231) + 10) );
                temperature = (t_g + t_b)/2;
            }
        }
        else {
            // temperature >= 6700
            if( red_i <= 1 || green_i <= 1 ) {
                temperature = max_white_balance_temperature_c;
            }
            else {
                int t_r = (int)(100 * (Math.pow(red_i / 329.698727446, 1.0 / -0.1332047592) + 60.0));
                int t_g = (int)(100 * (Math.pow(green_i / 288.1221695283, 1.0 / -0.0755148492) + 60.0));
                temperature = (t_r + t_g) / 2;
            }
        }
        temperature = Math.max(temperature, min_white_balance_temperature_c);
        temperature = Math.min(temperature, max_white_balance_temperature_c);
//        if( MyDebug.LOG ) {
//            Log.d(TAG, "    temperature: " + temperature);
//        }
        return temperature;
    }


}

