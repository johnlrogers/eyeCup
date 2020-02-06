package com.ora.android.eyecup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.ora.android.eyecup.json.ActivityResponse;
import com.ora.android.eyecup.json.EventActivity;
import com.ora.android.eyecup.json.ParticipantEvent;
import com.ora.android.eyecup.json.PatEventPicture;
import com.ora.android.eyecup.json.PatEventResponse;
import com.ora.android.eyecup.json.ProtocolRevEvent;
import com.ora.android.eyecup.json.ProtocolRevision;
import com.ora.android.eyecup.ui.login.LoginActivity;
import com.ora.android.eyecup.utilities.FileHelper;
import com.ora.android.eyecup.utilities.Notification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;

import static com.ora.android.eyecup.Globals.ACTIVITY_TYPE_INSTRUCTION;
import static com.ora.android.eyecup.Globals.ACTIVITY_TYPE_PICTURE;
import static com.ora.android.eyecup.Globals.ACTIVITY_TYPE_QUESTION;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_ABORT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_COMPLETE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_LOGIN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_LOGIN_FAIL;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_LOGOUT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_NONE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_SAVED;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_STARTED;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_UPLOAD_ABORT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_UPLOAD_COMPLETE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_UPLOAD_STARTED;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_ADMIN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_EXPIRE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_OPEN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_RUNNING;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_WARN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_POLL;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_TIMER_DELAY;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_TIMER_PERIOD;
import static com.ora.android.eyecup.Globals.APP_ASSET_DBNAME;
import static com.ora.android.eyecup.Globals.APP_DATA_DBNAME;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE_MIN_EXPIRE;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE_MIN_OPEN;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE_MIN_WARN;
import static com.ora.android.eyecup.Globals.APP_DIR_DATA;
import static com.ora.android.eyecup.Globals.APP_DIR_DATA_ARCHIVE;
import static com.ora.android.eyecup.Globals.APP_DIR_DATA_FRESH;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANTS;
import static com.ora.android.eyecup.Globals.APP_DIR_PROTOCOL;
import static com.ora.android.eyecup.Globals.APP_DIR_PROTOCOL_ARCHIVE;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_ERR_NO_ERR;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_ERR_PW;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_NAME;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_PW;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_ID;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_NO_ERR;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_PW;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_CHK;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_LST;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_NONE;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_RDB;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_SLD;

public class AlwaysService extends Service {

//    protected static final int NOTIFICATION_ID = 2567;
    private static Random random = new Random();
    private static int iSeed = random.nextInt(9999 - 1001 ) + 1001;    //random 1001 to 9998
    protected static final int NOTIFICATION_ID = iSeed;
    private static String TAG = "AlwaysService";
    private static AlwaysService mCurrentService;
    private int counter = 0;

    private Globals glob;                                                   //global utilities
    private FileHelper fh = new FileHelper();                               //file helper

    private boolean bStateMachineRunning = false;                           //is state machine running?
    private int mState = ALWAYS_SVC_STATE_POLL;                             //current service state
    private int mPrevState = -1;                                            //previous service state
    private int mEventState = ALWAYS_SVC_EVENT_NONE;                        //current event state
    private int mPrevEventState = -1;                                       //previous event state

    private LocalDateTime mDtNextEvtStart = null;                           //init next event datetime
    private LocalDateTime mDtNextEvtWarn = null;                            //init next event datetime
    private LocalDateTime mDtNextEvtExpire = null;                            //init next event datetime
    ArrayList<ProtocolRevision> mArrProtRev = new ArrayList<>();            //All Protocol Revs (should only be one, add for consistency)
    ArrayList<ProtocolRevEvent> mArrProtRevEvt = new ArrayList<>();         //All Protocol Rev Events
    ArrayList<EventActivity> mArrProtRevEvtAct = new ArrayList<>();         //All Protocol Rev Event Activities
    ArrayList<ActivityResponse> mArrProtRevEvtActRsp = new ArrayList<>();   //All Protocol Rev Event Activity Responses

    private int miCurProtRevIdx = 0;                                        //Current mArrProtRev Index
    private int miCurProtRevEvtIdx = 0;                                     //Current mArrProtRevEvt Index
    private int miCurActIdx = 0;                                            //Current mArrProtRevEvtAct Index

    private long mlCurProtRevId = 0;                                          //current Protocol Rev Id
    private long mlCurProtRevEvtId = 0;                                       //Current Protocol Rev Event Id
    private long mlCurProtRevEvtActId = 0;                                    //Current Protocol Rev Event Activity Id

    private ParticipantEvent mPatEvt = new ParticipantEvent();
    private PatEventResponse mPatEvtActRsp = new PatEventResponse();
    private String mstrPatNumber = "20-003-0001-234-0004";                  //todo use Participant object, get form dB
    private int miPatId = 4;


    /**************** start methods ***********************/
    public AlwaysService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        if (APP_DEMO_MODE) {                            //Demo mode?
//            mState = ALWAYS_SVC_STATE_EVT_WIN_OPEN;     //go straight to event
//        }

        glob = new Globals();                   //init Globals object
        InitDirectoryTree();                    //create directory tree if not present

        //        GetProtocolFromJSON();
        GetProtocolFromDb();                    //get initial default protocol from OraDb.db

        restartForeground();                    //start service if not running
        mCurrentService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        counter = 0;

        if (intent == null) {                               // Restart if it was killed by Android
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(this);
        }

        restartForeground();

        startTimer();

        return START_STICKY;    //tell android to restart if killed
    }

    /** "The system allows apps to call Context.startForegroundService() even while the app is in the background.
     *  However, the app must call that service's startForeground() method within five seconds after the service is created." */
    public void restartForeground() {
        Log.i(TAG, "restartForeground");
        try {
            Notification notification = new Notification();
            startForeground(NOTIFICATION_ID, notification.setNotification(this, "AlwaysService", "Notification from AlwaysService", R.drawable.ic_sleep));
            Log.i(TAG, "restartForeground success");
                startTimer();
        } catch (Exception e) {
            Log.e(TAG, "AlwaysService:restartForeground:Ex: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");

        Intent broadcastIntent = new Intent(Globals.INTENT_SVC_RESTART);    //set restart intent
        sendBroadcast(broadcastIntent);                                     //broadcast intent
        stopTimerTask();                                                    //stop timer
    }

    /** Called when the process is killed by Android */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved");

        Intent broadcastIntent = new Intent(Globals.INTENT_SVC_RESTART);    //set restart intent
        sendBroadcast(broadcastIntent);                                     //broadcast intent

        // stopTimerTask();     //skip: on some phones this may stop the timer AFTER it was restarted
    }

