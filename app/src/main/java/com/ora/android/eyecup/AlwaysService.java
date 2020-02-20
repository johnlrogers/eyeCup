package com.ora.android.eyecup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.ora.android.eyecup.oradb.TDevice;
import com.ora.android.eyecup.oradb.TParticipant;
import com.ora.android.eyecup.oradb.TParticipantEvent;
import com.ora.android.eyecup.oradb.TParticipantEventActivity;
import com.ora.android.eyecup.ui.login.LoginActivity;
import com.ora.android.eyecup.utilities.Notification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static com.ora.android.eyecup.Globals.ACTIVITY_TYPE_INSTRUCTION;
import static com.ora.android.eyecup.Globals.ACTIVITY_TYPE_PICTURE;
import static com.ora.android.eyecup.Globals.ACTIVITY_TYPE_QUESTION;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_CATCHUP_DLY_CNT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_ABORT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_ADMIN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_COMPLETE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_LOGIN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_LOGIN_FAIL;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_LOGOUT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_NONE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_SAVED;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_START;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_UPLOAD_ABORT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_UPLOAD_COMPLETE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_EVENT_UPLOAD_STARTED;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_ADMIN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_CATCHUP_UPLOAD;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_COMPLETE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_EXPIRE;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_OPEN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_RUNNING;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_THANKYOU;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_UPLOAD;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_EVT_WIN_WARN;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_STATE_POLL;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_THANKYOU_DLY_CNT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_TIMER_DELAY;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_TIMER_PERIOD;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_UPLOAD_DLY_CNT;
import static com.ora.android.eyecup.Globals.ALWAYS_SVC_UPLOAD_PIC_DLY_CNT;
import static com.ora.android.eyecup.Globals.APP_ASSET_DBNAME;
import static com.ora.android.eyecup.Globals.APP_DATA_DBNAME;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE_MIN_EXPIRE;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE_MIN_OPEN;
import static com.ora.android.eyecup.Globals.APP_DEMO_MODE_MIN_WARN;
import static com.ora.android.eyecup.Globals.APP_DFLT_DEVICE_APPID;
import static com.ora.android.eyecup.Globals.APP_DFLT_DEVICE_ID;
import static com.ora.android.eyecup.Globals.APP_DFLT_PAT_DEPTID;
import static com.ora.android.eyecup.Globals.APP_DFLT_PAT_ID;
import static com.ora.android.eyecup.Globals.APP_DFLT_PAT_LOCID;
import static com.ora.android.eyecup.Globals.APP_DFLT_PAT_NUM;
import static com.ora.android.eyecup.Globals.APP_DFLT_PAT_STUDYID;
import static com.ora.android.eyecup.Globals.APP_DFLT_PAT_STUDYPATNUM;
import static com.ora.android.eyecup.Globals.APP_DIR_DATA;
import static com.ora.android.eyecup.Globals.APP_DIR_DATA_ARCHIVE;
import static com.ora.android.eyecup.Globals.APP_DIR_DATA_FRESH;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANTS;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANT_EVENTS;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANT_LOG;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANT_PICS;
import static com.ora.android.eyecup.Globals.APP_DIR_PROTOCOL;
import static com.ora.android.eyecup.Globals.APP_DIR_PROTOCOL_ARCHIVE;
import static com.ora.android.eyecup.Globals.APP_LOG_FILENAME;
import static com.ora.android.eyecup.Globals.APP_PAT_NUM_LEN;
import static com.ora.android.eyecup.Globals.DT_FMT_FULL_ACTIVITY;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_ERR_NO_ERR;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_ERR_PW;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_NAME;
import static com.ora.android.eyecup.Globals.LOGIN_ADMIN_PW;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_ID;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_NO_ERR;
import static com.ora.android.eyecup.Globals.LOGIN_PARTICPANT_ERR_PW;
import static com.ora.android.eyecup.Globals.MSG_IDLE;
import static com.ora.android.eyecup.Globals.MSG_THANK_YOU;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_CHK;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_LST;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_NONE;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_RDB;
import static com.ora.android.eyecup.Globals.RESPONSE_TYPE_SLD;
import static com.ora.android.eyecup.Globals.URL_EVENT_UPLOAD;
import static com.ora.android.eyecup.Globals.URL_PICTURE_UPLOAD;

//public class AlwaysService extends Service {
public class AlwaysService extends Service implements AsyncResponse {

    private static String TAG = "AlwaysService";                            //Service Tag
    private static AlwaysService mCurrentService;                           //Service object
    private long mlCount = 0;                                               //Cycle counter (++ every ALWAYS_SVC_TIMER_PERIOD ms)
    private long mlShowIdleCnt = 0;                                         //Ctr when to stop "thank you" after complete event
    private long mlBeginEvtUploadCnt = 0;                                   //Ctr when to start upload after complete event
    private long mlChkCatchupUploadCnt = 0;                                 //Ctr when to start upload after complete event

    MediaPlayer mPlayer;                                                    //media player object

    private static Random random = new Random();
    private static int iSeed = random.nextInt(9999 - 1001 ) + 1001;  //random 1001 to 9998
    protected static final int NOTIFICATION_ID = iSeed;                     //App Svc Notifier ID

    private Globals glob;                                                   //global utilities

    private boolean bStateMachineRunning = false;                           //is state machine running?
    private int mState = ALWAYS_SVC_STATE_POLL;                             //current service state
    private int mPrevState = -1;                                            //previous service state
    private int mEventState = ALWAYS_SVC_EVENT_NONE;                        //current event state
    private int mPrevEventState = -1;                                       //previous event state

    private LocalDateTime mDtNextEvtStart = null;                           //init next event datetime
    private LocalDateTime mDtNextEvtWarn = null;                            //init next event datetime
    private LocalDateTime mDtNextEvtExpire = null;                          //init next event datetime
    ArrayList<ProtocolRevision> mArrProtRev = new ArrayList<>();            //All Protocol Revs (should only be one, add for consistency)
    ArrayList<ProtocolRevEvent> mArrProtRevEvt = new ArrayList<>();         //All Protocol Rev Events
    ArrayList<EventActivity> mArrProtRevEvtAct = new ArrayList<>();         //All Protocol Rev Event Activities
    ArrayList<ActivityResponse> mArrProtRevEvtActRsp = new ArrayList<>();   //All Protocol Rev Event Activity Responses

    private int miCurProtRevIdx = 0;                                        //Current mArrProtRev Index
    private int miCurProtRevEvtIdx = -1;                                    //Current mArrProtRevEvt Index
    private int miCurActIdx = 0;                                            //Current mArrProtRevEvtAct Index

    private long mlCurProtRevId = 0;                                        //current Protocol Rev Id
    private long mlCurProtRevEvtId = 0;                                     //Current Protocol Rev Event Id
    private long mlCurProtRevEvtActId = 0;                                  //Current Protocol Rev Event Activity Id

    public long mlCurPatEvtId = -1;                                         //Current ParticipantEvent Id

    private TDevice mCurDevice = new TDevice();                                         //table class: Device
    private TParticipant mCurPat = new TParticipant();                                  //table class: Participant
    private TParticipantEvent mCurPatEvt = new TParticipantEvent();                     //table class: ParticipantEvent
    private TParticipantEventActivity mCurPatEvtAct = new TParticipantEventActivity();  //table class: ParticipantEventActivity

    private ParticipantEvent mJSONPatEvt = new ParticipantEvent();          //JSON output class: ParticipantEvent
    private PatEventResponse mJSONPatEvtRsp = new PatEventResponse();       //JSON output class: PatEventResponse
    private PatEventPicture mJSONPatEvtPic = new PatEventPicture();         //JSON output class: PatEventPicture

    ArrayList<Long> mArrPatEvtIdUpload = new ArrayList<>();                 //list of PatEvtId sent to AsyncThread for Upload
    ArrayList<Long> mArrPatEvtActIdUpload = new ArrayList<>();              //list of PatEvtId sent to AsyncPicThread for Upload

    private String mstrPatFilesRoot;                                        //Root directory for Participant Files

    private String mstrDbVersion;                                           //db version comment

