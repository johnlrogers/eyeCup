package com.ora.android.eyecup;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {
    private Global global = Global.Context(this); //test change for test push
    private Patient patient;
    private EditText tbxPatNumber;
    private EditText tbxSPNYearId;
    private EditText tbxSPNDeptId;
    private EditText tbxSPNStudyId;
    private EditText tbxSPNLocId;
    private EditText tbxPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        patient = GetPatient();
        tbxPatNumber = global.<EditText>GetView(R.id.tbxPatNumber);
        tbxSPNYearId = global.<EditText>GetView(R.id.tbxSPNYearId);
        tbxSPNDeptId = global.<EditText>GetView(R.id.tbxSPNDeptId);
        tbxSPNStudyId = global.<EditText>GetView(R.id.tbxSPNStudyId);
        tbxSPNLocId = global.<EditText>GetView(R.id.tbxSPNLocId);
        tbxPassword = global.<EditText>GetView(R.id.tbxPassword);
        if (patient != null) {
            Integer[] spnComponents = patient.GetSPNComponents();
            tbxSPNYearId.setText(spnComponents[0]);
            tbxSPNDeptId.setText(spnComponents[1]);
            tbxSPNStudyId.setText(spnComponents[2]);
            tbxSPNLocId.setText(spnComponents[3]);
            tbxPatNumber.setText(spnComponents[4]);
            tbxPassword.setText(patient.password);
        }
        else {
            //todo Warning:(44, 63) String literal in `setText` can not be translated. Use Android resources instead.
            global.<TextView>GetView(R.id.lblPatient).setText("New Patient");
            global.<Button>GetView(R.id.btnSave).setText("Create Patient");
        }
    }
    public void OnClick(View view) {
        boolean success = false;
        if (patient != null) {
            Integer[] spnComponents = patient.GetSPNComponents();
            Integer[] uiSpnComponents = patient.GetSPNComponents();
            if (!spnComponents[4].equals(tbxPatNumber.getText().toString()) || spnComponents[0] != uiSpnComponents[0] ||
                    spnComponents[1] != uiSpnComponents[1] || spnComponents[2] != uiSpnComponents[2] ||
                    spnComponents[3] != uiSpnComponents[3] || spnComponents[4] != uiSpnComponents[4]
                    || !patient.password.equals(tbxPassword.getText().toString()) && NewPatientIsValid())
                success = SetPatient();
        }
        else if (NewPatientIsValid())
            success = SetPatient();
        //todo Warning:(57, 9) 'if' statement has empty body
        if (success)
            ; //edit: this occurs for successful change of patient info
    }
    private boolean SetPatient() { //returns "true" if successful
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());
        boolean success = false;
        Patient p = new Patient();
        p.studyPatNumbers = BuildSPN();
        p.password = tbxPassword.getText().toString();
        try {
            dba.open();
            success = dba.SetParticipantInfo(patient == null, patient.GetSPNComponents()[4].toString(),
                    patient.studyPatNumbers, patient.password);
            dba.close();
        } catch (NullPointerException e){
            Log.e("AdminActivity:SetPatient.SetParticipantInfo:NPEx", e.toString());
            //todo handle
        }
        if (success)
            patient = p;
        return success;
    }
    private Patient GetPatient() { //returns "null" if no patient exists
        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
        try {
            dba.open();
        } catch (NullPointerException e) {
            Log.e("AdminActivity:GetPatient.dba.open:NPEx", e.toString());
            //todo handle
        }
        Object[][] patInfo = dba.GetParticipantInfo();
        dba.close();
        if (patInfo == null)
            return null;
        Patient pat = new Patient();
        for (int i = 0; i < patInfo[0].length; i++) {
            if ((patInfo[0][i]).equals("StudyPatNumber"))
                pat.studyPatNumbers = patInfo[1][i].toString();
            else if ((patInfo[0][i]).equals("Password"))
                pat.password = patInfo[1][i].toString();
        }
        return pat;
    }
    private boolean NewPatientIsValid() {
        int patNumber;
        int spnYearId;
        int spnDeptId;
        int spnStudyId;
        int spnLocId;
        String pass = tbxPassword.getText().toString();
        try {
            patNumber = Integer.parseInt(tbxPatNumber.getText().toString());
            spnYearId = Integer.parseInt(tbxSPNYearId.getText().toString());
            spnDeptId = Integer.parseInt(tbxSPNDeptId.getText().toString());
            spnStudyId = Integer.parseInt(tbxSPNStudyId.getText().toString());
            spnLocId = Integer.parseInt(tbxSPNLocId.getText().toString());
        }
        catch (Exception e) {
            Log.e("AdminActivity:NewPatientIsValid:NPEx", e.toString());
            //todo handle
            return false;
        }
        return patNumber > -1 && patNumber < 10000 && spnYearId > -1 && spnYearId < 100 && spnDeptId > -1 && spnDeptId < 1000 &&
            spnStudyId > -1 && spnStudyId < 10000 && spnLocId > -1 && spnLocId < 1000 && pass.length() > 6; //set all patient info constraints here
    }
    private String BuildSPN() {
        if (NewPatientIsValid())
            return BuildSPNExplicitly(Integer.parseInt(tbxSPNYearId.getText().toString()),
                    Integer.parseInt(tbxSPNDeptId.getText().toString()),
                    Integer.parseInt(tbxSPNStudyId.getText().toString()),
                    Integer.parseInt(tbxSPNLocId.getText().toString()),
                    Integer.parseInt(tbxPatNumber.getText().toString()));
        return null; //default for method misuse
    }
    private String BuildSPNExplicitly(Integer yearId, Integer deptId, Integer studyId, Integer locId, Integer subjectId) {
        if (yearId == null || deptId == null || studyId == null || locId == null || subjectId == null)
            return null; //default for method misuse
        return yearId.toString() + "-" + deptId.toString() + "-" + studyId.toString() + "-" + locId.toString() + "-"
                + subjectId.toString();
    }
    private class Patient {
        public String studyPatNumbers = "";
        public String password = "";
        public Patient() { }
        public Integer[] GetSPNComponents() {
            Integer[] components = new Integer[5];
            if (studyPatNumbers.length() != 0) {
                try {
                    String[] spnComponents = studyPatNumbers.split("-");
                    for (int i = 0; i < spnComponents.length; i++)
                        components[i] = Integer.parseInt(spnComponents[i]);
                }
                catch (Exception e) {
                    Log.e("AdminActivity:Patient:Ex", e.toString());
                    //todo handle
                    components = null;
                } //default for method misuse
            }
            else
                components = null; //default for method misuse
            return components;
        }
    }
    @Override
    public void onBackPressed() {
        global.Back();
        super.onBackPressed();
    }
}
