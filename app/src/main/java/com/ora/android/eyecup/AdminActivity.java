package com.ora.android.eyecup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ora.android.eyecup.oradb.TDevice;
import com.ora.android.eyecup.oradb.TParticipant;

import androidx.appcompat.app.AppCompatActivity;

import static com.ora.android.eyecup.Globals.MIN_PASSWORD_LEN;

public class AdminActivity extends AppCompatActivity {
    private Global global = Global.Context(this);

//    private Patient patient;
//20200211
    private TParticipant mParticipant;
    private TDevice mDevice;

    private String mstrPW = "";
    private String mstrSPN = "";

    private boolean isBound = false;
    private AlwaysService alwaysService;
//20200211 end

    private EditText tbxPatNumber;
    private EditText tbxSPNYearId;
    private EditText tbxSPNDeptId;
    private EditText tbxSPNStudyId;
    private EditText tbxSPNLocId;
    private EditText tbxPassword;
    private EditText tbxPasswordConfirm;
    private TextView lblSPN;
    private EditText tbxDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

//        patient = GetPatient();
//20200211
        mParticipant = new TParticipant();      //patId > 0 if success
        getParticipant();                       //patId > 0 if success
        mDevice = new TDevice();                //deviceId > 0 if success
        getDevice();                            //deviceId > 0 if success
//20200211 end
        tbxSPNYearId = global.GetView(R.id.tbxSPNYearId);
        tbxSPNDeptId = global.GetView(R.id.tbxSPNDeptId);
        tbxSPNStudyId = global.GetView(R.id.tbxSPNStudyId);
        tbxSPNLocId = global.GetView(R.id.tbxSPNLocId);
        tbxPatNumber = global.GetView(R.id.tbxPatNumber);
        tbxPassword = global.GetView(R.id.tbxPassword);
//20200211
        tbxPasswordConfirm = global.GetView(R.id.tbxPasswordConfirm);
        lblSPN = global.GetView(R.id.lblSPN);
        tbxDevice = global.GetView(R.id.tbxDeviceId);
//        if (patient != null) {
//            Integer[] spnComponents = patient.GetSPNComponents();
//            tbxSPNYearId.setText(spnComponents[0]);
//            tbxSPNDeptId.setText(spnComponents[1]);
//            tbxSPNStudyId.setText(spnComponents[2]);
//            tbxSPNLocId.setText(spnComponents[3]);
//            tbxPatNumber.setText(spnComponents[4]);
//            tbxPassword.setText(patient.password);
//        }
        if (mParticipant.getPatId() != 0) {
            mstrPW = mParticipant.getPassword();
            mstrSPN = mParticipant.getStudyPatNumber();

            tbxSPNYearId.setText(String.valueOf(mParticipant.getPatYearId()));
            tbxSPNDeptId.setText(String.valueOf(mParticipant.getPatDeptId()));
            tbxSPNStudyId.setText(String.valueOf(mParticipant.getPatStudyId()));
            tbxSPNLocId.setText(String.valueOf(mParticipant.getPatLocationId()));
            tbxPatNumber.setText(String.valueOf(mParticipant.getPatNumber()));
//            tbxPassword.setText("");
            BuildSPNFmt(mParticipant.getPatYearId(),
                    mParticipant.getPatDeptId(),
                    mParticipant.getPatStudyId(),
                    mParticipant.getPatLocationId(),
                    mParticipant.getPatNumber());
        }
//20200211 end
        else {
            //todo Warning:(44, 63) String literal in `setText` can not be translated. Use Android resources instead.
//            global.<TextView>GetView(R.id.lblPatient).setText("New Patient");
//            global.<Button>GetView(R.id.btnSave).setText("Create Patient");
        }
//20200211
        if (mDevice.getDeviceId() != 0) {
            tbxDevice.setText(String.valueOf(mDevice.getDeviceAppId()));
        }
//20200211 end