    private int adminUnlockState = -1;                                      //"bottom-top-bottom-top" (0-1-2-3; activity_idle screen divisions) is the unlock pattern

    /**************** start methods ***********************/
    public AlwaysService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        glob = new Globals();                   //init Globals object
        GetPatFromDb();                         //get participant form database (first, for logging)
        InitDirectoryTree();                    //create directory tree if not present
        GetDbVersionFromDb();                   //get database version form database
        GetDeviceFromDb();                      //get participant form database
        GetProtocolFromDb();                    //get initial default protocol from OraDb.db

        restartForeground();                    //start service if not running
        mCurrentService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");
        LogMsg("AlwaysService OnStartCommand");
        mlCount = 0;

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
            Log.e(TAG, "restartForeground:Ex: " + e.getMessage());
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
    public void AlwaysServiceStateMachine() {

        Intent intent;

        if ((mPrevState != mState)
                || (mPrevEventState != mEventState)
                || (mlCount < mlShowIdleCnt)){        //Service or Event State changed, or still showing thank you?
//            Log.i(TAG, "ASSM, State: " + mPrevState + " -> " + mState);                    //Log State
//            Log.i(TAG, "ASSM, EventState: " + mPrevEventState + " -> " + mEventState);     //Log Event State
            Log.i(TAG, "ASSM, SSt: " + mPrevState + " -> " + mState + ", ESt:" + mPrevEventState + " -> " + mEventState);   //Log State

            mPrevState = mState;                                                    //set Prev Svc state
            mPrevEventState = mEventState;                                          //set prev Event state
            String strNextEventTime;                                                //string for next event time
            String strExpireTime = getEvtExpireDtStr();                             //string for expire event time
            switch (mState) {                                                       //Switch Svc state
//20200211
                case ALWAYS_SVC_STATE_ADMIN:
                    intent = new Intent(this, AdminActivity.class);     //Admin Activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EventWindowState", mState);
                    intent.putExtra("ExpireTime", strExpireTime);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    break;
//20200211 end
                case ALWAYS_SVC_STATE_EVT_WIN_OPEN:                                     //Open?, or
                    LogMsg("Event Window Opened, Expires " + strExpireTime);
                    intent = new Intent(this, LoginActivity.class);         //Login Activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EventWindowState", mState);
                    intent.putExtra("ExpireTime", strExpireTime);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    playAlertSound(false);
                break;

                case ALWAYS_SVC_STATE_EVT_WIN_WARN:                                     //Warn?
                    LogMsg("Event Window Warning, Expires " + strExpireTime);
                    intent = new Intent(this, LoginActivity.class);         //Login Activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("EventWindowState", mState);
                    intent.putExtra("ExpireTime", strExpireTime);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    playAlertSound(true);
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_EXPIRE:
//                    intent = new Intent(this, LoginActivity.class);         //Login Activity
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("EventWindowState", mState);
//                    intent.putExtra("ExpireTime", "Expired");
//                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                    startActivity(intent);
                    stopPlaying();

                    strNextEventTime = setNextEvtDtStr();
                    LogMsg("Event Window Expired, next event: " + strNextEventTime);
                    intent = new Intent(this, IdleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("ActTxt", "Event Missed.  Your next event is at " + strNextEventTime);
                    intent.putExtra("ActDbVerTxt", mstrDbVersion);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_RUNNING:          //Running Event
                    switch (mEventState) {                      //Event State?
                        case ALWAYS_SVC_EVENT_NONE:                 //None
                        case ALWAYS_SVC_EVENT_LOGIN:                //Login
                        case ALWAYS_SVC_EVENT_LOGIN_FAIL:           //Login Fail
                            LogMsg("Show Login Activity");
                            intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("EventWindowState", mState);
                            intent.putExtra("ExpireTime", strExpireTime);
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            startActivity(intent);
                            break;

                        case ALWAYS_SVC_EVENT_START:                //Start event?
                            LogMsg("Start Event");
                            StartEvent();                               //Start Event
                            break;

                        case ALWAYS_SVC_EVENT_ABORT:                //Abort?
                            break;

                        case ALWAYS_SVC_EVENT_COMPLETE:             //Complete Event
                            LogMsg("Event Complete");
                            boolean bOK = false;
                            mlShowIdleCnt = mlCount + ALWAYS_SVC_THANKYOU_DLY_CNT;          //change message before upload
                            mlBeginEvtUploadCnt = mlCount + ALWAYS_SVC_UPLOAD_DLY_CNT;      //set upload delay
                            mlChkCatchupUploadCnt = mlCount + ALWAYS_SVC_CATCHUP_DLY_CNT;   //set upload delay

                            bOK = EndParticipantEvent(mCurPatEvt.getPatEvtId());            //close the current event
                            //todo if not OK?
                            SaveParticipantEvent(mCurPatEvt.getPatEvtId());   //Upload the event

                            strNextEventTime = setNextEvtDtStr();                           //set next event time
                            String strMsg = MSG_THANK_YOU;                                  //thank you msg
                            strMsg = strMsg + MSG_IDLE + strNextEventTime;                  //Append time

                            intent = new Intent(this, IdleActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("ActTxt", strMsg);
                            intent.putExtra("ActDbVerTxt", mstrDbVersion);
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            startActivity(intent);

//                            mEventState = ALWAYS_SVC_EVENT_NONE;                    //set event state none
//                            mState = ALWAYS_SVC_STATE_EVT_WIN_COMPLETE;             //set service state complete
                            setServiceState(ALWAYS_SVC_STATE_EVT_WIN_THANKYOU, false);
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

                case ALWAYS_SVC_STATE_EVT_WIN_COMPLETE:           //Upload Current State
//20200218
//                    SaveParticipantEvent(mCurPatEvt.getPatEvtId());   //Upload the event
//                    if ((mlCount > mlShowIdleCnt) && (mlShowIdleCnt > 0)) {    //Done showing thank you?
//                        mlShowIdleCnt = 0;                                          //reset idle count
////                        mState = ALWAYS_SVC_STATE_POLL;                             //Set Polling state
//                        setServiceState(ALWAYS_SVC_STATE_EVT_WIN_THANKYOU, false);                             //Set Polling state
//                    }
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_UPLOAD:           //Upload Current State
//20200218
//                    SaveandUploadParticipantEvent(mCurPatEvt.getPatEvtId());   //Upload the event
                    UploadParticipantEvent(mCurPatEvt.getPatEvtId(), "");   //Upload the event
//                    mState = ALWAYS_SVC_STATE_POLL;                     //back to polling
                    setServiceState(ALWAYS_SVC_STATE_POLL, false);                             //Set Polling state
                    break;

                case ALWAYS_SVC_STATE_CATCHUP_UPLOAD:          //Upload all state
                    UploadNonUploadedEvents();                          //try to upload back events
//                    mState = ALWAYS_SVC_STATE_POLL;                     //back to polling
                    setServiceState(ALWAYS_SVC_STATE_POLL, false);                             //Set Polling state
                    break;

                case ALWAYS_SVC_STATE_EVT_WIN_THANKYOU:             //Thank you state
                case ALWAYS_SVC_STATE_POLL:                         //polling state
                default:
                    strNextEventTime = getNextEvtDtStr();                   //get next event time
                    String strMsg = "";
                    if (mlCount < mlShowIdleCnt) {                          //Show thank you message?
                        strMsg = MSG_THANK_YOU;
                    } else {
                        mlShowIdleCnt = 0;
                    }
                    strMsg = strMsg + MSG_IDLE + strNextEventTime;          //Append idle msg and time

                    intent = new Intent(this, IdleActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("ActTxt", "Your next event is at " + strNextEventTime);
                    intent.putExtra("ActTxt", strMsg);
                    intent.putExtra("ActDbVerTxt", mstrDbVersion);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    startActivity(intent);
                    break;
            }
        }

        if ((mState == ALWAYS_SVC_STATE_EVT_WIN_THANKYOU)           //Thank you state?, or
            || (mState == ALWAYS_SVC_STATE_POLL)) {                 //Polling you state??
            if ((mlCount > mlShowIdleCnt) && (mlShowIdleCnt > 0)) {    //Done showing thank you?
                mlShowIdleCnt = 0;                                          //reset idle count
//                setServiceState(ALWAYS_SVC_STATE_POLL, true);   //Set Polling state
                setServiceEventState(ALWAYS_SVC_EVENT_NONE);                //reset svc event state
            }
        }

        if (mState == ALWAYS_SVC_STATE_POLL) {                                  //Polling?
            if ((mlCount > mlBeginEvtUploadCnt) && (mlBeginEvtUploadCnt > 0 )){     //Idle long enough to start event upload?
                mlBeginEvtUploadCnt = 0;                                                //reset upload counter
                setServiceState(ALWAYS_SVC_STATE_EVT_WIN_UPLOAD, false);                             //Set Polling state
            }
            if ((mlCount > mlChkCatchupUploadCnt) && (mlChkCatchupUploadCnt > 0 )) {    //Idle long enough to start catchup upload?
                mlChkCatchupUploadCnt = mlCount + ALWAYS_SVC_CATCHUP_DLY_CNT;               //set next catchup check                                                  //reset catchup count
                setServiceState(ALWAYS_SVC_STATE_CATCHUP_UPLOAD,false);                             //Set Polling state
            }
        }
        getAlwaysServiceState();        //check for state machine changes for next cycle
    }

    //return ProtRevIdx
    //get the current protocol rev index
    public int setNextProtRevIdx() {
        if (APP_DEMO_MODE) {
            miCurProtRevIdx = 0;        //only one protocol
        } else {
            miCurProtRevIdx = 0;            //todo get protocol revision (should only ever be one anyway)
        }
        return miCurProtRevIdx;
    }

    //return ProtRevEvtIdx
    //get the current event index, or the next one if no active window
    //20200220 This function assumes all Events are Daily
    //ToDo handle events that are not frequency Daily, EvtStart 1
    //ToDo handle events that are not duration 0,null (forever)
    public int setNextProtRevEvtIdx() {

        if (APP_DEMO_MODE) {                //Demo?
            if (miCurProtRevEvtIdx == -1) {     //first time through
                miCurProtRevEvtIdx = 0;             //first event
            } else {                            //otherwise
                if (miCurProtRevEvtIdx == 1) {      //alternate events in demo
                    miCurProtRevEvtIdx = 0;
                } else {
                    miCurProtRevEvtIdx = 1;
                }
            }
        } else {                            //Not Demo?
            try {
                int iEvtIdx = 0;
                String strTimeOpen = "";
                String strTimeClose = "";
                LocalTime tmOpen;
                LocalTime tmClose;
                LocalTime tmNow = LocalTime.now();                          //get current time

                if (miCurProtRevEvtIdx == -1) {                                     //first time through
                    for (int i = 0; i < mArrProtRevEvt.size(); i++) {           //Iterate events that were loaded in TimeOpen order
                        strTimeOpen = mArrProtRevEvt.get(i).getEventTimeOpen();
                        strTimeClose = mArrProtRevEvt.get(i).getEventTimeClose();
                        tmOpen = LocalTime.parse(strTimeOpen);
                        tmClose = LocalTime.parse(strTimeClose);

                        if (tmNow.isBefore(tmClose)) {                              //we are BEFORE close
                            //                        if (tmNow.isAfter(tmOpen)) {                                //we are AFTER open
                            if (miCurProtRevEvtIdx != i) {                              //NOT the current event
                                iEvtIdx = i;                                                //SET The new event index
                                break;                                                      //stop iterating
                            }
                            //                        }
                        }
                    }
                    miCurProtRevEvtIdx = iEvtIdx;       //set the new current Event Idx (back to 0 if not found)
                } else {
                    if ((mState == ALWAYS_SVC_STATE_EVT_WIN_EXPIRE)         //we just expired? or
                            || ((mState == ALWAYS_SVC_STATE_EVT_WIN_RUNNING
                                && mEventState == ALWAYS_SVC_EVENT_COMPLETE)) ) {     //we just completed?
                        int iIdx = miCurProtRevEvtIdx;                          //get current Event index
                        iIdx++;                                                 //increment
                        if (iIdx >= mArrProtRevEvt.size()) {                    //to high?
                            iIdx = 0;                                               //first
                        }
                        miCurProtRevEvtIdx = iIdx;                              //set current index
                    }
                }
            } catch (NullPointerException e) {
                Log.e("AlwaysService:setNextProtRevEvtIdx:NPEx", e.toString());
                //todo handle
            }
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

        LocalTime tmNow = LocalTime.now();                                          //get current time
        LocalDate dtNextEvt = LocalDate.now();                                      //get current date
        LocalDateTime locDt = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);  //get current DateTime to minutes
        DateTimeFormatter fmtDt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");  //set up format

        if (APP_DEMO_MODE) {                                                //demo?
            mDtNextEvtStart = locDt.plusMinutes(APP_DEMO_MODE_MIN_OPEN);        //set values
            mDtNextEvtWarn = locDt.plusMinutes(APP_DEMO_MODE_MIN_WARN);
            mDtNextEvtExpire = locDt.plusMinutes(APP_DEMO_MODE_MIN_EXPIRE);
        } else {                                                            //NOT demo
            if (miCurProtRevEvtIdx == -1) {                                     //first time through
                setNextProtRevEvtIdx();                                             //set next index (miCurProtRevEvtIdx)
            } else {
                if ((mState == ALWAYS_SVC_STATE_EVT_WIN_EXPIRE)         //we just expired? or
                 || (mState == ALWAYS_SVC_STATE_EVT_WIN_RUNNING)) {     //we just completed?
                    int iIdx = miCurProtRevEvtIdx;                          //get current Event index
                    iIdx++;                                                 //increment
                    if (iIdx >= mArrProtRevEvt.size()) {                    //to high?
                        iIdx = 0;                                               //first
                    }
                    miCurProtRevEvtIdx = iIdx;                              //set current index
                }
            }
            String strTimeOpen = "";
            String strTimeWarn = "";
            String strTimeClose = "";
            LocalTime tmOpen;
            LocalTime tmWarn;
            LocalTime tmClose;

            strTimeOpen = mArrProtRevEvt.get(miCurProtRevEvtIdx).getEventTimeOpen();
            strTimeWarn = mArrProtRevEvt.get(miCurProtRevEvtIdx).getEventTimeWarn();
            strTimeClose = mArrProtRevEvt.get(miCurProtRevEvtIdx).getEventTimeClose();
            tmOpen = LocalTime.parse(strTimeOpen);
            tmWarn = LocalTime.parse(strTimeWarn);
            tmClose = LocalTime.parse(strTimeClose);

            if (tmNow.isAfter(tmClose)) {                              //we are AFTER Close, so it must be tomorrow
                dtNextEvt = dtNextEvt.plusDays(1);                          //add a day
            }

            mDtNextEvtStart = dtNextEvt.atTime(tmOpen);
            mDtNextEvtWarn = dtNextEvt.atTime(tmWarn);
            mDtNextEvtExpire = dtNextEvt.atTime(tmClose);
            Log.d(TAG, "setNextEvtDtStr: " + mDtNextEvtStart.format(fmtDt) + ", " + mDtNextEvtWarn.format(fmtDt) + ", " + mDtNextEvtExpire.format(fmtDt));
            LogMsg("setNextEvtDtStr: " + mDtNextEvtStart.format(fmtDt) + ", " + mDtNextEvtWarn.format(fmtDt) + ", " + mDtNextEvtExpire.format(fmtDt));
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

        if (mState == ALWAYS_SVC_STATE_ADMIN) {             //Admin state?
            return mState;                                      //stay there
        }
        if (mState == ALWAYS_SVC_STATE_EVT_WIN_RUNNING) {   //Event Running state?
            return mState;                                      //stay there
        }
        if (mState == ALWAYS_SVC_STATE_EVT_WIN_COMPLETE) {   //Event Complete state?
            return mState;                                      //stay there
        }
        if (mState == ALWAYS_SVC_STATE_EVT_WIN_UPLOAD) {    //Event Upload state?
            return mState;                                      //stay there
        }
        if (mState == ALWAYS_SVC_STATE_CATCHUP_UPLOAD) {    //Event Catchup state?
            return mState;                                      //stay there
        }

        if (mDtNextEvtStart == null) {              //next not populated?
            setNextEvtDtStr();                          //populate next
        }
        LocalDateTime dtNow = LocalDateTime.now();  //get date/time

        /////// If we get here, we know we are NOT in the Admin State, and NOT Already Running an Event ///////

        if (mDtNextEvtStart.isBefore(dtNow)) {      //We are after event Window Open
            if (mDtNextEvtWarn.isBefore(dtNow)) {       //We are after Window Open, after Warn
                if (mDtNextEvtExpire.isBefore(dtNow)) {       //We are after Warn and after Expire
                    if (mState != ALWAYS_SVC_STATE_EVT_WIN_EXPIRE) {    //Not EXPIRE?
//                        mState = ALWAYS_SVC_STATE_EVT_WIN_EXPIRE;           //set Expired
                        setServiceState(ALWAYS_SVC_STATE_EVT_WIN_EXPIRE, true);                             //Set Polling state
                    }
                } else {                                    //We are after Warn but before Expire
                    if (mState != ALWAYS_SVC_STATE_EVT_WIN_WARN) {  //Not WARN?
//                        mState = ALWAYS_SVC_STATE_EVT_WIN_WARN;         //set Warn
                        setServiceState(ALWAYS_SVC_STATE_EVT_WIN_WARN, true);                             //Set Polling state
                    }
                }
            } else {                                    //Otherwise, We are after Window Open, but before Warn
                if (mState != ALWAYS_SVC_STATE_EVT_WIN_OPEN) {  //Not WIN OPEN?
//                    mState = ALWAYS_SVC_STATE_EVT_WIN_OPEN;         //set Window Open
                    setServiceState(ALWAYS_SVC_STATE_EVT_WIN_OPEN, true);                             //Set Polling state
                }
            }
        }
        else {                                      //otherwise
//            mState = ALWAYS_SVC_STATE_POLL;             //keep polling
            setServiceState(ALWAYS_SVC_STATE_POLL, false);                             //Set Polling state

        }
        return mState;
    }

    /** Get current Always Service State */
    public int getAlwaysServiceState() {
        String strLog = "Timer loop: Ctr:";
        strLog = strLog + (mlCount++) + " SS:" + mState + " ES:" + mEventState;
        strLog = strLog + ": Id:" + mlShowIdleCnt;
        strLog = strLog + ": Upl:" + mlBeginEvtUploadCnt;
        strLog = strLog + ": Cat:" + mlChkCatchupUploadCnt;

        Log.i(TAG, strLog);         //Log State

        getNextEvtWinState();       //check for state update
        return mState;
    }

    /* Get the current participant from the database */
    private boolean GetDbVersionFromDb() {
        boolean bRet = false;
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            dba.open();                                                                 //open db
            String strQry = "SELECT * FROM tDBVersion";                                    //set SQL
            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate Cursor
                //set current Participant
                mstrDbVersion = crs.getString(crs.getColumnIndex("dbVersionComment"));  //db version
                bRet = true;
                break;
            }
            crs.close();
            dba.close();

        } catch (NullPointerException e) {
            Log.e("AlwaysService:GetDbVersionFromDb:NPEx", e.toString());

            mstrDbVersion = "Unable to get db Version";
        }
        LogMsg("Database Version: " + mstrDbVersion);
        return bRet;
    }

    private boolean GetDeviceFromDb() {
        boolean bRet = false;
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            dba.open();                                                                 //open db
            String strQry = "SELECT * FROM tDevice";                                    //set SQL
            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate Cursor
                //set current Participant
                mCurDevice.setDeviceId(crs.getLong(crs.getColumnIndex("DeviceId")));        //DeviceId
                mCurDevice.setDeviceAppId(crs.getString(crs.getColumnIndex("DeviceAppId")));//Device App Id (text)
                bRet = true;
                break;
            }
            crs.close();
            dba.close();

        } catch (NullPointerException e) {
            Log.e("AlwaysService:GetDeviceFromDb:NPEx", e.toString());

            Log.i("AlwaysService:GetDeviceFromDb:SetDefaults", e.toString());
            mCurDevice.setDeviceId(APP_DFLT_DEVICE_ID);         //default DeviceId
            mCurDevice.setDeviceAppId(APP_DFLT_DEVICE_APPID);   //default Device App Id (text)
        }
        LogMsg("Device App ID: " + mCurDevice.getDeviceAppId());
        return bRet;
    }

    /* Get the current participant from the database */
    private boolean GetPatFromDb() {
        boolean bRet = false;
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            dba.open();                                                                 //open db
            String strQry = "SELECT * FROM tParticipant";                               //set SQL
            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate Cursor
                                                                                            //set current Participant
                mCurPat.setPatId(crs.getLong(crs.getColumnIndex("PatId")));             //PatId
                mCurPat.setPatYearId(crs.getInt(crs.getColumnIndex("PatYearId")));     //YearId
                mCurPat.setPatDeptId(crs.getInt(crs.getColumnIndex("PatDeptId")));     //DeptId
                mCurPat.setPatStudyId(crs.getInt(crs.getColumnIndex("PatStudyId")));   //StudyId
                mCurPat.setPatLocationId(crs.getInt(crs.getColumnIndex("PatLocationId")));     //LocationId
                mCurPat.setPatNumber(crs.getInt(crs.getColumnIndex("PatNumber")));      //Pat number
                String strFQPatNumber = crs.getString(crs.getColumnIndex("StudyPatNumber"));    //full qualify num
                if (strFQPatNumber.length() == 0) {
                    //todo format the fully qualified number if empty
                }
                mCurPat.setStudyPatNumber(strFQPatNumber);                              //fully qualify patient number
                mstrPatFilesRoot = strFQPatNumber;
                bRet = true;
                break;
            }
            crs.close();
            dba.close();

        } catch (NullPointerException e) {
            Log.e("AlwaysService:GetPatFromDb:NPEx", e.toString());

            Log.i("AlwaysService:GetPatFromDb:SetDefaults", e.toString());
            mCurPat.setPatId(APP_DFLT_PAT_ID);              //PatId
            mCurPat.setPatDeptId(APP_DFLT_PAT_DEPTID);      //DeptId
            mCurPat.setPatStudyId(APP_DFLT_PAT_STUDYID);    //StudyId
            mCurPat.setPatLocationId(APP_DFLT_PAT_LOCID);   //LocationId
            mCurPat.setPatNumber(APP_DFLT_PAT_NUM);         //Pat number
            mCurPat.setStudyPatNumber(APP_DFLT_PAT_STUDYPATNUM); //fully qualify patient number
        }
        LogMsg("Get Participant: " + mCurPat.getStudyPatNumber());
        return bRet;
    }

    private boolean GetProtocolFromDb() {
        boolean bRet = false;
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
//            String strQry = "SELECT * FROM vProtRevEvtActivities WHERE ActId is not NULL and ActTypeId IS NOT NULL ORDER BY ProtRevId, ProtRevEvtId, ActSeq, ProtRevEvtActId, ActRspSeq";
            String strQry = "SELECT * FROM vProtRevEvtActExp";
            strQry = strQry + " WHERE ActId is not NULL and ActTypeId IS NOT NULL";
            strQry = strQry + " ORDER BY ProtRevId, ProtRevEvtId, ActSeq, ProtRevEvtActId, ActRspSeq";
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
                        newProtRev.setProtocolName(crs.getString(crs.getColumnIndex("ProtName")));
                        newProtRev.setProtocolRevName(crs.getString(crs.getColumnIndex("ProtRevName")));
                        newProtRev.setProtocolRevDt(crs.getString(crs.getColumnIndex("ProtRevDt")));
//                        newProtRev.setProtocolRevEventCnt(crs.getLong(crs.getColumnIndex("EvtCnt")));
//                        newProtRev.setProtocolRevEventCnt(crs.getLong(crs.getColumnIndex("ProtRevEvtCnt")));          //tProtRev stored event count
                        newProtRev.setProtocolRevEventCnt(crs.getLong(crs.getColumnIndex("CalcEvtCnt")));   //view calculated event count

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
                        newProtRevEvt.setEventDaysDuration(crs.getLong(crs.getColumnIndex("EvtDaysDuration")));
                        newProtRevEvt.setEventTimeOpen(crs.getString(crs.getColumnIndex("EvtTimeOpen")));
                        newProtRevEvt.setEventTimeWarn(crs.getString(crs.getColumnIndex("EvtTimeWarn")));
                        newProtRevEvt.setEventTimeClose(crs.getString(crs.getColumnIndex("EvtTimeClose")));
//                        newProtRevEvt.setEventActivityCnt(crs.getLong(crs.getColumnIndex("eventActivityCnt")));
//                        newProtRevEvt.setEventActivityCnt(crs.getLong(crs.getColumnIndex("ProtRevEvtActCnt")));           //tProtRevEvent stored count
                        newProtRevEvt.setEventActivityCnt(crs.getLong(crs.getColumnIndex("CalcActCnt")));       //view calculated count

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
                        newProtRevEvtAct.setProtRevEvtApplyTo(crs.getString(crs.getColumnIndex("ProtRevEvtApplyTo")));
                        newProtRevEvtAct.setMinRange(crs.getLong(crs.getColumnIndex("MinRange")));
                        newProtRevEvtAct.setMaxRange(crs.getLong(crs.getColumnIndex("MaxRange")));
                        newProtRevEvtAct.setActivityTypeId(crs.getLong(crs.getColumnIndex("ActTypeId")));
                        newProtRevEvtAct.setActivityTypeCode(crs.getString(crs.getColumnIndex("ActTypeCode")));
                        newProtRevEvtAct.setActivityText(crs.getString(crs.getColumnIndex("ActTxt")));
                        newProtRevEvtAct.setActivityPictureCode(crs.getString(crs.getColumnIndex("ActPictureCode")));
                        newProtRevEvtAct.setActivityResponseTypeId(crs.getLong(crs.getColumnIndex("RspTypeId")));
                        newProtRevEvtAct.setActivityResponseTypeCode(crs.getString(crs.getColumnIndex("ActResponseTypeCode")));
//                    newProtRevEvtAct.setImageFileId(crs.getLong(crs.getColumnIndex("ActImage")));
//                    newProtRevEvtAct.setImageFileId(crs.getLong(crs.getColumnIndex("ImageFileId")));
//                    newProtRevEvtAct.setImageFileName(crs.getString(crs.getColumnIndex("ImageFileName")));
//                    newProtRevEvtAct.setImageFilePath(crs.getString(crs.getColumnIndex("ImageFilePath")));
                        newProtRevEvtAct.setActivityResponseCnt(crs.getLong(crs.getColumnIndex("ActRspCnt")));      //tProtRevEventActivity stored response count
                        newProtRevEvtAct.setActivityResponseCnt(crs.getLong(crs.getColumnIndex("CalcRspCnt")));     //view calculated response count

                        lPrevAct = lCurAct;
                    }

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
                            newProtRevEvtActRsp.setActRspValue(crs.getLong(crs.getColumnIndex("ActRspVal")));
                            newProtRevEvtActRsp.setActRspText(crs.getString(crs.getColumnIndex("ActRspTxt")));

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
            bRet = true;
        } catch (NullPointerException e) {
            Log.e("AlwaysService:GetProtocolFromDB:NPEx", e.toString());
            //todo handle
        }
        LogMsg("Get Protocol Rev Id: " + mArrProtRev.get(0).getProtocolRevId());
        return bRet;
    }

    /**
     * initialize directory structure
     */
    private boolean InitDirectoryTree() {
        boolean bRet = false;
        Context ctx = getApplicationContext();
        File fDir;
        File fFile;
        FileOutputStream fStream;
        String str;
        byte [] sBytes;

//        fFile = new File(ctx.getExternalFilesDir(APP_DIR_PROTOCOL), "Readme.dat");
        fFile = new File(ctx.getExternalFilesDir(APP_DIR_PROTOCOL), "Readme.dat");
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

        fFile = new File(ctx.getExternalFilesDir(APP_DIR_PROTOCOL_ARCHIVE), "Readme.dat");
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

        fFile = new File(ctx.getExternalFilesDir(APP_DIR_PARTICIPANTS), "Readme.dat");
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

        fFile = new File(ctx.getExternalFilesDir(APP_DIR_DATA), "Readme.dat");
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

        fFile = new File(ctx.getExternalFilesDir(APP_DIR_DATA_ARCHIVE), "Readme.dat");
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

        fFile = new File(ctx.getExternalFilesDir(APP_DIR_DATA_FRESH), "Readme.dat");
        try {
            if (!fFile.exists()) {
                str = "A Fresh, empty Database is stored in this directory.";
                sBytes = glob.ByteArrayFromString(str);
                fStream = new FileOutputStream(fFile); //Use the stream as usual to write into the file.
                fStream.write(sBytes);
                fStream.close();
            }

            bRet = true;
        } catch (IOException e) {
            Log.e("AlwaysService:InitDirectoryTree:Readme:IOEx", e.toString());
            //todo handle
        }
        LogMsg("Init Directory Tree");
        return bRet;
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
//20200212 move to setServiceEventState
//                mState = ALWAYS_SVC_STATE_POLL;                 //start polling
//            }
//            mEventState = ALWAYS_SVC_EVENT_NONE;            //No event in progress
            setServiceEventState(ALWAYS_SVC_EVENT_NONE);    //
        }
        return true;
    }

    public int StartEvent() {

        mlCurProtRevId = mArrProtRev.get(setNextProtRevIdx()).getProtocolRevId();                                   //get Current Protocol Rev Id
        mlCurProtRevEvtId = mArrProtRevEvt.get(setNextProtRevEvtIdx()).getProtocolRevEventId();                     //get Current Protocol Rev Event Id
        mlCurProtRevEvtActId = mArrProtRevEvtAct.get(setNextProtRevEvtActIdx()).getProtocolRevEventActivityId();    //get Current Protocol Rev Event Activity Id

        mlCurPatEvtId = StartParticipantEvent();

        GotoEvtAct(miCurActIdx);

        return miCurActIdx;
//        return StartProtocol();
    }

    public String getStudyPatNumber() {
//        return mstrPatNumber;
        return mCurPat.getStudyPatNumber();
    }

    /* Get next Activity Index in the Event */
    public int setNextActivityIdx() {
        miCurActIdx++;                                              //increment activity index
//20200212 check is in ASSM
        if (miCurActIdx == mArrProtRevEvtAct.size()) {              //passed end of array?
//            setServiceEventState(ALWAYS_SVC_EVENT_COMPLETE);            //set event complete
//            AlwaysServiceStateMachine();                                //exercise state machine
            return miCurActIdx;                                         //bail, return index
        }

        while (mArrProtRevEvtAct.get(miCurActIdx).getProtocolRevEventId() != mlCurProtRevEvtId) {   //different event?
            miCurActIdx++;                                                                              //increment again
            if (miCurActIdx == mArrProtRevEvtAct.size()) {              //passed end of array?
//                setServiceEventState(ALWAYS_SVC_EVENT_COMPLETE);            //set event complete
//                AlwaysServiceStateMachine();                                //exercise state machine
                break;                                                      //bail
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
//            AlwaysServiceStateMachine();                                //exercise state machine
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
                intent.putExtra("PatNum", getStudyPatNumber());
                startActivity(intent);

                break;
            default:
                break;
        }
    }

    public void SetAdminUnlockState(boolean topScreenSection) {
        if (topScreenSection) {
            if (adminUnlockState == 0)
                adminUnlockState++;
            else {
                if (adminUnlockState == 2)
                    GoToAdmin();
                adminUnlockState = -1;
            }
        }
        else {
            if (adminUnlockState == -1 || adminUnlockState == 1)
                adminUnlockState++;
            else
                adminUnlockState = -1;
        }
    }

    private void GoToAdmin() {
        Log.d("AlwaysService", "GoToAdmin");

//        Intent intent = new Intent(this, AdminActivity.class);
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

        LogMsg("Admin Login triggered");
        Intent intent = new Intent(this, LoginActivity.class);         //Login Activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EventWindowState", mState);
        intent.putExtra("ExpireTime", "Admin Login");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        startActivity(intent);

    }
    public void saveAdminChanges () {
        refreshParticipant();
        refreshDevice();
        setServiceState(ALWAYS_SVC_STATE_POLL, false);

    }
    public void refreshParticipant() {
        GetPatFromDb();                         //get participant form database
    }

    public void refreshDevice() {
        GetDeviceFromDb();                      //get participant form database
    }

    public void setLoginEvtState(int iEvtState) {
        if (iEvtState == LOGIN_PARTICPANT_ERR_NO_ERR) {         //Evt State Part Login
            setServiceEventState(ALWAYS_SVC_EVENT_START);           //start an event
        } else if (iEvtState == LOGIN_ADMIN_ERR_NO_ERR) {       //admin login?
            setServiceEventState(ALWAYS_SVC_EVENT_ADMIN);           //start admin
        }
    }

    /** set service state */
    public void setServiceEventState(int iNewState) {

        mEventState = iNewState;                                //set new state
        switch (mEventState) {
            case ALWAYS_SVC_EVENT_START:                        //Evt State Start Event
                setServiceState(ALWAYS_SVC_STATE_EVT_WIN_RUNNING, true);      //set event running
                break;
            case ALWAYS_SVC_EVENT_ADMIN:                        //Event state admin?
                setServiceState(ALWAYS_SVC_STATE_ADMIN, true);                //set state admin
                break;
//            case ALWAYS_SVC_EVENT_COMPLETE:                     //Event state complete
//                setServiceState(ALWAYS_SVC_STATE_EVT_WIN_COMPLETE, false); //set state complete
//                break;
            case ALWAYS_SVC_EVENT_NONE:                         //Event state no event
                setServiceState(ALWAYS_SVC_STATE_POLL, true);             //set state polling
                break;
            case ALWAYS_SVC_EVENT_COMPLETE:                     //Event state complete, no reason to change Service state; force ASSM do that now
                setServiceState(mState, true);          //no change to state, but force State machine
                break;
            default:                                            //otherwise
                setServiceState(mState, false);         //no change to state, do not force State machine
                break;
        }
    }

    /** set service state */
    public int setServiceState(int iNewState, boolean bForceStMach) {

        mState = iNewState;
        if (bForceStMach) {
            AlwaysServiceStateMachine();
        }

        return iNewState;
    }

    public int TryLogin(String username, String password) {

//        if (username.equals(LOGIN_ADMIN_NAME)) {        //admin user name?
        if (username.toUpperCase().equals(LOGIN_ADMIN_NAME)) {        //admin user name?
            if (password.equals(LOGIN_ADMIN_PW)) {          //password OK?
                LogMsg("Administrator Login");
                return LOGIN_ADMIN_ERR_NO_ERR;                  //return admin login
            } else {                                        //otherwise
                LogMsg("Administrator Login Fail");
                return LOGIN_ADMIN_ERR_PW;                      //return admin password error
            }
        }

        /////////////// NOT Admin ID - Continue ////////////////////
        int iPatID = -1;
        int iPatNum;
        String strPatNum;
        String strPatStudyNum = "";
        String strPatPW = "";

            //        DatabaseAccess dba = DatabaseAccess.getInstance(Global.GetAppContext());
        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            dba.open();                                                                 //open db
            String strQry = "SELECT * FROM tParticipant ORDER BY PatNumber";
            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
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
        } catch (NullPointerException e) {
            Log.e("AlwaysService:TryLogin:NPEx", e.toString());
            //todo handle
        }

        if (iPatID == -1) {                     //user not found
            LogMsg("Administrator Login ID Fail");
            return LOGIN_PARTICPANT_ERR_ID;         //return Participant ID error
        }

        if (!strPatPW.equals(password)) {       //wrong password
            LogMsg("Participant Login PW Fail");
            return LOGIN_PARTICPANT_ERR_PW;         //return Participant password error
        } else {                                //Otherwise
            LogMsg("Participant Login");
            return LOGIN_PARTICPANT_ERR_NO_ERR;     //return Participant Login OK
        }
    }

    //edit: call each time a participant event activity is committed; set "entity" to a PatEventResponse or a PatEventPicture
    public boolean SaveActivityResult(int iActIdx, int iResponseVal, String strResponseTxt, String strResponsePath) {

        if (iActIdx <= 0)       //not valid index?
            return false;                 //bail

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get and open database
        try {
            TParticipantEventActivity actResp = new TParticipantEventActivity();        //new event activity to record
            actResp.setPatevtid(mlCurPatEvtId);                                         //set patient event id
            actResp.setProtrevevtactid(mArrProtRevEvtAct.get(iActIdx).getProtocolRevEventActivityId().intValue());  //set prot rev event Activity Id
            actResp.setResponseVal(iResponseVal);                                       //set Response value
            actResp.setResponseTxt(strResponseTxt);                                     //set Response text
            actResp.setResponsePath(strResponsePath);                                   //set Response path

            dba.open();                                                                 //open db
            dba.SavePatEvtActivityResponse(actResp);                                    //Save it
            dba.close();                                                                //close db
            LogMsg("ActivityResult: " + mlCurPatEvtId + "," + actResp.getProtrevevtactid() + "," + iResponseVal + "," + strResponseTxt + "," + strResponsePath);
        } catch (NullPointerException e ) {
            Log.e("AlwaysService:SaveActivityResult:NPEx", e.toString());
            //todo handle, try again?
            return false;
        }
        return true;
    }

    //participant event begins;
    // Create or init new mCurPatEvt
    // Create new Pat Event in database
    //
    // returns its corresponding "PatEvtId";
    public long StartParticipantEvent() {

//        ParticipantEvent participantEvent = GetParticipantInfoEntity();

        Globals glob = new Globals();
        String strDt = glob.GetDateStr(DT_FMT_FULL_ACTIVITY, glob.getDate());       //get datetime now
        long lNewPatEvtId = 0L;

        if (mCurPatEvt == null)                     //cur event not initiallized?
            mCurPatEvt = new TParticipantEvent();       //init
                                                    //set up new event iun database
        mCurPatEvt.setPatId(mCurPat.getPatId());
        mCurPatEvt.setDeviceId(mCurDevice.getDeviceId());
        mCurPatEvt.setProtrevevtid(mlCurProtRevEvtId);
        mCurPatEvt.setPatEvtDtStart(strDt);
        mCurPatEvt.setPatEvtDtEnd("");
        mCurPatEvt.setPatEvtDtUpload("");
        mCurPatEvt.setPatEvtFileName("");
        mCurPatEvt.setPatEvtResponseCnt(0);
        mCurPatEvt.setPatEvtPictureCnt(0);

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get and open database
        try {
            dba.open();                                                                 //open db
            lNewPatEvtId = dba.InsertTParticipantEvent(mCurPatEvt);                     //create new database event;
            mCurPatEvt.setPatEvtId(lNewPatEvtId);                                       //set current PatEvtId
            dba.close();                                                                //close db

        } catch (NullPointerException e ) {
            Log.e("AlwaysService:StartParticipantEvent:NPEx", e.toString());
            //todo handle, try again?
        }
        return lNewPatEvtId;
    }

    public void CommitActivityInfo(long lPatEvtId, Object entity) { //edit: call each time a participant event activity is committed; set "entity" to a PatEventResponse or a PatEventPicture
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
            dba.InsertParticipantResponse(lPatEvtId, (PatEventResponse)entity);
            dba.UpdateParticipantEventChildCnt(lPatEvtId, true, GetPatActivityRespOrPicCnt(lPatEvtId, false, dba));
        }
        else {
            dba.InsertParticipantPicture(lPatEvtId, (PatEventPicture) entity);
            dba.UpdateParticipantEventChildCnt(lPatEvtId, false, GetPatActivityRespOrPicCnt(lPatEvtId, true, dba));
        }
        dba.close();
    }
    private int GetPatActivityRespOrPicCnt(long lPatEvtId, boolean isPicture, DatabaseAccess dba) { //dba must be open during this for it to work properly
        if (dba == null)
            return -1; //default for method misuse
        List<Object[]> data = dba.GetTableData("tParticipantEvent");
        int oldEvtCnt = 0;
        for (int i = 0; i < data.get(0).length; i++) {
            if (data.get(0)[i].equals("PatEvtId")) {
                for (int j = 1; j < data.size(); j++) {
                    if ((long)data.get(j)[i] == lPatEvtId) {

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

    //call each time a participant event ends
    public boolean EndParticipantEvent(long lPatEvtId) {
        boolean bRet = false;
        Globals glob = new Globals();
        String strDt = glob.GetDateStr(DT_FMT_FULL_ACTIVITY, glob.getDate());       //get datetime now
        String path = "";

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get and open database
        try {
            dba.open();                                                         //open
            dba.MarkParticipantEventEnded(lPatEvtId, strDt);                    //mark event ended
            bRet = true;
        } catch (NullPointerException e ) {
            Log.e(TAG + ":EndParticipantEvent:NPEx", e.toString());
            //todo handle, try again?
        } catch (Exception e) {
            Log.e(TAG + ":EndParticipantEvent:Ex", e.toString());
        } finally {
            dba.close();
        }
        return bRet;
    }

    //Save the output JSON; returns the file path of the output json;
    public String SaveParticipantEvent(long lPatEvtId) {
        String path = "";
        String strFile = "";

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get and open database
        try {
            dba.open();                                                         //open

            path = dba.CreateJSON("vOuterPatEvtAll",           //create JSON output
                    "PatEventResponses",
                    "vInnerPatEvtAll",
                    "PatEventImages",
                    "vInnerPatEvtImagesAll",
                    lPatEvtId,
                    getApplicationContext());                                   //returns full path of json object

            if (!path.equals("")) {                                             //good path?
                Path p = Paths.get(path);                                           //path object
                strFile = p.getFileName().toString();                               //get filename only
                LogMsg("Event JSON Created: " + strFile);
                dba.UpdateTParticipantEventFileName(lPatEvtId, strFile);            //update the tParticipantEvent json file name
            }

        } catch (NullPointerException e ) {
            Log.e(TAG + ":SaveParticipantEvent:NPEx", e.toString());
            //todo handle, try again?
        } catch (Exception e) {
            Log.e(TAG + ":SaveParticipantEvent:Ex", e.toString());
        } finally {
            dba.close();
        }
        return path;
    }

    //Upload a specific Participant Event
    public String SaveandUploadParticipantEvent(long lPatEvtId) { //edit: call each time a participant event ends; returns the file path of the output json; figure out how to get the caller connected to this service
        String path = "";
        String strFile = "";
        String strPatEvtId = "";

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get and open database
        try {
            dba.open();                                                         //open

            String strURL = URL_EVENT_UPLOAD + mCurDevice.getDeviceId() + "/" + mCurDevice.getDeviceAppId() + "/";  //get url
//            strURL = "https://postman-echo.com/post";
//            strURL = "https://icupapi.lionridgedev.com/v1/diary/basic";

            strPatEvtId = Long.toString(lPatEvtId);                             //get PatEvtId
            path = dba.CreateJSON("vOuterPatEvtAll",           //create JSON output
                    "PatEventResponses",
                    "vInnerPatEvtAll",
                    "PatEventImages",
                    "vInnerPatEvtImagesAll",
                    lPatEvtId,
                    getApplicationContext());                                   //full path of json object

            if (!path.equals("")) {                                             //good path?
                Path p = Paths.get(path);                                           //path object
                strFile = p.getFileName().toString();                               //get filename only
                dba.UpdateTParticipantEventFileName(lPatEvtId, strFile);            //update the tParticipantEvent json file name
            }
//            dba.TrySendJSONToServer(strURL, strPatEvtId, path, strFile);        //upload event JSON
            TrySendJSONToServer(strURL, strPatEvtId, path, strFile);        //upload event JSON
            UploadParticipantEventPictures(lPatEvtId);                          //upload event pictures

        } catch (NullPointerException e ) {
            Log.e(TAG + ":EndParticipantEvent:NPEx", e.toString());
            //todo handle, try again?
        } catch (Exception e) {
            Log.e(TAG + ":EndParticipantEvent:Ex", e.toString());
        } finally {
            dba.close();
        }
        return path;
    }

    //edit: call each time a participant event ends;
    // returns the file path of the output json; figure out how to get the caller connected to this service
    //Upload a specific Participant Event
    //the JSON has already been created
    public String UploadParticipantEvent(long lPatEvtId, String strFile) {
        String path = "";
        String strPatEvtId = "";

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get and open database
        try {
            if (strFile.length() == 0) {                                                //no file name?
                dba.open();                                                                 //open db
                String strQry = "SELECT PatEvtId, PatEvtFileName FROM vPatEvtCompNoUpload";
                strQry = strQry + " WHERE PatEvtId = " + lPatEvtId;
                Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
                while (crs.moveToNext()) {                                                      //Iterate Events
                    strFile = crs.getString(crs.getColumnIndex("PatEvtFileName"));  //Get file name
                }
                crs.close();
                dba.close();
            }

            strPatEvtId = Long.toString(lPatEvtId);                             //get PatEvtId

            String strDir = APP_DIR_PARTICIPANTS + "/" + strFile.substring(0,APP_PAT_NUM_LEN) + APP_DIR_PARTICIPANT_EVENTS; //get dir
            File fNewFile = new File(getApplicationContext().getExternalFilesDir(strDir), strFile);                         //get file
            if (!fNewFile.isFile()) {                                                           //doesn't exist?
                path = SaveParticipantEvent(lPatEvtId);                                             //save it now
            }

            if (fNewFile.isFile()) {                                                            //exists?
                path = fNewFile.getAbsolutePath();

                String strURL = URL_EVENT_UPLOAD + mCurDevice.getDeviceId() + "/" + mCurDevice.getDeviceAppId() + "/";  //get url
//            strURL = "https://postman-echo.com/post";
//            strURL = "https://icupapi.lionridgedev.com/v1/diary/basic";

//                String strResp = dba.TrySendJSONToServer(strURL, strPatEvtId, path, strFile);        //upload event JSON
                String strResp = TrySendJSONToServer(strURL, strPatEvtId, path, strFile);        //upload event JSON
                LogMsg("Upload " + strFile + " " + strResp);
            }
            UploadParticipantEventPictures(lPatEvtId);                          //upload event pictures

        } catch (NullPointerException e ) {
            Log.e(TAG + ":EndParticipantEvent:NPEx", e.toString());
            //todo handle, try again?
        } catch (Exception e) {
            Log.e(TAG + ":EndParticipantEvent:Ex", e.toString());
        } finally {
            dba.close();
        }
        return path;
    }

    //upload pictures for a specific event
    public boolean UploadParticipantEventPictures(long lPatEvtId) {
        boolean bRet = false;

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            long lPatEvtActId;
            String strFile;
            String strDir = APP_DIR_PARTICIPANTS + "/" + mCurPat.getStudyPatNumber() + APP_DIR_PARTICIPANT_PICS;
            File fPicFile;
            String strPicFile;

            String strQry = "SELECT PatEvtActId, PatEvtActFileName FROM vPatEvtActNoUpload";
            if (lPatEvtId > 0) {
                strQry = strQry + " WHERE PatEvtId = " + lPatEvtId;
            }
            dba.open();                                                         //open db
            Cursor crs = dba.db.rawQuery(strQry, null);             //get cursor to view
            try {
                while (crs.moveToNext()) {                                          //Iterate Events

                    try {
                        Thread.sleep(1000 * ALWAYS_SVC_UPLOAD_PIC_DLY_CNT);     //no rush uploading pictures
                    } catch (InterruptedException e) {
                        Log.e(TAG + ":UploadParticipantEventPictures:Sleep:IntEx", e.toString());
                        //todo handle
                    }
                    lPatEvtActId = crs.getInt(crs.getColumnIndex("PatEvtActId"));       //Get Id
                    strFile = crs.getString(crs.getColumnIndex("PatEvtActFileName"));   //Get file name

                    try {
                        String strURL = URL_PICTURE_UPLOAD + mCurDevice.getDeviceId() + "/" + mCurDevice.getDeviceAppId() + "/";  //get url
//                    strURL = "https://postman-echo.com/post";
//                    strURL = "https://icupapi.lionridgedev.com/v1/diary/basic";

                        String strPatEvtActId = Long.toString(lPatEvtActId);                                //get Event Act Id
                        fPicFile = new File(getApplicationContext().getExternalFilesDir(strDir), strFile);  //get fully qualified file
                        strPicFile = fPicFile.getAbsolutePath();                                            //get fully qualified path
                        Log.d(TAG + "::UploadParticipantEventPictures", strFile);

//                        String strResp = dba.TrySendPictureToServer(strURL, strPatEvtActId, strPicFile, strFile);
                        String strResp = TrySendPictureToServer(strURL, strPatEvtActId, strPicFile, strFile);
                        LogMsg("Upload " + strFile + " " + strResp);
                    } catch (Exception e) {
                        Log.e(TAG + ":UploadParticipantEventPictures:Ex", e.toString());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG + ":UploadParticipantEventPictures:Ex", e.toString());
            } finally {
                if (crs != null) {
                    crs.close();
                }
            }
        } catch (NullPointerException e) {
            Log.e(TAG + ":UploadParticipantEventPictures:NPEx", e.toString());
            //todo handle
        } finally {
            if (dba != null) {
                dba.close();
            }
        }
        return bRet;
    }
    public boolean UploadNonUploadedEvents() {
        boolean bRet = false;

        DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());   //get db access
        try {
            long lPatEvtId;
            String strFile;

            dba.open();                                                                 //open db
            String strQry = "SELECT PatEvtId, PatEvtFileName FROM vPatEvtCompNoUpload";
            Cursor crs = dba.db.rawQuery(strQry, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate Events
                lPatEvtId = crs.getInt(crs.getColumnIndex("PatEvtId"));         //Get Id
                strFile = crs.getString(crs.getColumnIndex("PatEvtFileName"));  //Get file name
                UploadParticipantEvent(lPatEvtId, strFile);
//                UploadParticipantEventPictures(lPatEvtId);
                //todo enable catchup loading
            }
            crs.close();
            dba.close();
        } catch (NullPointerException e) {
            Log.e("AlwaysService:UploadNonUploadedEvents:NPEx", e.toString());
            //todo handle
        }
        return bRet;
    }

    public void LogMsg(String logMsg) { //log message to "Log_yyyy-MM-dd.txt" (where "y" is year, "M" is month, and "d" is day) in the "Logs" folder
        Context context = getApplicationContext();
        DatabaseAccess dba = DatabaseAccess.getInstance(context);
        String spn = getStudyPatNumber();
        String strDir = APP_DIR_PARTICIPANTS + "/" + spn + APP_DIR_PARTICIPANT_LOG;
        String strFile = spn;
        strFile = strFile + APP_LOG_FILENAME;

        try {
            String path = "";
            try {
                path = getExternalFilesDir(strDir).getPath();
            } catch (NullPointerException ex) {
                Log.e("LogMsg:getPath:NPEx", ex.toString());
            }
            String fileName = strFile;
            File newFile = new File(path, fileName);
            if (!newFile.exists())
            {
                try
                {
                    newFile.createNewFile();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, MODE_APPEND);
            fileOutputStream.write(((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(Calendar.getInstance().getTime())
                    + " " + logMsg + System.lineSeparator()).getBytes());
            fileOutputStream.close();

            fileName = context.getFilesDir().toString() + "/" + fileName;
            try { dba.MoveTo(fileName, newFile.getAbsolutePath(), true); }
            catch (Exception e) {
                Log.e("LogMsg:Ex", e.toString());
                //todo handle
            }

            File fNewFile = new File(context.getExternalFilesDir(strDir), strFile);
            String strNewFile = fNewFile.getAbsolutePath();
            try { dba.MoveTo(fileName, strNewFile, true); }                  //copy created to external files participant dir, keep orig
            catch (Exception e) { Log.e("logMsg:Ex", e.toString()); }   //todo handle
            //get internal files path and file
//            strNewFile = context.getFilesDir().toString() + "/" + APP_DIR_PARTICIPANTS + "/" + spn + "/Events" + "/" + relFileName;
//            try { dba.MoveTo(fileName, strNewFile, false); }                  //copy created to internal files participant dir, no keep orig
//            catch (Exception e) { Log.e("DA:CreateJSON:MoveTo:Ex", e.toString()); }   //todo handle

        }
        catch (Exception e) {
            Log.e("LogMsg:Ex", e.toString());
            //todo handle
        }
    }

    public void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
    public void playAlertSound(boolean bWarn) {
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        MediaPlayer thePlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        stopPlaying();

        try {
            if (bWarn) {
                Uri uriWarn = null;
                RingtoneManager ringtoneMgr = new RingtoneManager(this);
                ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
                Cursor alarmsCursor = ringtoneMgr.getCursor();
                int alarmsCount = alarmsCursor.getCount();
                Uri[] alarms = new Uri[alarmsCount];
                if (alarmsCount != 0 || alarmsCursor.moveToFirst()) {
                    while(!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
                        int currentPosition = alarmsCursor.getPosition();
                        alarms[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
                        if (currentPosition == 12) {
                            uriWarn = alarms[currentPosition];
                        }
                    }
                    alarmsCursor.close();
                }
                if (uriWarn != null) {
                    mPlayer = MediaPlayer.create(getApplicationContext(), uriWarn);
                }
            } else {
                RingtoneManager notifyMgr = new RingtoneManager(this);
                notifyMgr.setType(TYPE_NOTIFICATION);
                Cursor notifyCursor = notifyMgr.getCursor();
                int notifyCount = notifyCursor.getCount();
                Uri[] notifications = new Uri[notifyCount];
                if (notifyCount != 0 || notifyCursor.moveToFirst()) {
                    while(!notifyCursor.isAfterLast() && notifyCursor.moveToNext()) {
                        int currentPosition = notifyCursor.getPosition();
                        notifications[currentPosition] = notifyMgr.getRingtoneUri(currentPosition);
//                Log.i(notifications[currentPosition].);
                    }
                    notifyCursor.close();
                }
//            Uri uri = notifyMgr.getRingtoneUri(12);
                mPlayer = MediaPlayer.create(getApplicationContext(), RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), TYPE_NOTIFICATION));
            }
            mPlayer.start();

        } catch (Exception e) {
            Log.e("Login:playAlertSound", e.toString());
        }
    }

    //    public String TrySendJSONToServer(String filePath) {        //returns null if unsuccessful
    public String TrySendJSONToServer(String strURL, String strPatEvtId, String filePath, String fileName) {        //returns null if unsuccessful
        Context context = getApplicationContext();
        DatabaseAccess dba = DatabaseAccess.getInstance(context);

        String response = "";
        AsyncThread asyncThread = new AsyncThread(dba);
        asyncThread.delegate = this;
        AsyncTask<String, Void, String> taskSendJSON = asyncThread.execute(strURL, strPatEvtId, filePath, fileName);
        return response; //edit: get and return response synchronously
    }

    public String TrySendPictureToServer(String strURL, String strPatEvtActId, String filePath, String fileName) {  //returns null if unsuccessful
        Context context = getApplicationContext();
        DatabaseAccess dba = DatabaseAccess.getInstance(context);

        String response = "";
        AsyncPicThread asyncThread = new AsyncPicThread(dba);
        asyncThread.delegate = this;
        AsyncTask<String, Void, String> taskSendPic = asyncThread.execute(strURL, strPatEvtActId, filePath, fileName);
        return response; //edit: get and return response synchronously
    }

    @Override
    public void processFinish(String strOut){
        //
        LogMsg(strOut);
    }
}

