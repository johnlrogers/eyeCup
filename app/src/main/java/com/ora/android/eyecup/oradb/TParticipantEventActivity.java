package com.ora.android.eyecup.oradb;

public class TParticipantEventActivity {

    private long PatEvtActId;

    private long PatEvtId;
    private long ProtRevEvtActId;
    private int ResponseVal;
    private String ResponseTxt;
    private String ResponsePath;
    private String PatEvtDtUpload;

    private long ActId;
    private long PatEvtActRspId;

    public long getPatEvtActId(){
        return PatEvtActId;
    }

    public long getPatevtid(){
        return PatEvtId;
    }

    public void setPatevtid(long PatEvtId){
        this.PatEvtId=PatEvtId;
    }

    public long getProtrevevtactid(){
        return ProtRevEvtActId;
    }

    public void setProtrevevtactid(long ProtRevEvtActId){
        this.ProtRevEvtActId=ProtRevEvtActId;
    }

    public long getActid(){
        return ActId;
    }

    public void setActid(long ActId){
        this.ActId=ActId;
    }

    public int getResponseVal(){
        return ResponseVal;
    }

    public void setResponseVal(int ResponseVal){
        this.ResponseVal=ResponseVal;
    }

    public long getPatevtactrspid(){
        return PatEvtActRspId;
    }

    public void setPatevtactrspid(long PatEvtActRspId){
        this.PatEvtActRspId=PatEvtActRspId;
    }

    public String getResponseTxt(){
        return ResponseTxt;
    }

    public void setResponseTxt(String ResponseTxt){
        this.ResponseTxt=ResponseTxt;
    }

    public String getResponsePath(){
        return ResponsePath;
    }

    public void setResponsePath(String ResponsePath){
        this.ResponsePath=ResponsePath;
    }

    public String getPatEvtDtUpload(){
        return PatEvtDtUpload;
    }

    public void setPatEvtDtUpload(String PatEvtDtUpload){
        this.PatEvtDtUpload=PatEvtDtUpload;
    }
}
