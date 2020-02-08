package com.ora.android.eyecup.oradb;

public class TParticipantEvent {

    private long PatEvtId;

    private long PatId;
    private long DeviceId;
    private long ProtRevEvtId;
    private String PatEvtDtStart;
    private String PatEvtDtEnd;
    private String PatEvtDtUpload;
    private String PatEvtFileName;
    private int PatEvtResponseCnt;
    private int PatEvtPictureCnt;

    private long StudyId;
    private long SiteId;
    private String StudyPatNumber;

    //todo database needs to be updated to make this autoincrement like PatId and PatEvtActId
    public void setPatEvtId(long PatEvtId){
        this.PatEvtId=PatEvtId;
    }

    public long getPatEvtId(){
        return PatEvtId;
    }

    public long getPatId(){
        return PatId;
    }

    public void setPatId(long PatId){
        this.PatId=PatId;
    }

    public long getDeviceId(){
        return DeviceId;
    }

    public void setDeviceId(long DeviceId){
        this.DeviceId=DeviceId;
    }

    public long getProtrevevtid(){
        return ProtRevEvtId;
    }

    public void setProtrevevtid(long ProtRevEvtId){
        this.ProtRevEvtId=ProtRevEvtId;
    }

    public String getPatEvtDtStart(){
        return PatEvtDtStart;
    }

    public void setPatEvtDtStart(String PatEvtDtStart){
        this.PatEvtDtStart=PatEvtDtStart;
    }

    public String getPatEvtDtEnd(){
        return PatEvtDtEnd;
    }

    public void setPatEvtDtEnd(String PatEvtDtEnd){
        this.PatEvtDtEnd=PatEvtDtEnd;
    }

    public String getPatEvtDtUpload(){
        return PatEvtDtUpload;
    }

    public void setPatEvtDtUpload(String PatEvtDtUpload){
        this.PatEvtDtUpload=PatEvtDtUpload;
    }

    public String getPatEvtFileName(){
        return PatEvtFileName;
    }

    public void setPatEvtFileName(String PatEvtFileName){
        this.PatEvtFileName=PatEvtFileName;
    }

    public int getPatEvtResponseCnt(){
        return PatEvtResponseCnt;
    }

    public void setPatEvtResponseCnt(int PatEvtResponseCnt){
        this.PatEvtResponseCnt=PatEvtResponseCnt;
    }

    public int getPatEvtPictureCnt(){
        return PatEvtPictureCnt;
    }

    public void setPatEvtPictureCnt(int PatEvtPictureCnt){
        this.PatEvtPictureCnt=PatEvtPictureCnt;
    }

    public long getStudyId(){
        return StudyId;
    }

    public void setStudyId(long StudyId){
        this.StudyId=StudyId;
    }

    public long getSiteid(){
        return SiteId;
    }

    public void setSiteid(long SiteId){
        this.SiteId=SiteId;
    }

    public String getStudypatnumber(){
        return StudyPatNumber;
    }

    public void setStudypatnumber(String StudyPatNumber){
        this.StudyPatNumber=StudyPatNumber;
    }

}