    /** static to avoid multiple timers to be created when the service is called several times */
    private static Timer timer;
    private static TimerTask timerTask;
//    long oldTime = 0;

    public void startTimer() {
        Log.i(TAG, "startTimer()");

        stopTimerTask();                    //stop if already running
        timer = new Timer();                //create new timer

        initializeTimerTask();              //init

        Log.i(TAG, "Scheduling timer");

        timer.schedule(timerTask, ALWAYS_SVC_TIMER_DELAY, ALWAYS_SVC_TIMER_PERIOD); //   //schedule the timer
    }

    /** Init Log counter */
    public void initializeTimerTask() {
        Log.i(TAG, "initializeTimerTask()");
        timerTask = new TimerTask() {
            public void run() {
//                Log.d(TAG, "Time running, timer loop: " + (counter++));     //todo turn off in release?
                AlwaysServiceStateMachine();            //execute the State Machine
            }
        };
    }

    /** stop the task */
    public void stopTimerTask() {
        Log.i(TAG, "stopTimerTask()");
        if (timer != null) {        //timer running?
            timer.cancel();             //stop it
            timer = null;
        }
    }

    /** get current service object */
    public static AlwaysService getmCurrentService() {
        return mCurrentService;
    }

    /** set current service object */
    public static void setmCurrentService(AlwaysService mCurrentService) {
        AlwaysService.mCurrentService = mCurrentService;
    }

