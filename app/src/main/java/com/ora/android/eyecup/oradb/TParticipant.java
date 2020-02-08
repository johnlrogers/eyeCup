package com.ora.android.eyecup.oradb;

public class TParticipant {

    private long PatId;

    private long PatYearId;
    private long PatDeptId;
    private long PatStudyId;
    private long PatLocationId;
    private int PatNumber;
    private String StudyPatNumber;
    private String Password;

    public long getPatId(){
        return PatId;
    }

    public void setPatId(long PatId){
        this.PatId=PatId;
    }

    public long getPatYearId(){
        return PatYearId;
    }

    public void setPatYearId(long PatYearId){
        this.PatYearId=PatYearId;
    }

    public long getPatDeptId(){
        return PatDeptId;
    }

    public void setPatDeptId(long PatDeptId){
        this.PatDeptId=PatDeptId;
    }

    public long getPatStudyId(){
        return PatStudyId;
    }

    public void setPatStudyId(long PatStudyId){
        this.PatStudyId=PatStudyId;
    }

    public long getPatLocationId(){
        return PatLocationId;
    }

    public void setPatLocationId(long PatLocationId){
        this.PatLocationId=PatLocationId;
    }

    public int getPatNumber(){
        return PatNumber;
    }

    public void setPatNumber(int PatNumber){
        this.PatNumber=PatNumber;
    }

    public String getStudypatnumber(){
        return StudyPatNumber;
    }

    public void setStudypatnumber(String StudyPatNumber){
        this.StudyPatNumber=StudyPatNumber;
    }

    public String getPassword(){
        return Password;
    }

    public void setPassword(String Password){
        this.Password=Password;
    }

}
