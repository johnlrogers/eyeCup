package com.ora.android.eyecup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {
    private Global global = Global.Context(this);
    private Patient patient;
    private EditText tbxPatNumber;
    private EditText tbxSPNYearId;
    private EditText tbxSPNDeptId;
    private EditText tbxSPNStudyId;
    private EditText tbxSPNLocId;
    private EditText tbxSPNSubjectId;
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
        tbxSPNSubjectId = global.<EditText>GetView(R.id.tbxSPNSubjectId);
        tbxPassword = global.<EditText>GetView(R.id.tbxPassword);
        if (patient != null) {
            tbxPatNumber.setText(patient.patNumber);
            Integer[] spnComponents = patient.GetSPNComponents();
            tbxSPNYearId.setText(spnComponents[0]);
            tbxSPNDeptId.setText(spnComponents[1]);
            tbxSPNStudyId.setText(spnComponents[2]);
            tbxSPNLocId.setText(spnComponents[3]);
            tbxSPNSubjectId.setText(spnComponents[4]);
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
            if (!patient.patNumber.equals(tbxPatNumber.getText().toString()) || !patient.studyPatNumber.equals(BuildSPN())
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
        boolean success;
        Patient p = new Patient();
        p.patNumber = tbxPatNumber.getText().toString();
        p.studyPatNumber = BuildSPN();
        p.password = tbxPassword.getText().toString();
        try {
            dba.open();
        } catch (NullPointerException ex) {
            //todo handle
        }
        success = dba.SetParticipantInfo(patient == null, patient.patNumber, patient.studyPatNumber, patient.password);
        dba.close();
        if (success)
            patient = p;
        return success;
    }
    private Patient GetPatient() { //returns "null" if no patient exists
        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
        try {
            dba.open();
        } catch (NullPointerException ex) {
            //todo handle
        }
        Object[][] patInfo = dba.GetParticipantInfo();
        dba.close();
        if (patInfo == null)
            return null;
        Patient pat = new Patient();
        for (int i = 0; i < patInfo[0].length; i++) {
            if ((patInfo[0][i]).equals("PatNumber"))
                pat.patNumber = patInfo[1][i].toString();
            else if ((patInfo[0][i]).equals("StudyPatNumber"))
                pat.studyPatNumber = patInfo[1][i].toString();
            else if ((patInfo[0][i]).equals("Password"))
                pat.password = patInfo[1][i].toString();
        }
        return pat;
    }
    private boolean NewPatientIsValid() {
        //todo primitive type?
        Integer patNumInt;
        Integer spnYearId;
        String pass = tbxPassword.getText().toString();
        try {
            patNumInt = Integer.parseInt(tbxPatNumber.getText().toString());
            spnYearId = Integer.parseInt(tbxSPNYearId.getText().toString());
            Integer.parseInt(tbxSPNDeptId.getText().toString());
            Integer.parseInt(tbxSPNStudyId.getText().toString());
            Integer.parseInt(tbxSPNLocId.getText().toString());
            Integer.parseInt(tbxSPNSubjectId.getText().toString());
        }
        catch (Exception ex) { return false; }
        return patNumInt.toString().length() != 4 || spnYearId < 0 || spnYearId > 99 || pass.length() < 7; //all patient info constraints set here
    }
    private String BuildSPN() {
        if (NewPatientIsValid())
            return BuildSPNExplicitly(Integer.parseInt(tbxSPNYearId.getText().toString()),
                    Integer.parseInt(tbxSPNDeptId.getText().toString()),
                    Integer.parseInt(tbxSPNStudyId.getText().toString()), Integer.parseInt(tbxSPNLocId.getText().toString()),
                    Integer.parseInt(tbxSPNSubjectId.getText().toString()));
        return null; //default for method misuse
    }
    private String BuildSPNExplicitly(Integer yearId, Integer deptId, Integer studyId, Integer locId, Integer subjectId) {
        if (yearId == null || deptId == null || studyId == null || locId == null || subjectId == null)
            return null; //default for method misuse
        return yearId.toString() + "-" + deptId.toString() + "-" + studyId.toString() + "-" + locId.toString() + "-"
                + subjectId.toString();
    }
    private class Patient {
        public String patNumber = "";
        public String studyPatNumber = "";
        public String password = "";
        public Patient() { }
        public Integer[] GetSPNComponents() {
            Integer[] components = new Integer[5];
            if (studyPatNumber.length() != 0) {
                try {
                    String[] spnComponents = studyPatNumber.toString().split("-");
                    for (int i = 0; i < spnComponents.length; i++)
                        components[i] = Integer.parseInt(spnComponents[i]);
                }
                catch (Exception ex) { components = null; } //default for method misuse
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