        final Button button = findViewById(R.id.btnCamSettings);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OpenCameraSettings();
            }
        });

        tbxSPNYearId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                PrepareSPNFmt();
            }
        });
        tbxSPNDeptId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                PrepareSPNFmt();
            }
        });
        tbxSPNStudyId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                PrepareSPNFmt();
            }
        });
        tbxSPNLocId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                PrepareSPNFmt();
            }
        });
        tbxPatNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                PrepareSPNFmt();
            }
        });
    }

    public void OpenCameraSettings() {
        Intent intent;

        intent = new Intent(this, AdminCameraActivity.class);     //Admin CameraActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void OnClick(View view) {
        boolean success = false;

        if (mParticipant != null
            && PatientIsValid()
            && PatientPWIsValid()) {
            if (   (mParticipant.getPatNumber() != Integer.parseInt(tbxPatNumber.getText().toString()))
                || (mParticipant.getPatYearId() != Integer.parseInt(tbxSPNYearId.getText().toString()))
                || (mParticipant.getPatDeptId() != Integer.parseInt(tbxSPNDeptId.getText().toString()))
                || (mParticipant.getPatStudyId() != Integer.parseInt(tbxSPNStudyId.getText().toString()))
                || (mParticipant.getPatLocationId() != Integer.parseInt(tbxSPNLocId.getText().toString()))
                || (!mParticipant.getPassword().equals(tbxPassword.getText().toString()))
                )
                success = setParticipant();
        }
        else if (PatientIsValid() && PatientPWIsValid()) {
            success = setParticipant();
        }

        if (mDevice != null
                && DeviceIsValid()) {
            if (!mDevice.getDeviceAppId().equals(tbxDevice.getText().toString())) {
                success = setDevice();
            }

        }
        //todo Warning:(57, 9) 'if' statement has empty body
        if (success)
            ; //edit: this occurs for successful change of patient info

        if (isBound) {
            alwaysService.saveAdminChanges();       //refresh service with new db info
            try {
                finish();

            } catch (Exception e) {
                Log.e("AdminActivity:onClick:Finish", e.toString());
                //todo handle
            }
        }
    }

//    private boolean PatDataChanged () {
//        boolean bChgd = false;
//
//        bChgd =
//        return bChgd;
//    }
    private boolean setParticipant() { //returns "true" if successful
        boolean success = false;
        boolean bChgPW = false;
        boolean bNewPat = false;

        TParticipant pat = new TParticipant();
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());
        try {
            dba.open();
            pat.setPatNumber(Integer.parseInt(tbxPatNumber.getText().toString()));
            pat.setPatYearId(Integer.parseInt(tbxSPNYearId.getText().toString()));
            pat.setPatDeptId(Integer.parseInt(tbxSPNDeptId.getText().toString()));
            pat.setPatStudyId(Integer.parseInt(tbxSPNStudyId.getText().toString()));
            pat.setPatLocationId(Integer.parseInt(tbxSPNLocId.getText().toString()));
            pat.setStudyPatNumber(lblSPN.getText().toString());
            if (!mParticipant.getPassword().equals(tbxPassword.getText().toString())) {
                pat.setPassword(tbxPassword.getText().toString());
                bChgPW = true;
            }
            success = dba.SetParticipantInfo(bNewPat, bChgPW, pat);
            if (success) {
                if (isBound) {
                    alwaysService.LogMsg("Save Participant Info: " + pat.getStudyPatNumber());
                }
            }
        } catch (NullPointerException e){
            Log.e("AdminActivity:SetPatient.SetParticipantInfo:NPEx", e.toString());
            //todo handle
        } finally {
            dba.close();
        }
        if (success)
            mParticipant = pat;

        return success;
    }

    private boolean setDevice() { //returns "true" if successful
        boolean success = false;

        TDevice dvc = new TDevice();
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());
        try {
            dba.open();
            dvc.setDeviceAppId(tbxDevice.getText().toString());
            success = dba.SetDeviceInfo(dvc);
            if (success) {
                if (isBound) {
                    alwaysService.LogMsg("Save Device Info: " + dvc.getDeviceAppId());
                }
            }
        } catch (NullPointerException e){
            Log.e("AdminActivity:SetPatient.SetParticipantInfo:NPEx", e.toString());
            //todo handle
        } finally {
            dba.close();
        }
        if (success)
            mDevice = dvc;

        return success;
    }
