package com.ora.android.eyecup.oradb;

public class TParticipantEventHistory {

    private long PatEvtHstId;

    private long PatEvtId;
    private long PatEvtHstStatId;
    private String PatEvtHstHstComment;
    private String PatEvtHstHstCreateUsr;

    public long getPatEvtHstId(){
        return PatEvtHstId;
    }

    public long getPatevtid(){
        return PatEvtId;
    }

    public void setPatevtid(long PatEvtId){
        this.PatEvtId=PatEvtId;
    }

    public long getPatevthststatid(){
        return PatEvtHstStatId;
    }

    public void setPatevthststatid(long PatEvtHstStatId){
        this.PatEvtHstStatId=PatEvtHstStatId;
    }

    public String getPatevthsthstcomment(){
        return PatEvtHstHstComment;
    }

    public void setPatevthsthstcomment(String PatEvtHstHstComment){
        this.PatEvtHstHstComment=PatEvtHstHstComment;
    }

    public String getPatevthsthstcreateusr(){
        return PatEvtHstHstCreateUsr;
    }

    public void setPatevthsthstcreateusr(String PatEvtHstHstCreateUsr){
        this.PatEvtHstHstCreateUsr=PatEvtHstHstCreateUsr;
    }
}
