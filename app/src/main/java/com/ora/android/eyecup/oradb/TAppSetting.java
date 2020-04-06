package com.ora.android.eyecup.oradb;

public class TAppSetting {

    private long AppSetId = 0;
    private String AppSetName;
    private int AppSetInt = 0;
    private float AppSetReal = 0f;
    private String AppSetText;
    private float AppSetMin = 0f;
    private float AppSetMax = 0f;

    public long getAppSetId(){
        return AppSetId;
    }

    public String getAppSetName(){
        return AppSetName;
    }
    public void setAppSetName(String AppSetName){
        this.AppSetName=AppSetName;
    }

    public int getAppSetInt(){
        return AppSetInt;
    }
    public void setAppSetInt(int AppSetInt){
        this.AppSetInt=AppSetInt;
    }

    public float getAppSetReal(){
        return AppSetReal;
    }
    public void setAppSetReal(float AppSetReal){
        this.AppSetReal=AppSetReal;
    }

    public String getAppSetText(){
        return AppSetText;
    }
    public void setAppSetText(String AppSAppSetTextetName){
        this.AppSetText=AppSetText;
    }

    public float getAppSetMin(){
        return AppSetMin;
    }
    public void setAppSetMin(float AppSetMin){
        this.AppSetMin=AppSetMin;
    }

    public float getAppSetMax(){ return AppSetMax; }
    public void setAppSetMax(float AppSetMax){
        this.AppSetMax=AppSetMax;
    }

}