    //    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public AlwaysService getService() {
            return AlwaysService.this;
        }
    }

    /** Where am I in the state machine?  What should I be doing */ //todo Do something with state
    public int AlwaysServiceStateMachine() {
//        Log.i(TAG, "AlwaysServiceStateMachine(), State: " + mPrevState + "->" + mState);                    //Log State
//        Log.i(TAG, "AlwaysServiceStateMachine(), EventState: " + mPrevEventState + "->" + mEventState);     //Log Event State

        Intent intent;

        if ((mPrevState != mState) || (mPrevEventState != mEventState)){        //Service or Event State changed?
//            if (mPrevState != mState) {
                Log.i(TAG, "AlwaysServiceStateMachine(), State: " + mPrevState + " -> " + mState);                    //Log State
//            }
//            if (mPrevEventState != mEventState) {
                Log.i(TAG, "AlwaysServiceStateMachine(), EventState: " + mPrevEventState + " -> " + mEventState);     //Log Event State
//            }
            mPrevState = mState;                                                    //set Prev Svc state
            mPrevEventState = mEventState;                                          //set prev Event state
            String strNextEventTime;                                                //string for next event time
            String strExpireTime = getEvtExpireDtStr();                             //string for expire event time
            switch (mState) {                                                       //Switch Svc state
                case ALWAYS_SVC_STATE_EVT_WIN_OPEN:                                     //Open?, or
                case ALWAYS_SVC_STATE_EVT_WIN_WARN:                                     //Warn?
                    intent = new Intent(this, LoginActivity.class);         //Login Activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EventWindowState", mState);
                    intent.putExtra("ExpireTime", strExpireTime);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_EXPIRE:
                    strNextEventTime = setNextEvtDtStr();
                    intent = new Intent(this, IdleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("ActTxt", "Event Missed.  Your next event is at " + strNextEventTime);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_RUNNING:
                    switch (mEventState) {
                        case ALWAYS_SVC_EVENT_NONE:
                        case ALWAYS_SVC_EVENT_LOGIN:
                        case ALWAYS_SVC_EVENT_LOGIN_FAIL:
                            intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("EventWindowState", mState);
                            intent.putExtra("ExpireTime", strExpireTime);
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            startActivity(intent);
                            break;
                        case ALWAYS_SVC_EVENT_STARTED:
                            StartEvent();
                            break;
                        case ALWAYS_SVC_EVENT_ABORT:
                            break;
                        case ALWAYS_SVC_EVENT_COMPLETE:
                            strNextEventTime = setNextEvtDtStr();
                            intent = new Intent(this, IdleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("ActTxt", "Thank you for participating.  Your next event is at " + strNextEventTime);
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            startActivity(intent);

                            mState = ALWAYS_SVC_STATE_POLL;

                            break;
                        case ALWAYS_SVC_EVENT_LOGOUT:
                            break;
                        case ALWAYS_SVC_EVENT_SAVED:
                            break;
                        case ALWAYS_SVC_EVENT_UPLOAD_STARTED:
                            break;
                        case ALWAYS_SVC_EVENT_UPLOAD_ABORT:
                            break;
                        case ALWAYS_SVC_EVENT_UPLOAD_COMPLETE:
                            break;
                        default:
                            break;
                    }

                    break;
                case ALWAYS_SVC_STATE_POLL:
                default:
                    strNextEventTime = getNextEvtDtStr();
                    intent = new Intent(this, IdleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("ActTxt", "Your next event is at " + strNextEventTime);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    break;
            }
        }
        return getAlwaysServiceState();
    }


    //return ProtRevIdx
    //get the current protocol rev index
    public int setNextProtRevIdx() {
        if (APP_DEMO_MODE) {
            miCurProtRevIdx = 0;
        } else {
            miCurProtRevIdx = 0;            //todo get protocol revision (should only ever be one anyway)
        }
        return miCurProtRevIdx;
    }

    //return ProtRevEvtIdx
    //get the current event index, or the next one if no active window
    public int setNextProtRevEvtIdx() {

        if (APP_DEMO_MODE) {
            miCurProtRevEvtIdx = 0;
        } else {
            DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
            try {
                dba.open();                                                                 //open db
            } catch (NullPointerException e) {
                Log.e("AlwaysService:setNextProtRevEvtIdx:NPEx", e.toString());
                //todo handle
            }
            String strQry = "SELECT * FROM tProtRevEvent";
            strQry = strQry + " WHERE ProtRevId = " + mlCurProtRevId;
            strQry = strQry + " ORDER BY EvtTimeOpen";                                  //select events //todo include Freq/Duration in sort

            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate cursor
                //todo use times and state to determine next event
                miCurProtRevEvtIdx = 0;     //todo current or next event
            }
            crs.close();
            dba.close();
        }
        return miCurProtRevEvtIdx;
    }

    //return ProtRevEvtActIdx
    //get the current event activity index, or the next one if no active window
    public int setNextProtRevEvtActIdx() {

        for (int j = 0; j < mArrProtRevEvtAct.size(); j++) {                            //iterate ProtRevEvtActivities
            if (mArrProtRevEvtAct.get(j).getProtocolRevEventId() == mlCurProtRevEvtId) {    //current Rev Event Id?

                miCurActIdx = j;             //set current activity index
            }
            if (miCurActIdx == j) {
                break;
            }
        }
        return miCurActIdx;
    }

    public String setNextEvtDtStr() {

        LocalDateTime locDt = LocalDateTime.now();
//        LocalDateTime locDtFlr =  locDt.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime locDtCeiling =  locDt.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (APP_DEMO_MODE) {
            mDtNextEvtStart = locDtCeiling.plusMinutes(APP_DEMO_MODE_MIN_OPEN);
            mDtNextEvtWarn = locDtCeiling.plusMinutes(APP_DEMO_MODE_MIN_WARN);
            mDtNextEvtExpire = locDtCeiling.plusMinutes(APP_DEMO_MODE_MIN_EXPIRE);
        } else {
            DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
            try {
                dba.open();                                                                 //open db
            } catch (NullPointerException e) {
                Log.e("AlwaysService:getNextEvtDtStr:NPEx", e.toString());
                //todo handle
            }

            mDtNextEvtStart = locDtCeiling.plusMinutes(APP_DEMO_MODE_MIN_OPEN);        //todo get actual next
            mDtNextEvtWarn = locDtCeiling.plusMinutes(APP_DEMO_MODE_MIN_WARN);
            mDtNextEvtExpire = locDtCeiling.plusMinutes(APP_DEMO_MODE_MIN_EXPIRE);

            dba.close();
        }

//        return mDtNextEvtStart.toString();
        return mDtNextEvtStart.format(fmtDt);
    }

    public String getNextEvtDtStr() {

        if (mDtNextEvtStart == null) {
            return setNextEvtDtStr();
        }

        DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return mDtNextEvtStart.format(fmtDt);
    }

    public String getEvtWarnDtStr() {

        LocalDateTime locDt = LocalDateTime.now();
        DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (mDtNextEvtWarn == null) {
            mDtNextEvtWarn = LocalDateTime.now();       //todo best way to handle null?
        }
        return mDtNextEvtWarn.format(fmtDt);
    }

    public String getEvtExpireDtStr() {

        LocalDateTime locDt = LocalDateTime.now();
        DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (mDtNextEvtExpire == null) {
            mDtNextEvtExpire = LocalDateTime.now();       //todo best way to handle null?
        }
        return mDtNextEvtExpire.format(fmtDt);
    }

    public int getNextEvtWinState() {

        if (mState == ALWAYS_SVC_STATE_ADMIN) {     //Admin state?
            return mState;                              //stay there
        }

        if (mState == ALWAYS_SVC_STATE_EVT_WIN_RUNNING) {   //Event Running state?
            return mState;                                      //stay there
        }

        if (mDtNextEvtStart == null) {              //next not populated?
            setNextEvtDtStr();                          //populate next
        }

        LocalDateTime dtNow = LocalDateTime.now();  //get date/time

        /////// If we get here, we know we are NOT Admin State, and NOT Already Running an Event ///////

        if (mDtNextEvtStart.isBefore(dtNow)) {      //We are after event Window Open
            if (mDtNextEvtWarn.isBefore(dtNow)) {       //We are after Window Open, after Warn
                if (mDtNextEvtExpire.isBefore(dtNow)) {       //We are after Warn and after Expire
                    if (mState != ALWAYS_SVC_STATE_EVT_WIN_EXPIRE) {    //Not EXPIRE?
                        mState = ALWAYS_SVC_STATE_EVT_WIN_EXPIRE;           //set Expired
                    }
                } else {                                    //We are after Warn but before Expire
                    if (mState != ALWAYS_SVC_STATE_EVT_WIN_WARN) {  //Not WARN?
                        mState = ALWAYS_SVC_STATE_EVT_WIN_WARN;         //set Warn
                    }
                }
            } else {                                    //Otherwise, We are after Window Open, but before Warn
                if (mState != ALWAYS_SVC_STATE_EVT_WIN_OPEN) {  //Not WIN OPEN?
                    mState = ALWAYS_SVC_STATE_EVT_WIN_OPEN;         //set Window Open
                }
            }
        } else {                                    //We are before event Window Open
            mState = ALWAYS_SVC_STATE_POLL;             //Keep Polling
        }
        return mState;
    }

    /** Get current Always Service State */
    public int getAlwaysServiceState() {
        Log.d(TAG, "Timer loop: Service State: EventState: " + (counter++) + ": " + mState + ": " + mEventState);         //Log State
//        Log.d(TAG, "Time running, timer loop: " + (counter++));     //todo turn off in release?

        getNextEvtWinState();       //check for state update
        return mState;
    }

    private int StartProtocol() {

        mlCurProtRevId = mArrProtRev.get(setNextProtRevIdx()).getProtocolRevId();                                   //get Current Protocol Rev Id
        mlCurProtRevEvtId = mArrProtRevEvt.get(setNextProtRevEvtIdx()).getProtocolRevEventId();                     //get Current Protocol Rev Event Id
        mlCurProtRevEvtActId = mArrProtRevEvtAct.get(setNextProtRevEvtActIdx()).getProtocolRevEventActivityId();    //get Current Protocol Rev Event Activity Id

        GotoEvtAct(miCurActIdx);

        return miCurActIdx;
    }


    private boolean GetProtocolFromDb() {

        ProtocolRevision newProtRev = new ProtocolRevision();             //current Protocol Rev
        ProtocolRevEvent newProtRevEvt = new ProtocolRevEvent();          //Current Protocol Rev Event
        EventActivity newProtRevEvtAct = new EventActivity();             //Current Protocol Rev Event Activity
        ActivityResponse newProtRevEvtActRsp = new ActivityResponse();    //Current Protocol Rev Event Activity Response

        long lPrevRev = 0;
        long lPrevEvt = 0;
        long lPrevAct = 0;
        long lPrevRsp = 0;

        long lCurRev = 0;
        long lCurEvt = 0;
        long lCurAct = 0;
        long lCurRsp = 0;
        long lCurRspTypeId;

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            dba.open();                                                                 //open db
        } catch (NullPointerException e) {
            Log.e("AlwaysService:GetProtocolFromDB:NPEx", e.toString());
            //todo handle
        }
                                                                                    //select view
        String strQry = "SELECT * FROM vProtRevEvtActivities WHERE ActId is not NULL and ActTypeId IS NOT NULL ORDER BY ProtRevId, ProtRevEvtId, ActSeq, ProtRevEvtActId, ActRspSeq";

        Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view

        while (crs.moveToNext()) {                                                  //Iterate Event Activities and Responses
            {
                lCurRev = crs.getLong(crs.getColumnIndex("ProtRevId"));     //get Protocol Rev
                if (lCurRev != lPrevRev) {                                              //changed?
                    if (lPrevRev != 0) {                                                    //Not first?
                        mArrProtRev.add(newProtRev);                                              //Add to the array list
                    }
                    newProtRev = new ProtocolRevision();
                    newProtRev.setProtocolRevId(lCurRev);                                     //set current Protocol Rev
                                                                                            //set current protocol Rev values
                    newProtRev.setProtocolName(crs.getString(crs.getColumnIndex("ProtName")));          //todo add to vProtRevEvtActivities
                    newProtRev.setProtocolRevName(crs.getString(crs.getColumnIndex("ProtRevName")));    //todo add to vProtRevEvtActivities
                    newProtRev.setProtocolRevDt(crs.getString(crs.getColumnIndex("ProtRevDt")));        //todo add to vProtRevEvtActivities
                    newProtRev.setProtocolRevEventCnt(crs.getLong(crs.getColumnIndex("EvtCnt")));

                    lPrevRev = lCurRev;                                                     //reset previous
                }

                lCurEvt = crs.getLong(crs.getColumnIndex("ProtRevEvtId"));  //get Protocol Rev Event
                if (lCurEvt != lPrevEvt) {                                              //changed?
                    if (lPrevEvt != 0) {                                                    //Not first?
                        mArrProtRevEvt.add(newProtRevEvt);                                        //Add to the array list

                    }
                    newProtRevEvt = new ProtocolRevEvent();
                    newProtRevEvt.setProtocolRevEventId(lCurEvt);                             //set current Event
                                                                                            //set current Event Values
                    newProtRevEvt.setProtocolRevId(lCurRev);
                    newProtRevEvt.setProtocolRevEventName(crs.getString(crs.getColumnIndex("ProtRevEvtName")));
                    newProtRevEvt.setFrequencyCode(crs.getString(crs.getColumnIndex("EvtFreq")));
                    newProtRevEvt.setEventDayStart(crs.getLong(crs.getColumnIndex("EvtStart")));
//                    newProtRevEvt.sete(crs.getLong(crs.getColumnIndex("EvtDaysDuration")));
                    newProtRevEvt.setEventTimeOpen(crs.getString(crs.getColumnIndex("EvtTimeOpen")));
                    newProtRevEvt.setEventTimeWarn(crs.getString(crs.getColumnIndex("EvtTimeWarn")));
                    newProtRevEvt.setEventTimeClose(crs.getString(crs.getColumnIndex("EvtTimeClose")));
 //                   newProtRevEvt.setEventActivityCnt(crs.getLong(crs.getColumnIndex("eventActivityCnt")));

                    lPrevEvt = lCurEvt;
                }

                lCurAct = crs.getLong(crs.getColumnIndex("ProtRevEvtActId"));   //get Protocol Rev Event Activity
                if (lCurAct != lPrevAct) {                                                  //changed?
                    if (lPrevAct != 0) {                                                        //Not first?
                        mArrProtRevEvtAct.add(newProtRevEvtAct);                                      //Add to the array list

                        //While we iterate, Objects are added to arrays when a new one is found
                        //Now we need to add the last of each object, if it exists
                        if (lCurRsp > 0) {
                            mArrProtRevEvtActRsp.add(newProtRevEvtActRsp);
                        }
                    }
                    newProtRevEvtAct = new EventActivity();
                    newProtRevEvtAct.setProtocolRevEventActivityId(lCurAct);                      //set current
                    lCurRsp = 0;                                                                //Init response cnt
                    lPrevRsp = 0;                                                               //Init response previous
                                                                                                //set current Activity Values
                    newProtRevEvtAct.setProtocolRevEventId(lCurEvt);
                    newProtRevEvtAct.setActivitySeq(crs.getLong(crs.getColumnIndex("ActSeq")));
                    newProtRevEvtAct.setActivityId(crs.getLong(crs.getColumnIndex("ActId")));
                    newProtRevEvtAct.setProtRevEvtApplyTo(crs.getString(crs.getColumnIndex("ApplyTo")));
                    newProtRevEvtAct.setMinRange(crs.getLong(crs.getColumnIndex("MinRange")));
                    newProtRevEvtAct.setMaxRange(crs.getLong(crs.getColumnIndex("MaxRange")));
                    newProtRevEvtAct.setActivityTypeId(crs.getLong(crs.getColumnIndex("ActTypeId")));
                    newProtRevEvtAct.setActivityTypeCode(crs.getString(crs.getColumnIndex("ActTypeCode")));
                    newProtRevEvtAct.setActivityText(crs.getString(crs.getColumnIndex("ActText")));
                    newProtRevEvtAct.setActivityPictureCode(crs.getString(crs.getColumnIndex("ActPictureCode")));
                    newProtRevEvtAct.setActivityResponseTypeId(crs.getLong(crs.getColumnIndex("RspTypeId")));
                    newProtRevEvtAct.setActivityResponseTypeCode(crs.getString(crs.getColumnIndex("ActResponseTypeCode")));
//                    newProtRevEvtAct.setActivityResponseCnt();
//                    newProtRevEvtAct.setImageFileId(crs.getLong(crs.getColumnIndex("ImageFileId")));
//                    newProtRevEvtAct.setImageFileName(crs.getString(crs.getColumnIndex("ImageFileName")));
//                    newProtRevEvtAct.setImageFilePath(crs.getString(crs.getColumnIndex("ImageFilePath")));
//                    newProtRevEvtAct.setActivityResponseCnt(crs.getLong(crs.getColumnIndex("ActRespCnt")));     //Use Field or Calculate???

                    lPrevAct = lCurAct;
                }

                //TODO: Future just get ActRspId from the View.  Currently it is not in there
                lCurRspTypeId = crs.getLong(crs.getColumnIndex("RspTypeId"));   //get response type
                if ((lCurRspTypeId > 1) && (lCurRspTypeId != 4)) {                          //Greater than 1 means there is a response, Not 4 means skip Sliders
//20200126
//                    lCurRsp = lCurRsp + 1;                                                      //increment Protocol Rev Event Activity Response
                    lCurRsp = crs.getLong(crs.getColumnIndex("ActRspId"));          //get ActResponseId                                                     //increment Protocol Rev Event Activity Response
                    if (lCurRsp != lPrevRsp) {                                                  //changed?
                        if (lPrevRsp != 0) {                                                        //Not first?
                            mArrProtRevEvtActRsp.add(newProtRevEvtActRsp);                                //Add to the array list
                        }

                        newProtRevEvtActRsp = new ActivityResponse();
                        newProtRevEvtActRsp.setProtocolRevEventActivityId(lCurAct);                      //set current Response
                        //set current Response Values
                        newProtRevEvtActRsp.setActRspId(lCurRsp);
                        newProtRevEvtActRsp.setActId(crs.getLong(crs.getColumnIndex("ActId")));
                        newProtRevEvtActRsp.setActRspSeq(crs.getLong(crs.getColumnIndex("ActRspSeq")));
                        newProtRevEvtActRsp.setActRspValue(crs.getLong(crs.getColumnIndex("ActRspValue")));
                        newProtRevEvtActRsp.setActRspText(crs.getString(crs.getColumnIndex("ActRspText")));

                        lPrevRsp = lCurRsp;
                    }
                }
            }
        }
        //While we iterate, Objects are added to arrays when a new one is found
        //Now we need to add the last of each object, if it exists
        if (lCurRev > 0) {
            mArrProtRev.add(newProtRev);
        }
        if (lCurEvt > 0) {
            mArrProtRevEvt.add(newProtRevEvt);
        }
        if (lCurAct > 0) {
            mArrProtRevEvtAct.add(newProtRevEvtAct);
        }
        if (lCurRsp > 0) {
            mArrProtRevEvtActRsp.add(newProtRevEvtActRsp);
        }

        crs.close();
        dba.close();

        return true;
    }

    /**
     * initialize directory structure
     */
    private boolean InitDirectoryTree() {


        String strRootDir = this.getDataDir().getPath();            //./...
        File fDir;
        File fFile;
        FileOutputStream fStream;
        String str;
        byte [] sBytes;

        fDir = new File(strRootDir, APP_DIR_PROTOCOL);

        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                //todo what if fail?
            }
        }
        fFile = new File(fDir, "Readme.dat"); //Getting a file within the dir.
        try {
            if (!fFile.exists()) {
                str = "The current Protocol is stored in this directory.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }
        } catch (IOException e) {
            Log.e("AlwaysService:InitDirectoryTree:Readme:Ex", e.toString());
            //todo handle
        }

//        fDir = new File(this.getExternalFilesDir(null), strDir);
        fDir = new File(strRootDir, APP_DIR_PROTOCOL_ARCHIVE);
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                Log.d("AlwaysService:InitDirectoryTree:APP_DIR_PROTOCOL_ARCHIVE", "failed to create directory");
                //todo handle
            }
        }
        fFile = new File(fDir, "Readme.dat"); //Getting a file within the dir.
        try {
            if (!fFile.exists()) {
                str = "Previous Protocols are stored in this directory.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }
        } catch (IOException e) {
            Log.e("AlwaysServiceInitDirectoryTree:Ex", e.toString());
            //todo handle
        }

//        fDir = new File(this.getExternalFilesDir(null), strDir);
        fDir = new File(strRootDir, APP_DIR_PARTICIPANTS);
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                Log.d("AlwaysService:InitDirectoryTree:APP_DIR_PARTICIPANTS", "failed to create directory");
                //todo handle
            }
        }
        fFile = new File(fDir, "Readme.dat"); //Getting a file within the dir.
        try {
            if (!fFile.exists()) {
                str = "Each Participant will have a directory here.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }
        } catch (IOException e) {
            Log.e("AlwaysService:InitDirectoryTree:Readme:IOEx", e.toString());
            //todo handle
        }

//        fDir = new File(this.getExternalFilesDir(null), strDir);
        fDir = new File(strRootDir, APP_DIR_DATA);
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                Log.d("AlwaysService:InitDirectoryTree:APP_DIR_DATA", "failed to create directory");
                //todo handle
            }
        }
        fFile = new File(fDir, "Readme.dat"); //Getting a file within the dir.
        try {
            if (!fFile.exists()) {
                str = "The current working Database is stored in this directory.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }
        } catch (IOException e) {
            Log.e("AlwaysService:InitDirectoryTree:Readme:IOEx", e.toString());
            //todo handle
        }

        fFile = new File(fDir, APP_DATA_DBNAME);    //Getting a file within the dir.
//        try {

        if (!fFile.exists()) {
            installDatabaseFromAssets();
        }

//        fDir = new File(this.getExternalFilesDir(null), strDir);
        fDir = new File(strRootDir, APP_DIR_DATA_ARCHIVE);
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                Log.d("AlwaysService:InitDirectoryTree:APP_DIR_DATA_ARCHIVE", "failed to create directory");
                //todo handle
            }
        }
        fFile = new File(fDir, "Readme.dat"); //Getting a file within the dir.
        try {
            if (!fFile.exists()) {
                str = "Database backups are stored in this directory.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }
        } catch (IOException e) {
            Log.e("AlwaysService:InitDirectoryTree:Readme:IOEx", e.toString());
            //todo handle
        }

//        fDir = new File(this.getExternalFilesDir(null), strDir);
        fDir = new File(strRootDir, APP_DIR_DATA_FRESH);
        if (!fDir.exists()) {
            if (!fDir.mkdirs()) {
                Log.d("AlwaysService:InitDirectoryTree:APP_DIR_DATA_FRESH", "failed to create directory");
                //todo handle
            }
        }
        fFile = new File(fDir, "Readme.dat"); //Getting a file within the dir.
        try {
            if (!fFile.exists()) {
                str = "A Fresh, empty Database is stored in this directory.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }
        } catch (IOException e) {
            Log.e("AlwaysService:InitDirectoryTree:Readme:IOEx", e.toString());
            //todo handle
        }

        return true;
    }

    //todo WHEN is installDatabase from Assets called?
    public boolean installDatabaseFromAssets() {

        String strRootDir = this.getDataDir().getPath();
        File fDir;
        File fFile;
        FileOutputStream foStream;

        Context context = getApplicationContext();

        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open(APP_ASSET_DBNAME);
            fDir = new File(strRootDir, APP_DIR_DATA);
            fFile = new File(fDir, APP_DATA_DBNAME); //Getting a file within the dir.
            foStream = new FileOutputStream(fFile);
            //OutputStream out = new FileOutputStream(path+"yourdata.extension");
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                foStream.write(buffer, 0, read);
                read = in.read(buffer);
            }
        } catch (Exception e) {
            //todo Warning:(872, 11) Some important exceptions might be ignored in a 'catch' block
            Log.e("AlwaysService:installDatabaseFromAssets:Ex", e.toString());
            //todo handle
        }

        return true;
    }

    //todo look at using StartStaeMachine
    public boolean StartStateMachine() {

        if (!bStateMachineRunning) {                    //Not running yet?
            bStateMachineRunning = true;                    //start running
//            if (APP_DEMO_MODE) {                            //if demo mode
//                mState = ALWAYS_SVC_STATE_EVT_WIN_OPEN;         //go straight to open event //todo not needed
//            } else {                                         //otherwise
                mState = ALWAYS_SVC_STATE_POLL;                 //start polling
//            }
//            mEventState = ALWAYS_SVC_EVENT_NONE;            //No event in progress
            setServiceEventState(ALWAYS_SVC_EVENT_NONE);    //
        }
        return true;
    }

    public int StartEvent() {

        return StartProtocol();
    }

    public String getPatNumber() {
        return mstrPatNumber;
    }

    /* Get next Activity Index in the Event */
    public int setNextActivityIdx() {
        miCurActIdx++;                                                                              //increment activity index
        while (mArrProtRevEvtAct.get(miCurActIdx).getProtocolRevEventId() != mlCurProtRevEvtId) {   //different event?
            miCurActIdx++;                                                                              //increment again
            if (miCurActIdx == mArrProtRevEvtAct.size()) {              //passed end of array?
 //               miCurActIdx = 0;    //todo best way to finish event?
//                mEventState = ALWAYS_SVC_EVENT_COMPLETE;
                setServiceEventState(ALWAYS_SVC_EVENT_COMPLETE);            //set event complete
                AlwaysServiceStateMachine();                                //exercise state machine
                break;                                                                                      //bail
            }
        }
        return miCurActIdx;             //return index
    }

    /* Get current Activity Index in the Event */
    public int getCurActivityIdx() {
        return miCurActIdx;             //return index
    }

    public int getActivityTypeFromIdx(int j) {

        if (mArrProtRevEvtAct.size() <= j) {
            return 0;
        } else {
            return mArrProtRevEvtAct.get(j).getActivityTypeId().intValue();
        }
    }
    public String getActivityTextFromIdx(int j) {

        if (mArrProtRevEvtAct.size() <= j) {
            return "";
        } else {
            return mArrProtRevEvtAct.get(j).getActivityText();
        }
    }

    public String getActivityPictureCodeIdx(int j) {

        if (mArrProtRevEvtAct.size() <= j) {
            return "";
        } else {
            return mArrProtRevEvtAct.get(j).getActivityPictureCode();
        }
    }

    public EventActivity getActivityFromIdx(int j) {

        if (mArrProtRevEvtAct.size() <= j) {
            return null;
        } else {
            return mArrProtRevEvtAct.get(j);
        }
    }

    public void GotoEvtAct(int iCurActIdx) {
        Intent intent;
        int iRspTypeId;
        String strApplyTo;
        boolean bBoth = false;

        if (iCurActIdx >= mArrProtRevEvtAct.size()) {   //activity index too high?
            setServiceEventState(ALWAYS_SVC_EVENT_COMPLETE);            //set event complete
            AlwaysServiceStateMachine();                                //exercise state machine
            return;
        }

        ActivityResponse actRsp;
        ArrayList<ActivityResponse> arrActRsp = new ArrayList<>();   //Event Activity Responses
        String strRsps = "";

        strApplyTo = mArrProtRevEvtAct.get(iCurActIdx).getProtRevEvtApplyTo();
        if (strApplyTo.equals("B")) {
            bBoth = true;
        }

        for (int i = 0; i < mArrProtRevEvtActRsp.size(); i++) {

            //todo why this warning???: Warning:(982, 56) Number objects are compared using '==', not 'equals()'
            if (mArrProtRevEvtActRsp.get(i).getActId().longValue() == mArrProtRevEvtAct.get(iCurActIdx).getActivityId()) {
                actRsp = new ActivityResponse();
                actRsp.setActId(mArrProtRevEvtActRsp.get(i).getActId());
                actRsp.setActRspId(mArrProtRevEvtActRsp.get(i).getActRspId());
                actRsp.setActRspSeq(mArrProtRevEvtActRsp.get(i).getActRspSeq());
                actRsp.setActRspText(mArrProtRevEvtActRsp.get(i).getActRspText());
                actRsp.setActRspValue(mArrProtRevEvtActRsp.get(i).getActRspValue());
                arrActRsp.add(actRsp);
            }
        }
        if (arrActRsp.size() > 0) {
            Gson gS = new Gson();
            strRsps = gS.toJson(arrActRsp); // Converts the object to a JSON String
        }

        switch (mArrProtRevEvtAct.get(iCurActIdx).getActivityTypeId().intValue()) {                     //switch Activity Type
            case ACTIVITY_TYPE_INSTRUCTION:                                                             //Instruction
                Log.d("AlwaysService", "GotoEvtAct: ACTIVITY_TYPE_INSTRUCTION");

                intent = new Intent(this, InstructionActivity.class);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("ActIdx", miCurActIdx);
                intent.putExtra("ActTxt", mArrProtRevEvtAct.get(iCurActIdx).getActivityText());
                startActivity(intent);
                break;

            case ACTIVITY_TYPE_QUESTION:                                                                //Question
                Log.d("AlwaysService", "GotoEvtAct: ACTIVITY_TYPE_QUESTION");

                iRspTypeId = mArrProtRevEvtAct.get(iCurActIdx).getActivityResponseTypeId().intValue();      //Get Response Type
                switch (iRspTypeId) {                                                                       //Switch Response Type
                    case RESPONSE_TYPE_RDB:                                                                 //Radio Button
                    case RESPONSE_TYPE_LST:                                                                 //List
                        if (bBoth) {                                                                            //ApplyTo Both?
                            intent = new Intent(this, DoubleRadioBtnsActivity.class);               //use double
                        } else {                                                                                //Otherwise
                            intent = new Intent(this, SingleRadioBtnsActivity.class);               //use single
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        intent.putExtra("ActIdx", miCurActIdx);
                        intent.putExtra("ActTxt", mArrProtRevEvtAct.get(iCurActIdx).getActivityText());
                        intent.putExtra("ActRsp", strRsps);
                        startActivity(intent);
                        break;
                        //TODO test List types, until then rdb only
//                    case RESPONSE_TYPE_LST:                                                                 //List
//                        if (bBoth) {                                                                            //ApplyTo Both?
//                            intent = new Intent(this, DoubleListActivity.class);                    //use double
//                        } else {                                                                                //Otherwise
//                            intent = new Intent(this, SingleListActivity.class);                    //use single
//                        }
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("ActId", miCurActIdx);
//                        intent.putExtra("ActTxt", mArrProtRevEvtAct.get(iCurActIdx).getActivityText());
//                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                        startActivity(intent);
//                        break;
                    case RESPONSE_TYPE_SLD:
                        if (bBoth) {                                                                            //ApplyTo Both?
                            intent = new Intent(this, DoubleSeekBarActivity.class);                 //use double
                        } else {                                                                                //Otherwise
                            intent = new Intent(this, SingleSeekBarActivity.class);                 //use single
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        intent.putExtra("ActIdx", miCurActIdx);
                        intent.putExtra("ActTxt", mArrProtRevEvtAct.get(iCurActIdx).getActivityText());
                        intent.putExtra("RspMin", mArrProtRevEvtAct.get(iCurActIdx).getMinRange());
                        intent.putExtra("RspMax", mArrProtRevEvtAct.get(iCurActIdx).getMaxRange());
                        startActivity(intent);
                        break;
                    case RESPONSE_TYPE_CHK:
                        break;
                    case RESPONSE_TYPE_NONE:
                        break;
                    default:
                        break;
                }
                break;

            case ACTIVITY_TYPE_PICTURE:                                                                 //Picture
                Log.d("AlwaysService", "GotoEvtAct: ACTIVITY_TYPE_PICTURE");
                intent = new Intent(this, CameraActivity.class);   //todo picture class
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("ActIdx", miCurActIdx);
                intent.putExtra("ActTxt", mArrProtRevEvtAct.get(iCurActIdx).getActivityText());
                intent.putExtra("ActPicCode", mArrProtRevEvtAct.get(iCurActIdx).getActivityPictureCode());
                intent.putExtra("PatNum", getPatNumber());
                startActivity(intent);

                break;
            default:
                break;
        }
    }

    /** set service state */
    public int setServiceEventState(int iNewState) {

        if (iNewState == LOGIN_PARTICPANT_ERR_NO_ERR) {
            mEventState = ALWAYS_SVC_EVENT_STARTED;
            setServiceState(ALWAYS_SVC_STATE_EVT_WIN_RUNNING);
        } else {
            mEventState = iNewState;
        }
        AlwaysServiceStateMachine();

        return iNewState;
    }

    /** set service state */
    public int setServiceState(int iNewState) {

        mState = iNewState;
        AlwaysServiceStateMachine();

        return iNewState;
    }

    public int TryLogin(String username, String password) {

        if (username.equals(LOGIN_ADMIN_NAME)) {        //admin user name?
            if (password.equals(LOGIN_ADMIN_PW)) {          //password OK?
                return LOGIN_ADMIN_ERR_NO_ERR;                  //return admin login
            } else {                                        //otherwise
                return LOGIN_ADMIN_ERR_PW;                      //return admin password error
            }
        }

        /////////////// NOT Admin ID - Continue ////////////////////

            //        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            dba.open();                                                                 //open database
        } catch (NullPointerException e) {
            Log.e("AlwaysService:TryLogin:NPEx", e.toString());
            //todo handle
        }
//        Object[][] patInfo = dba.GetParticipantInfo();

        String strQry = "SELECT * FROM tParticipant ORDER BY PatNumber";
        Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view

        int iPatID = -1;
        int iPatNum;
        String strPatNum;
        String strPatStudyNum = "";
        String strPatPW = "";

        while (crs.moveToNext()) {                                                  //Iterate Participants
            iPatNum = crs.getInt(crs.getColumnIndex("PatNumber"));             //Get Participant Number
            strPatNum = String.valueOf(iPatNum);                                        //convert string
            if (strPatNum.equals(username)) {                                           //Same as looking for?
                iPatID = crs.getInt(crs.getColumnIndex("PatId"));                   //set other fields
                strPatStudyNum = crs.getString(crs.getColumnIndex("StudyPatNumber"));
                strPatPW = crs.getString(crs.getColumnIndex("Password"));

                break;
            }
        }
        crs.close();
        dba.close();

        if (iPatID == -1) {                     //user not found
            return LOGIN_PARTICPANT_ERR_ID;         //return Participant ID error
        }

        if (!strPatPW.equals(password)) {       //wrong password
            return LOGIN_PARTICPANT_ERR_PW;         //return Participant password error
        } else {                                //Otherwise
            return LOGIN_PARTICPANT_ERR_NO_ERR;     //return Participant Login OK
        }
//        return LOGIN_ERR_UNK;                   //we should never get this far

//
//
//        try {
//            Integer patNum = null;
//            String dbPass = null;
//            for (int i = 0; i < patInfo[0].length; i++) {
//                if (((String)patInfo[0][i]).equals("PatNumber"))
//                    patNum = (int)patInfo[1][i];
//                else if (((String)patInfo[0][i]).equals("Password"))
//                    password = patInfo[1][i].toString();
//            }
//
//            LoggedInUser patient = new LoggedInUser(patNum.toString(), "Patient " + patNum.toString());
//            LoggedInUser administrator = new LoggedInUser("Admin", "Administrator");

//            if (username.equals(patNum.toString()) && password.equals(dbPass)) {
//                AlwaysService.getmCurrentService().mState = Globals.SVC_EVT_STATE_RUN;        //Participant logged in
//                return new Result.Success<>(patient);                                           //edit: start the event
//            }

//            if (username.equals("Admin") && password.equals("123456")) { //edit: set admin username and password here
//                AlwaysService.getmCurrentService().mState = Globals.SVC_EVT_STATE_ADMIN;      //User Logged in
//                return new Result.Success<>(administrator);                                     //edit: go to admin screen
//            }

//            exception = new Exception("Incorrect username or password.");
//        } catch (Exception e) { exception = e; }

//        return new Result.Error(new IOException("Error logging in", exception));

    }

    private ParticipantEvent GetParticipantInfoEntity(DatabaseAccess dba) {
        if (dba == null)
            return null; //default for method misuse
        ParticipantEvent event = new ParticipantEvent();
        dba.open();
        Object[][] patInfo = dba.GetParticipantInfo();
        List<Object[]> deviceInfo = dba.GetTableData("tDevice");
        dba.close();
        for (int i = 0; i < patInfo.length; i++)
        {
            if (patInfo[i][0].equals("PatId")) {
                event.setParticipantId(patInfo[i][1].toString());
                miPatId = (int)patInfo[i][0];
            }
            else if (patInfo[i][0].equals("StudyPatNumber"))
            {
                mstrPatNumber = (String)patInfo[i][0];
                Integer[] components = new Integer[5];
                try {
                    String[] spnComponents = patInfo[i][1].toString().split("-");
                    for (int j = 0; j < spnComponents.length; j++)
                        components[j] = Integer.parseInt(spnComponents[j]);
                }
                catch (Exception e) {
                    Log.e("AlwaysService:getParticipantInfoEntity:Ex", e.toString());
                    //todo handle
                    components = null;
                }
                if (components != null) {
                    event.setYearId(components[0].toString());
                    event.setDepartmentId(components[1].toString());
                    event.setStudyId(components[2].toString());
                    event.setLocationId(components[3].toString());
                    event.setSubjectId(components[4].toString());
                }
            }
        }
        for (int i = 0; i < deviceInfo.get(0).length; i++)
            if (deviceInfo.get(0)[i].equals("DeviceId"))
                event.setDeviceId(deviceInfo.get(1)[i].toString());
            else if (deviceInfo.get(0)[i].equals("DeviceAppId"))
                event.setDeviceAppId(deviceInfo.get(1)[i].toString());
        return event;
    }
    public long StartParticipantEvent(DatabaseAccess dba, long protocolRevId, long protocolRevEventId, long eventId, DateFormat dFormat) { //edit: call each time a participant event begins; returns its corresponding "PatEvtId"; have a universal "DateFormat" for formatting SQLite db dates, to set here and everywhere else applicable
        if (dba == null || dFormat == null)
            return -1; //default for method misuse
        ParticipantEvent participantEvent = GetParticipantInfoEntity(dba);
        List<Object[]> existingEvents = dba.GetTableData("tParticipantEvent");
        long maxId = 0;
        for (int i = 1; i < existingEvents.size(); i++) {
            for (int j = 0; j < existingEvents.get(i).length; j++) {
                if (existingEvents.get(0)[i].equals("PatEventId") && (long)existingEvents.get(i)[i] > maxId)
                    maxId = (long)existingEvents.get(i)[i];
            }
        }
        participantEvent.setPatEventId(maxId + 1);
        participantEvent.setProtocolRevId(protocolRevId);
        participantEvent.setProtocolRevEventId(protocolRevEventId);
        participantEvent.setEventId(eventId);
        participantEvent.setPatEventDtStart(dFormat.format(Calendar.getInstance().getTime()));
        participantEvent.setPatEventResponseCnt(0L);
        participantEvent.setPatEventPictureCnt(0L);
        dba.open();
        dba.InsertParticipantEvent(participantEvent);
        dba.close();
        return participantEvent.getPatEventId();
    }
    public void CommitActivityInfo(long patEvtId, Object entity) { //edit: call each time a participant event activity is committed; set "entity" to a PatEventResponse or a PatEventPicture
        if (entity == null)
            return; //returned for method misuse
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());
        try {
            dba.open();
        } catch (NullPointerException e ) {
            Log.e("AlwaysService:CommitActivityInfo:NPEx", e.toString());
            //todo handle
        }
        if (entity.getClass().equals(PatEventResponse.class)) {
            dba.InsertParticipantResponse(patEvtId, (PatEventResponse)entity);
            dba.UpdateParticipantEventChildCnt(patEvtId, true, GetPatActivityRespOrPicCnt(patEvtId, false, dba));
        }
        else {
            dba.InsertParticipantPicture(patEvtId, (PatEventPicture) entity);
            dba.UpdateParticipantEventChildCnt(patEvtId, false, GetPatActivityRespOrPicCnt(patEvtId, true, dba));
        }
        dba.close();
    }
    private int GetPatActivityRespOrPicCnt(long patEvtId, boolean isPicture, DatabaseAccess dba) { //dba must be open during this for it to work properly
        if (dba == null)
            return -1; //default for method misuse
        List<Object[]> data = dba.GetTableData("tParticipantEvent");
        int oldEvtCnt = 0;
        for (int i = 0; i < data.get(0).length; i++) {
            if (data.get(0)[i].equals("PatEvtId")) {
                for (int j = 1; j < data.size(); j++) {
                    if ((long)data.get(j)[i] == patEvtId) {

                        for (int k = 1; k < data.size(); k++) {
                            if (!isPicture)
                                if (data.get(0)[k].equals("PatEvtResponseCnt")) {
                                    oldEvtCnt = (int)data.get(j)[i];
                                    break;
                                }
                                else {
                                    if (data.get(0)[k].equals("PatEvtPictureCnt"))
                                        oldEvtCnt = (int)data.get(j)[i];
                                    break;
                                }
                        }
                        break;
                    }
                }
                break;
            }
        }
        return oldEvtCnt;
    }
    public String EndParticipantEvent(long patEvtId, AppCompatActivity caller, DatabaseAccess dba, DateFormat dFormat) { //edit: call each time a participant event ends; returns the file path of the output json; figure out how to get the caller connected to this service
        if (caller == null || dba == null || dFormat == null)
            return null; //default for method misuse
        dba.open();
        dba.MarkParticipantEventEnded(patEvtId, dFormat.format(Calendar.getInstance().getTime()));
        String path = dba.CreateJSON("vOuterPatEvt", "PatEventResponses",
                "vInnerPatEvt","PatEventImages", "vInnerPatEvtPictures", caller);
        dba.close();
        return path; //edit: also, before returning, start process to try uploading periodically, then call method in dba to mark it uploaded when done
    }
}

