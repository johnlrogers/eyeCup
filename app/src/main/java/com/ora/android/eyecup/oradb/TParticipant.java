package com.ora.android.eyecup.oradb;

public class TParticipant {

    private long PatId = 0;

    private int PatYearId = 0;
    private int PatDeptId = 0;
    private int PatStudyId = 0;
    private int PatLocationId = 0;
    private int PatNumber = 0;
    private String StudyPatNumber = "";
    private String Password = "";

    public long getPatId(){
        return PatId;
    }

    public void setPatId(long PatId){
        this.PatId=PatId;
    }

    public int getPatYearId(){
        return PatYearId;
    }

    public void setPatYearId(int PatYearId){
        this.PatYearId=PatYearId;
    }

    public int getPatDeptId(){
        return PatDeptId;
    }

    public void setPatDeptId(int PatDeptId){
        this.PatDeptId=PatDeptId;
    }

    public int getPatStudyId(){
        return PatStudyId;
    }

    public void setPatStudyId(int PatStudyId){
        this.PatStudyId=PatStudyId;
    }

    public int getPatLocationId(){
        return PatLocationId;
    }

    public void setPatLocationId(int PatLocationId){
        this.PatLocationId=PatLocationId;
    }

    public int getPatNumber(){
        return PatNumber;
    }

    public void setPatNumber(int PatNumber){
        this.PatNumber=PatNumber;
    }

    public String getStudyPatNumber(){
        return StudyPatNumber;
    }

    public void setStudyPatNumber(String StudyPatNumber){
        this.StudyPatNumber=StudyPatNumber;
    }

    public String getPassword(){
        return Password;
    }

    public void setPassword(String Password){
        this.Password=Password;
    }

}
