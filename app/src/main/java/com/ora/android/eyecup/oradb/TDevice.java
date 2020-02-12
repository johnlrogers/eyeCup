package com.ora.android.eyecup.oradb;

public class TDevice {

    private long DeviceId = 0;

    private String DeviceAppId;

    public long getDeviceId(){
        return DeviceId;
    }

    public void setDeviceId(long DeviceId){
        this.DeviceId=DeviceId;
    }

    public String getDeviceAppId(){
        return DeviceAppId;
    }

    public void setDeviceAppId(String DeviceAppId){
        this.DeviceAppId=DeviceAppId;
    }

}