//    private boolean SetPatient() { //returns "true" if successful
//        boolean success = false;
//        Patient p = new Patient();
//        p.studyPatNumbers = BuildSPN();
//        p.password = tbxPassword.getText().toString();
//        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());
//        try {
//            dba.open();
//            success = dba.SetParticipantInfo(patient == null, patient.GetSPNComponents()[4].toString(),
//                    patient.studyPatNumbers, patient.password);
//            dba.close();
//        } catch (NullPointerException e){
//            Log.e("AdminActivity:SetPatient.SetParticipantInfo:NPEx", e.toString());
//            //todo handle
//        }
//        if (success)
//            patient = p;
//        return success;
//    }

//20200211
    //get the Participant object
    private TParticipant getParticipant() {
        TParticipant pat = new TParticipant();
        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
        try {
            dba.open();
            mParticipant = dba.getParticipant();
            //todo what if patId 0?
    //        if (mParticipant.getPatId() == 0)
    //        {
    //
    //        }
        } catch (NullPointerException e) {
            Log.e("AdminActivity:getParticipant:NPEx", e.toString());
            //todo handle
        } finally {
            dba.close();
        }
        return pat;
    }

    //get the Device object
    private TDevice getDevice() {
        TDevice dvc = new TDevice();
        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
        try {
            dba.open();
            mDevice = dba.getDevice();
            //todo what if deviceId 0?
//        if (mDevice.getDeviceId() == 0)
//        {
//
//        }
        } catch (NullPointerException e) {
            Log.e("AdminActivity:getParticipant:NPEx", e.toString());
            //todo handle
        } finally {
            dba.close();
        }
        return dvc;
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    private void PrepareSPNFmt() {
        int yearId = 0;
        int deptId = 0;
        int studyId = 0;
        int locId = 0;
        int subjectId = 0;

        if (!isEmpty(tbxSPNYearId)) yearId = Integer.parseInt(tbxSPNYearId.getText().toString());
        if (!isEmpty(tbxSPNDeptId)) deptId = Integer.parseInt(tbxSPNDeptId.getText().toString());
        if (!isEmpty(tbxSPNStudyId)) studyId = Integer.parseInt(tbxSPNStudyId.getText().toString());
        if (!isEmpty(tbxSPNLocId)) locId = Integer.parseInt(tbxSPNLocId.getText().toString());
        if (!isEmpty(tbxPatNumber)) subjectId = Integer.parseInt(tbxPatNumber.getText().toString());

        BuildSPNFmt(yearId, deptId, studyId, locId, subjectId);
    }

    private String BuildSPNFmt(int yearId, int deptId, int studyId, int locId, int subjectId) {
        String strSPN = "";

        strSPN = strSPN + String.format ("%02d", yearId) + "-";
        strSPN = strSPN + String.format ("%02d", deptId) + "-";
        strSPN = strSPN + String.format ("%03d", studyId) + "-";
        strSPN = strSPN + String.format ("%03d", locId) + "-";
        strSPN = strSPN + String.format ("%04d", subjectId);

        lblSPN.setText(strSPN);

        return strSPN;
    }

//20200211 end

//    private Patient GetPatient() { //returns "null" if no patient exists
//        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
//        try {
//            dba.open();
//        } catch (NullPointerException e) {
//            Log.e("AdminActivity:GetPatient.dba.open:NPEx", e.toString());
//            //todo handle
//        }
//        Object[][] patInfo = dba.GetParticipantInfo();
//        dba.close();
//        if (patInfo == null)
//            return null;
//        Patient pat = new Patient();
//        for (int i = 0; i < patInfo[0].length; i++) {
//            if ((patInfo[0][i]).equals("StudyPatNumber"))
//                pat.studyPatNumbers = patInfo[1][i].toString();
//            else if ((patInfo[0][i]).equals("Password"))
//                pat.password = patInfo[1][i].toString();
//        }
//        return pat;
//    }

    //set all patient info constraints here
    private boolean PatientIsValid() {
        int iPat;
        int iYear;
        int iDept;
        int iStudy;
        int iLoc;

        try {
            iPat = Integer.parseInt(tbxPatNumber.getText().toString());
            iYear = Integer.parseInt(tbxSPNYearId.getText().toString());
            iDept = Integer.parseInt(tbxSPNDeptId.getText().toString());
            iStudy = Integer.parseInt(tbxSPNStudyId.getText().toString());
            iLoc = Integer.parseInt(tbxSPNLocId.getText().toString());
        }
        catch (Exception e) {
            Log.e("AdminActivity:PatientIsValid:NPEx", e.toString());
            //todo handle
            return false;
        }
        return iYear > -1 && iYear < 100
                && iDept > -1 && iDept < 100
                && iStudy > -1 && iStudy < 1000
                && iLoc > -1 && iLoc < 1000
                && iPat > -1 && iPat < 10000;
    }
    //set all patient info constraints here
    private boolean DeviceIsValid() {
        long lDvc;

        try {
            lDvc = Long.parseLong(tbxDevice.getText().toString());
        }
        catch (Exception e) {
            Log.e("AdminActivity:PatientIsValid:NPEx", e.toString());
            //todo handle
            return false;
        }
        return lDvc > -1 && lDvc < 100000000;
    }

    //set all patient info constraints here
    private boolean PatientPWIsValid() {
        String pass = tbxPassword.getText().toString();
        String passconf = tbxPasswordConfirm.getText().toString();

        //todo Warning:(444, 20) Condition 'passconf.length() >= MIN_PASSWORD_LEN' is always 'true'
        return pass.equals(passconf)
                && (pass.trim().length() >= MIN_PASSWORD_LEN)
                && (passconf.trim().length() >= MIN_PASSWORD_LEN);
    }

//    private String BuildSPN() {
//        if (NewPatientIsValid()) {
//            return BuildSPNExplicitly(Integer.parseInt(tbxSPNYearId.getText().toString()),
//                    Integer.parseInt(tbxSPNDeptId.getText().toString()),
//                    Integer.parseInt(tbxSPNStudyId.getText().toString()),
//                    Integer.parseInt(tbxSPNLocId.getText().toString()),
//                    Integer.parseInt(tbxPatNumber.getText().toString()));
//        }
//        return null; //default for method misuse
//    }

//    private String BuildSPNExplicitly(Integer yearId, Integer deptId, Integer studyId, Integer locId, Integer subjectId) {
//        if (yearId == null
//                || deptId == null
//                || studyId == null
//                || locId == null
//                || subjectId == null)
//            return null; //default for method misuse
//        return yearId.toString() + "-" + deptId.toString() + "-" + studyId.toString() + "-" + locId.toString() + "-"
//                + subjectId.toString();
//    }

//    private class Patient {
//        public String studyPatNumbers = "";
//        public String password = "";
//        public Patient() { }
//        public Integer[] GetSPNComponents() {
//            Integer[] components = new Integer[5];
//            if (studyPatNumbers.length() != 0) {
//                try {
//                    String[] spnComponents = studyPatNumbers.split("-");
//                    for (int i = 0; i < spnComponents.length; i++)
//                        components[i] = Integer.parseInt(spnComponents[i]);
//                }
//                catch (Exception e) {
//                    Log.e("AdminActivity:Patient:Ex", e.toString());
//                    //todo handle
//                    components = null;
//                } //default for method misuse
//            }
//            else
//                components = null; //default for method misuse
//            return components;
//        }
//    }

    @Override
    public void onBackPressed() {
        global.Back();
        super.onBackPressed();
//20200302
        if (isBound) {
            alwaysService.CancelAdminChanges();       //cancel
            try {
                finish();
            } catch (Exception e) {
                Log.e("MainActivity:onCreate:Finish", e.toString());
                //todo handle
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AlwaysService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AlwaysService.LocalBinder binder = (AlwaysService.LocalBinder) service;
            alwaysService = binder.getService();
            isBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

}
