package com.ora.android.eyecup;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

public class Globals {

//    public static final boolean APP_DEMO_MODE = false;   //control schedule or on-demand
    public static final boolean APP_DEMO_MODE = true;
    public static final int APP_DEMO_MODE_MIN_OPEN = 5;
    public static final int APP_DEMO_MODE_MIN_WARN = 5;
    public static final int APP_DEMO_MODE_MIN_EXPIRE = 10;

    public static final String APP_FILE_PROTOCOLREVISION = "protocolrevision.json";

    public static final String INTENT_SVC_RESTART = "com.ora.android.eyecup.restarter";
    public static final String EYECUP_NOTIFY_CHANNEL = "com.ora.android.eyecup";

    public static final String ALWAYS_SVC_UPDATE_MSG = "AlwaysServiceMsg";
    public static final int ALWAYS_SVC_TIMER_DELAY = 1000;
    public static final int ALWAYS_SVC_TIMER_PERIOD = 10000;

    public static final int ALWAYS_SVC_STATE_CLOSED = 0;                //service closed
    public static final int ALWAYS_SVC_STATE_POLL = 1;                  //service is polling for events
    public static final int ALWAYS_SVC_STATE_EVT_WIN_OPEN = 2;          //an event window is open, not started
    public static final int ALWAYS_SVC_STATE_EVT_WIN_WARN = 3;          //an event window is open and warning threshold reached, not started
    public static final int ALWAYS_SVC_STATE_EVT_WIN_EXPIRE = 4;        //an event window has expired
    public static final int ALWAYS_SVC_STATE_EVT_WIN_RUNNING = 5;       //an event has been started
    public static final int ALWAYS_SVC_STATE_ADMIN = 200;               //Admin user logged in The admin screen is open

    public static final int ALWAYS_SVC_EVENT_NONE = 1100;               //no active events
    public static final int ALWAYS_SVC_EVENT_LOGIN = 1110;              //waiting event login
    public static final int ALWAYS_SVC_EVENT_LOGIN_FAIL = 1119;         //event login failed
    public static final int ALWAYS_SVC_EVENT_STARTED = 1120;            //participant started event (protocol engine running)
    public static final int ALWAYS_SVC_EVENT_ABORT = 1125;
    public static final int ALWAYS_SVC_EVENT_COMPLETE = 1130;
    public static final int ALWAYS_SVC_EVENT_LOGOUT = 1140;
    public static final int ALWAYS_SVC_EVENT_SAVED = 1150;
    public static final int ALWAYS_SVC_EVENT_UPLOAD_STARTED = 1160;
    public static final int ALWAYS_SVC_EVENT_UPLOAD_ABORT = 1165;
    public static final int ALWAYS_SVC_EVENT_UPLOAD_COMPLETE = 1169;

    public static final int LOGIN_PARTICPANT_ERR_NO_ERR = 10;
    public static final int LOGIN_PARTICPANT_ERR_ID = 11;
    public static final int LOGIN_PARTICPANT_ERR_PW = 12;
    public static final int LOGIN_ADMIN_ERR_NO_ERR = 20;
    public static final int LOGIN_ADMIN_ERR_ID = 21;
    public static final int LOGIN_ADMIN_ERR_PW = 22;
    public static final int LOGIN_ERR_UNK = 99;
    public static final String LOGIN_ADMIN_NAME = "Admin";
    public static final String LOGIN_ADMIN_PW = "123456";

    public static final String APP_DIR_PROTOCOL = "Protocol";
    public static final String APP_DIR_PROTOCOL_ARCHIVE = "Protocol/Archive";
    public static final String APP_DIR_PARTICIPANTS = "Participants";
    public static final String APP_DIR_DATA = "databases";
    public static final String APP_DIR_DATA_ARCHIVE = "databases/Archive";
    public static final String APP_DIR_DATA_FRESH = "databases/Fresh";
//    public static final String APP_DATA_DBNAME = "ORADb.db";
    public static final String APP_DATA_DBNAME = "ORADb_V3.db";
    public static final String APP_ASSET_DBNAME = "databases/ORADb_V3.db";

    public static final int ACTIVITY_TYPE_INSTRUCTION = 1;
    public static final int ACTIVITY_TYPE_QUESTION = 2;
    public static final int ACTIVITY_TYPE_PICTURE = 3;

    public static final int RESPONSE_TYPE_NONE = 1;
    public static final int RESPONSE_TYPE_RDB = 2;
    public static final int RESPONSE_TYPE_LST = 3;
    public static final int RESPONSE_TYPE_SLD = 4;
    public static final int RESPONSE_TYPE_CHK = 5;

    public static final String ACTIVITY_APPLYTO_NA = "N";
    public static final String ACTIVITY_APPLYTO_LEFT = "L";
    public static final String ACTIVITY_APPLYTO_RIGHT = "R";
    public static final String ACTIVITY_APPLYTO_BOTH = "B";

    public static int RDB_INVERSE_SEP_FACTOR_VERT = 32;
    public static int RDB_TEXT_SIZE = 15;

    public static final int DT_FMT_FULL_DISPLAY = 1;
    public static final int DT_FMT_FULL_FILENAME = 2;
    public static final int DT_FMT_DATE = 3;
    public static final int DT_FMT_TIME = 4;

    public Date getDate() {
        return Calendar.getInstance().getTime();
    }

    public LocalDate getLocalDate() {
        return LocalDate.now();
    }

    //Using Java8
    //if equal return 0
    //if start < end return 1
    //if end < start return -1
    public static int compareTimeJava8(String startTimeStr, String endTimeStr) {

        LocalDate today = LocalDate.now();
        String startTimeStrT = today + " " + startTimeStr;
        String endTimeStrT = today + " " + endTimeStr;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {

            LocalDateTime startTime = LocalDateTime.parse(startTimeStrT, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStrT, formatter);

            Duration d = Duration.between(startTime, endTime);

//            System.out.println("dur " + d.getSeconds());
            if (d.getSeconds() == 0) {
//                System.out.println("Both Start time and End Time are equal");
                return 0;
            } else if (d.getSeconds() > 0) {
                System.out.println("Start time is less than end time");
                return 1;
            } else {
                System.out.println("Start time is greater than end time");
                return -1;
            }
        } catch (DateTimeParseException e) {
            Log.e("Globals:compareTimeJava8:DTPEx", e.toString());
            //todo handle
            return -9;
        }

    }
    public String GetDateStr(int iFormatCode, Date dt) {
        String str;
        SimpleDateFormat fmtDt;

        switch (iFormatCode) {
            case DT_FMT_FULL_DISPLAY:
                fmtDt = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa");
                break;
            case DT_FMT_FULL_FILENAME:
                fmtDt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                break;
            case DT_FMT_DATE:
                fmtDt = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case DT_FMT_TIME:
                fmtDt = new SimpleDateFormat("HH:mm:ss");
                break;
            default:
                fmtDt = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa");
                break;
        }

        str = fmtDt.format(dt);

        return str;
    }

    public byte[] ByteArrayFromString(String str) {

        int iLen;
        iLen = str.length();

        byte[] bytes = new byte[iLen+1];

        try {
            bytes = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.e("Globals:ByteArrayFromString:UnsupportedEncodeEx", e.toString());
            //todo handle
        }
        return bytes;
    }

    public String StringFromByteArray(byte[] bytes) {

        int iLen;
        String str = "";

        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.e("Globals:StringFromByteArray:UnsupportedEncodeEx", e.toString());
            //todo handle
        }
        return str;
    }

    public int mod(int x, int y)
    {
        int result = x % y;
        if (result < 0)
            result += y;
        return result;
    }
}
