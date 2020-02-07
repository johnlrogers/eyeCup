package com.ora.android.eyecup.data;

import android.util.Log;

import com.ora.android.eyecup.DatabaseAccess;
import com.ora.android.eyecup.Global;
import com.ora.android.eyecup.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {
        if (username == null || password == null)
            return null; //default for method misuse

        Exception exception;
        //JLR 20200126

        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
//        DatabaseAccess dba = DatabaseAccess.getInstance(Global.Context(this.g));

        try {
            dba.open();
        } catch (NullPointerException e) {
            Log.e("LoginDS:dba.open:Ex", e.toString());
            //todo handle
        }
        Object[][] patInfo = dba.GetParticipantInfo();
        dba.close();

        try {
            Integer patNum = null;
            String dbPass = null;
            for (int i = 0; i < patInfo[0].length; i++) {
                if ((patInfo[0][i]).equals("PatNumber"))
                    patNum = (int)patInfo[1][i];
                else if ((patInfo[0][i]).equals("Password"))
                    password = patInfo[1][i].toString();
            }

            //todo logic next few lines needs looked at
            String strPatNum = "";
            try {
                strPatNum = patNum.toString();
            } catch (NullPointerException e) {
                Log.e("LoginDS:patNum.toString:NPEx", e.toString());
                //todo handle
            }
            LoggedInUser patient = new LoggedInUser(patNum.toString(), "Patient " + strPatNum);
            LoggedInUser administrator = new LoggedInUser("Admin", "Administrator");

            //todo warning Warning:(41, 17) Condition 'username.equals(patNum.toString()) && password.equals(dbPass)' is always 'false'
            //todo warning Warning:(41, 55) Condition 'password.equals(dbPass)' is always 'false' when reached
            if (username.equals(patNum.toString()) && password.equals(dbPass)) {
//                AlwaysService.getmCurrentService().mState = Globals.SVC_EVT_STATE_RUN;        //Participant logged in
                    return new Result.Success<>(patient);
            }

            if (username.equals("Admin") && password.equals("123456")) { //edit: set admin username and password here
//                AlwaysService.getmCurrentService().mState = Globals.SVC_EVT_STATE_ADMIN;      //User Logged in
                return new Result.Success<>(administrator);                                     //edit: go to admin screen
            }

            exception = new Exception("Incorrect username or password.");

        } catch (Exception e) {
            Log.e("LoginDS:CursorGetInnerJSON:resultObj.put:Ex", e.toString());
            //todo handle
            exception = e;
        }
        return new Result.Error(new IOException("Error logging in", exception));
    }


    public void logout() {
//        AlwaysService.getmCurrentService().mState = Globals.SVC_EVT_STATE_LOGIN;
    }
}
