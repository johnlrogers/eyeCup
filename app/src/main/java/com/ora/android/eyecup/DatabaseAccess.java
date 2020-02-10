package com.ora.android.eyecup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ora.android.eyecup.json.ActivityResponse;
import com.ora.android.eyecup.json.EventActivity;
import com.ora.android.eyecup.json.ParticipantEvent;
import com.ora.android.eyecup.json.PatEventPicture;
import com.ora.android.eyecup.json.PatEventResponse;
import com.ora.android.eyecup.json.ProtocolRevEvent;
import com.ora.android.eyecup.json.ProtocolRevision;
import com.ora.android.eyecup.oradb.TParticipantEvent;
import com.ora.android.eyecup.oradb.TParticipantEventActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANTS;
import static com.ora.android.eyecup.Globals.APP_DIR_PARTICIPANT_EVENTS;
import static com.ora.android.eyecup.Globals.DT_FMT_FULL_ACTIVITY;
import static com.ora.android.eyecup.Globals.DT_FMT_FULL_FILENAME;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    public SQLiteDatabase db;
    private static DatabaseAccess instance;
    private Cursor c = null;
    private DatabaseAccess(Context context) {
        openHelper = new DatabaseOpenHelper(context);
    }
    public static DatabaseAccess getInstance(Context context) { //call this from an activity to initialize a new DatabaseAccess instance or get the existing one, like this: "DatabaseAccess dba = DatabaseAccess.getInstance(getApplicationContext());"
        if (context == null)
            return null; //default for method misuse
        if (instance == null)
            instance = new DatabaseAccess(context);
        return instance;
    }
    public void open() { db = openHelper.getWritableDatabase(); } //call before calling (a) method(s) to access the database each time
    public void close() { //call after calling (a) method(s) to access the database each time
        if (db != null)
            db.close();
    }
    private void MoveTo(String origAbsPathFile, String newAbsPathFile, boolean keepOrig) throws IOException {
        if (origAbsPathFile == null || newAbsPathFile == null)
            return; //returned for method misuse
        try (InputStream in = new FileInputStream(origAbsPathFile)) {
            try (OutputStream out = new FileOutputStream(newAbsPathFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            }
        }
        if (!keepOrig)
            new File(origAbsPathFile).delete();
    }
    private String FormatFileName(String appRelPathFile, String ext, DateFormat dFormat) { //"appRelPathFile" is the path (with forward slashes, if applicable) including file name relative to this app's data folder in the device itself, "ext" is the extension starting with "."; pass "null" for "dFormat" to append no timestamp
        if (appRelPathFile == null || ext == null)
            return null; //default for method misuse
        String fileName = db.getPath();
        fileName = fileName.substring(0, fileName.lastIndexOf("/"));
        fileName = fileName.substring(0, fileName.lastIndexOf("/") + 1) + appRelPathFile;
        if (dFormat != null)
            fileName += "_" + dFormat.format(Calendar.getInstance().getTime());
        return fileName + ext;
    }
    private JSONObject CursorGetInnerJSON(JSONObject resultObj, String innerObjectName) { //cursor must be open when this method is run
        JSONArray innerArray = new JSONArray();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            JSONObject innerObject = new JSONObject();
            try {
                for (int i = 0; i < c.getColumnCount(); i++)
                    innerObject.put(c.getColumnName(i), CursorGetObj(i));
            }
            catch (Exception e) {
                Log.e("DA:CursorGetInnerJSON:innerObject.put:Ex", e.toString());
                //todo handle
            }
            innerArray.put(innerObject);
            c.moveToNext();
        }
        try {
            resultObj.put(innerObjectName, innerArray);
        }
        catch (Exception e) {
            Log.e("DA:CursorGetInnerJSON:resultObj.put:Ex", e.toString());
            //todo handle
        }
        return resultObj;
    }
    private Object CursorGetObj(int colIndex) { //cursor must be open when this method is run
        Object objJSON = null; //default for method misuse
        if (c.getColumnCount() <= colIndex)
            return objJSON;
        if (c.getType(colIndex) == Cursor.FIELD_TYPE_STRING)
            objJSON = c.getString(colIndex);
        else if (c.getType(colIndex) == Cursor.FIELD_TYPE_INTEGER)
            objJSON = c.getInt(colIndex);
        if (objJSON == null)
            objJSON = "";
        return objJSON;
    }
    //call the "open" method before calling these methods below, and call the "close" method after each method call
    public boolean SetParticipantInfo(boolean newPat, String patNum, String studyPatNum, String password) { //returns "true" if successful
        boolean success = false;
        String sqlCmd;
        if (!newPat)
            sqlCmd = "UPDATE tParticipant SET PatNumber = " + patNum + ", StudyPatNumber = " + studyPatNum + ", Password = "
                    + password + ";";
        else
            sqlCmd = "INSERT INTO tParticipant (PatNumber, StudyPatNumber, Password) VALUES (" + patNum + ", " + studyPatNum + ", "
                    + password + ");";
        try {
            db.execSQL(sqlCmd);
            success = true;
        }
        catch (Exception e) {
            Log.e("DA:SetParticipantInfo:Ex", e.toString());
            //todo handle
        }
        return success;
    }

    public String GetStudyPatNumber() {
        String studyPatNumber;
        try { c = db.rawQuery("SELECT StudyPatNumber FROM tParticipant LIMIT 1;", new String[] { }); }
        catch (Exception e) {
            Log.e("DA:GetParticipantInfo:Ex", e.toString());
            //todo handle
        }
        c.moveToFirst();
        studyPatNumber = c.getString(0);
        c.close();
        return studyPatNumber;
    }

    public Object[][] GetParticipantInfo() { //returns null if no participant exists
        try { c = db.rawQuery("SELECT * FROM tParticipant LIMIT 1;", new String[] { }); }
        catch (Exception e) {
            Log.e("DA:GetParticipantInfo:Ex", e.toString());
            //todo handle
        }
        Object[][] selectedVals = new Object[2][c.getCount()];
        //todo Warning:(133, 13) Condition 'selectedVals.length == 0' is always 'false'
        if (selectedVals[0].length == 0)
            return null; //no participant exists
        c.moveToFirst();
        for (int i = 0; i < c.getColumnCount(); i++) {
            selectedVals[0][i] = c.getColumnName(i);
            selectedVals[1][i] = CursorGetObj(i);
        }
        c.close();
        return selectedVals;
    }

    public List<Object[]> GetTableData(String tblName) { //first row is column names
        try { c = db.rawQuery("SELECT * FROM " + tblName + ";", new String[] { }); }
        catch (Exception e) {
            Log.e("DA:GetTableData:Ex", e.toString());
            //todo handle
        }
        List<Object[]> selectedVals = new ArrayList<>();
        selectedVals.add(c.getColumnNames());
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Object[] vals = new Object[c.getColumnCount()];
            for (int i = 0; i < c.getColumnCount(); i++)
                vals[i] = CursorGetObj(i);
            selectedVals.add(vals);
            c.moveToNext();
        }
        c.close();
        return selectedVals;
    }

    public Object[] CallView(String selectCol, String whereCol, String whereVal, boolean prot) { //use to get specific column data from one of the 2 main database views; put apostrophes around "whereVal" if its type is database text //edit: adjust (including views (set)) as much as needed based on future needs
        String view;
        if (!prot)
            view = "vPatEvtActivity";
        else
            view = "vProtRevEvtActivities";
        try { c = db.rawQuery("SELECT " + selectCol + " FROM " + view + " WHERE " + whereCol + " = " + whereVal + ";",
                new String[] { }); }
        catch (Exception e) {
            Log.e("DA:CallView:Ex", e.toString());
            //todo handle
        }
        Object[] selectedVals = new Object[c.getCount()];
        int count = 0;
        int colIndex = c.getColumnIndex(selectCol);
        while (c.moveToNext()) {
            selectedVals[count] = CursorGetObj(colIndex);
            count++;
        }
        c.close();
        return selectedVals;
    }
    public String BackupDB() { //save copy of the SQLite database on the device to the returned internal storage path
        String fileName = FormatFileName("files/Archives/ORADb_Backup", ".db",
                new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss"));
        try { MoveTo(db.getPath(), fileName, true); }
        catch (Exception e) {
            Log.e("DA:BackupDb:Ex", e.toString());
            //todo handle
        }
        return fileName;
    }
    private void ClearTable(String name) {
        if (name == null)
            return; //returned for method misuse
        try { db.execSQL("DELETE FROM " + name + ";"); }
        catch (Exception e) {
            Log.e("DA:clearTable:Ex", e.toString());
            //todo handle
        }
    }
    public void InsertParticipantEvent(ParticipantEvent event) {
        if (event == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tParticipantEvent VALUES (PatEvtId, PatId, DeviceId, ProtRevEvtId, PatEvtDtStart," +
                    " PatEvtResponseCnt, PatEvtPictureCnt) VALUES (" + event.getPatEventId().toString() + ", " +
                    event.getParticipantId() + ", " + event.getDeviceId() + ", " + event.getProtocolRevEventId().toString() + "', " +
                    event.getPatEventDtStart() + "', " + event.getPatEventResponseCnt().toString() + ", " +
                    event.getPatEventPictureCnt().toString() + ";");
        }
        catch (Exception e) {
            Log.e("DA:InsertParticipantEvent:Ex", e.toString());
            //todo handle
        }
    }

    public long InsertTParticipantEvent(TParticipantEvent patEvt) {
        long lNextId = 0;
        if (patEvt == null)
            return lNextId; //returned for method misuse

        try {
            String strSQL = "INSERT INTO tParticipantEvent (PatId, DeviceId, ProtRevEvtId, PatEvtDtStart, PatEvtDtEnd, PatEvtDtUpload) VALUES (";
            strSQL = strSQL + patEvt.getPatId() + ", ";
            strSQL = strSQL + patEvt.getDeviceId() + ", ";
            strSQL = strSQL + patEvt.getProtrevevtid() + ", ";
            strSQL = strSQL + "'" + patEvt.getPatEvtDtStart() + "', ";
            strSQL = strSQL + "'" + patEvt.getPatEvtDtEnd() + "', ";
            strSQL = strSQL + "'" + patEvt.getPatEvtDtUpload() + "')";

            db.execSQL(strSQL);

            strSQL = "SELECT MAX(PatEvtId) as MaxId FROM tParticipantEvent";
            Cursor crs = db.rawQuery(strSQL, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate cursor
                lNextId = crs.getLong(crs.getColumnIndex("MaxId"));     //todo current or next event
            }
            crs.close();
        }
        catch (Exception e) {
            Log.e("DA:InsertTParticipantEvent:Ex", e.toString());
            //todo handle
            return lNextId;
        }
        return lNextId; //returned for method misuse
    }

    public boolean UpdateTParticipantEventDtEnd(long patEvtId) {
        String colName = "PatEvtDtEnd";
        Globals glob = new Globals();
        String strDt = glob.GetDateStr(DT_FMT_FULL_ACTIVITY, glob.getDate());               //get datetime now
        try { db.execSQL("UPDATE tParticipantEvent SET " + colName + " = '" + strDt + "' WHERE PatEvtId = " + patEvtId + ";"); }
        catch (Exception e) {
            Log.e("DA:UpdateTParticipantEventDtEnd:Ex", e.toString());
            //todo handle
            return false;
        }
        return true;
    }

    public boolean UpdateTParticipantEventDtUpload(long patEvtId) {
        String colName = "PatEvtDtUpload";
        Globals glob = new Globals();
        String strDt = glob.GetDateStr(DT_FMT_FULL_ACTIVITY, glob.getDate());               //get datetime now
        try { db.execSQL("UPDATE tParticipantEvent SET " + colName + " = '" + strDt + "' WHERE PatEvtId = " + patEvtId + ";"); }
        catch (Exception e) {
            Log.e("DA:UpdateTParticipantEventDtUpload:Ex", e.toString());
            //todo handle
            return false;
        }
        return true;
    }

    public boolean UpdateTParticipantEventActivityDtUpload(long patEvtActId) {
        String colName = "PatEvtActDtUpload";
        Globals glob = new Globals();
        String strDt = glob.GetDateStr(DT_FMT_FULL_ACTIVITY, glob.getDate());               //get datetime now
        try { db.execSQL("UPDATE tParticipantEventActivity SET " + colName + " = '" + strDt + "' WHERE PatEvtActId = " + patEvtActId + ";"); }
        catch (Exception e) {
            Log.e("DA:UpdateTParticipantEventActivityDtUpload:Ex", e.toString());
            //todo handle
            return false;
        }
        return true;
    }

    public boolean UpdateTParticipantEventFileName(long patEvtId, String strFilename) {
        String colName = "PatEvtFileName";
        try { db.execSQL("UPDATE tParticipantEvent SET " + colName + " = '" + strFilename + "' WHERE PatEvtId = " + patEvtId + ";"); }
        catch (Exception e) {
            Log.e("DA:UpdateTParticipantEventFileName:Ex", e.toString());
            //todo handle
            return false;
        }
        return true;
    }

    public void UpdateParticipantEventChildCnt(long patEvtId, boolean isResponse, int count) { //edit: call when child count is updated //set "isResponse" to "false" for picture count
        String colName = "PatEvtResponseCnt";
        if (!isResponse)
            colName = "PatEvtPictureCnt";
        try { db.execSQL("UPDATE tParticipantEvent SET " + colName + " = " + count + " WHERE PatEvtId = " + patEvtId + ";"); }
        catch (Exception e) {
            Log.e("DA:UpdateParticipantEventChildCnt:Ex", e.toString());
            //todo handle
        }
    }
    public void InsertParticipantResponse(long patEvtId, PatEventResponse response) {
        if (response == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tParticipantEventActivity (PatEvtId, ProtRevEvtActId, ResponseVal, ResponseTxt, PatEvtActDt) VALUES "
                    + "(" + patEvtId + ", " + response.getProtocolRevEventActivityId().toString() + ", " +
                    response.getResponseVal().toString() + ", '" + response.getResponseTxt() + "', '" + response.getResponseDt() +
                    "');");
        }
        catch (Exception e) {
            Log.e("DA:InsertParticipantResponse:Ex", e.toString());
            //todo handle
        }
    }
    public void InsertParticipantPicture(long patEvtId, PatEventPicture picture) { //the "PictureFileName" property of "picture" must be set to the full file path, including name (and extension)
        if (picture == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tParticipantEventActivity (PatEvtId, ProtRevEvtActId, ResponsePath, PatEvtActDt) VALUES ("
                    + patEvtId + ", " + picture.getProtocolRevEventActivityId().toString() + ", '" + picture.getPictureFileName() +
                    "', '" + picture.getPictureDt() + "');");
        }
        catch (Exception e) {
            Log.e("DA:InsertParticipantPicture:Ex", e.toString());
            //todo handle
        }
    }

    public boolean MarkParticipantEventEnded(long patEvtId, String date) { //edit: call when ended
        boolean bRet = false;
        try {
            db.execSQL("UPDATE tParticipantEvent SET PatEvtDtEnd = '" + date + "' WHERE PatEvtId = " + patEvtId + ";");
            bRet = true;
        }
        catch (Exception e) {
            Log.e("DA:MarkParticipantEventEnded:Ex", e.toString());
            //todo handle
        }
        return bRet;
    }

//    public String TrySendJSONToServer(String filePath) {        //returns null if unsuccessful
    public String TrySendJSONToServer(String strURL, String strPatEvtId, String filePath, String fileName) {        //returns null if unsuccessful

    String response = "";
//        AsyncThread asyncThread = new AsyncThread();
        AsyncThread asyncThread = new AsyncThread(instance);
    AsyncTask<String, Void, String> taskSendJSON = asyncThread.execute(strURL, strPatEvtId, filePath, fileName);
    return response; //edit: get and return response synchronously
}

    public String TrySendPictureToServer(String strURL, String strPatEvtActId, String filePath, String fileName) {  //returns null if unsuccessful

        String response = "";
//        AsyncPicThread asyncThread = new AsyncPicThread();
        AsyncPicThread asyncThread = new AsyncPicThread(instance);
        AsyncTask<String, Void, String> taskSendPic = asyncThread.execute(strURL, strPatEvtActId, filePath, fileName);
        return response; //edit: get and return response synchronously
    }

    public void MarkParticipantEventUploaded(ParticipantEvent newEvent) { //edit: call when uploaded
        if (newEvent == null)
            return; //returned for method misuse
        try { db.execSQL("UPDATE tParticipantEvent WHERE PatEvtId = " + newEvent.getPatEventId().toString() + " SET PatEvtDtUpload = '" +
                newEvent.getPatEventDtUpload() + "';"); }
        catch (Exception e) {
            Log.e("DA:InsertParticipantEventUploaded:Ex", e.toString());
            //todo handle
        }
    }

    private void InsertProtocolRevision(ProtocolRevision entity) {
        if (entity == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tProtRev (ProtRevId, ProtName, ProtRevName, ProtRevDt) VALUES (" +
                    entity.getProtocolRevId().toString() + ", '" + entity.getProtocolName() + "', '" + entity.getProtocolRevName() +
                    "', '" + entity.getProtocolRevDt() + "');");
        }
        catch (Exception e) {
            Log.e("DA:InsertProtocolRevision:Ex", e.toString());
            //todo handle
        }
    }

    private void InsertProtocolRevEvent(ProtocolRevEvent entity) {
        if (entity == null)
            return; //returned for method misuse
        try {
//            db.execSQL("INSERT INTO tProtRevEvent (ProtRevEvtId, ProtRevId, ProtRevEvtName, EvtFreq, EvtStart, EvtTimeOpen, " +
//                    "EvtTimeWarn, EvtTimeClose) VALUES (" + entity.getProtocolRevEventId().toString() + ", " +
//                    entity.getProtocolRevId().toString() + ", '" + entity.getProtocolRevEventName() + "', '" + entity.getFrequencyCode()
//                    + "', " + entity.getEventDayStart().toString() + ", '" + entity.getEventTimeOpen() + "', '" +
//                    entity.getEventTimeWarn() + "', '" + entity.getEventTimeClose() + "');");
            db.execSQL("INSERT INTO tProtRevEvent (ProtRevEvtId, ProtRevId, ProtRevEvtName, EvtFreq, EvtStart, EvtDaysDuration, EvtTimeOpen, " +
                    "EvtTimeWarn, EvtTimeClose) VALUES (" + entity.getProtocolRevEventId().toString() + ", " +
                    entity.getProtocolRevId().toString() + ", '" + entity.getProtocolRevEventName() + "', '" + entity.getFrequencyCode()
                    + "', " + entity.getEventDayStart().toString() + "', " + entity.getEventDaysDuration().toString() + ", '" + entity.getEventTimeOpen() + "', '" +
                    entity.getEventTimeWarn() + "', '" + entity.getEventTimeClose() + "');");
        }
        catch (Exception e) {
            Log.e("DA:InsertProtocolRevEvent:Ex", e.toString());
            //todo handle
        }
    }
    private void InsertEventActivity(EventActivity entity) {
        if (entity == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tProtRevEventActivity (ProtRevEvtActId, ProtRevEvtId, ActSeq, ActId, ProtRevEvtApplyTo, MinRange," +
                    " MaxRange) VALUES (" + entity.getProtocolRevEventActivityId().toString() + ", " +
                    entity.getProtocolRevEventId().toString() + ", " + entity.getActivitySeq().toString() + ", " +
                    entity.getActivityId().toString() + ", '" + entity.getProtRevEvtApplyTo() + "', " + entity.getMinRange().toString()
                    + ", " + entity.getMaxRange().toString() + ");");
            db.execSQL("INSERT INTO tActivity (ActId, ActTypeId, RspTypeId, ActText, ActPictureCode) VALUES (" +
                    entity.getActivityId().toString() + ", " + entity.getActivityTypeId().toString() + ", " +
                    entity.getActivityResponseTypeId().toString() + ", '" + entity.getActivityText() + "', '" +
                    entity.getActivityPictureCode() + "');");
        }
        catch (Exception e) {
            Log.e("DA:InsertEventActivity:Ex", e.toString());
            //todo handle
        }
    }
    private void InsertActivityResponse(ActivityResponse entity, EventActivity parent) {
        if (entity == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tProtRevEventActivity (ProtRevEvtActId, ProtRevEvtId, ActSeq, ActId, ProtRevEvtApplyTo) VALUES" +
                    "(" + entity.getProtocolRevEventActivityId().toString() + ", " + parent.getProtocolRevEventId().toString() + ", " +
                    parent.getActivitySeq().toString() + ", " + entity.getActId().toString() + ", '" + parent.getProtRevEvtApplyTo() +
                    "');");
            db.execSQL("INSERT INTO tActivityResponseAvail (ActRspId, ActId, ActRspSeq, ActRspVal) VALUES (" +
                    entity.getActRspId().toString() + ", " + entity.getActId().toString() + ", " + entity.getActRspSeq().toString() +
                    ", " + entity.getActRspValue().toString() + ");");
        }
        catch (Exception e) {
            Log.e("DA:InsertActivityResponse:Ex", e.toString());
            //todo handle
        }
    }
    public void JSONProtocolRevision(String jsonFilePath) throws IOException { //edit: call whenever a new JSON protocol revision file is put in path "jsonFilePath"
        if (jsonFilePath == null)
            return; //returned for method misuse
        JsonParser parser = new JsonParser();
        JsonObject jsonObj = (JsonObject)parser.parse(new FileReader(jsonFilePath));
        ProtocolRevision revision = new ProtocolRevision(jsonObj.get("ProtocolRevId").getAsLong(),
                jsonObj.get("ProtocolName").getAsString(), jsonObj.get("ProtocolRevName").getAsString(),
                jsonObj.get("ProtocolRevDt").getAsString(), jsonObj.get("ProtocolRevEventCnt").getAsLong(),
                ToPREEntityList(ValidateJsonArray(jsonObj, "ProtocolRevEvents")));
        BackupDB();
        //edit: clear participant tables' data
        ClearTable("tProtRevEventActivity");
        ClearTable("tProtRevEvent");
        ClearTable("tProtRev");
        ClearTable("tActivityResponseAvail");
        ClearTable("tActivity");
        InsertProtocolRevision(revision);
        List<ProtocolRevEvent> protocolRevEvents = revision.getProtocolRevEvents();
        for (int i = 0; i < protocolRevEvents.size(); i++) {
            ProtocolRevEvent pre = protocolRevEvents.get(i);
            InsertProtocolRevEvent(pre);
            List<EventActivity> eventActivities = pre.getEventActivities();
            for (int j = 0; j < eventActivities.size(); j++) {
                EventActivity ea = eventActivities.get(j);
                InsertEventActivity(ea);
                List<ActivityResponse> activityResponses = ea.getActivityResponses();
                for (int k = 0; k < activityResponses.size(); k++) {
                    ActivityResponse ar = activityResponses.get(i);
                    InsertActivityResponse(ar, ea);
                }
            }
        }
    }

    private List<ProtocolRevEvent> ToPREEntityList(JsonArray protocolRevEvents) {
        if (protocolRevEvents == null)
            return null; //default for method misuse
        List<ProtocolRevEvent> entityList = new ArrayList<>();
        for (int i = 0; i < protocolRevEvents.size(); i++) {
            JsonObject pre = protocolRevEvents.get(i).getAsJsonObject();
            entityList.add(new ProtocolRevEvent(pre.get("ProtocolRevEventId").getAsLong(),
                    pre.get("ProtocolRevId").getAsLong(), OptionalOrDefault(pre, "ProtocolRevEventName", ""),
                    pre.get("FrequencyCode").getAsString(), pre.get("EventDayStart").getAsLong(),
                    pre.get("EventDaysDuration").getAsLong(),
                    pre.get("EventTimeOpen").getAsString(), pre.get("EventTimeWarn").getAsString(),
                    //todo Warning:(344, 105) Use `Long.valueOf(0)` instead
                    pre.get("EventTimeClose").getAsString(), OptionalOrDefault(pre, "EventActivityCnt", 0L),
                    ToEAEntityList(OptionalOrDefault(pre, "EventActivities", new JsonArray()))));
        }
        return entityList;
    }
    private List<EventActivity> ToEAEntityList(JsonArray eventActivities) {
        if (eventActivities == null)
            return null; //default for method misuse
        List<EventActivity> entityList = new ArrayList<>();
        for (int i = 0; i < eventActivities.size(); i++) {
            JsonObject ea = eventActivities.get(i).getAsJsonObject();
            entityList.add(new EventActivity(ea.get("ProtocolRevEventActivityId").getAsLong(),
                    ea.get("ProtocolRevEventId").getAsLong(), ea.get("ActivitySeq").getAsLong(), ea.get("ActivityId").getAsLong(),
                    ea.get("ProtRevEvtApplyTo").getAsString(), ea.get("ActivityTypeId").getAsLong(),
                    ea.get("ActivityTypeCode").getAsString(), OptionalOrDefault(ea, "ActivityText", ""),
                    ea.get("ActivityResponseTypeId").getAsLong(), ea.get("ActivityResponseTypeCode").getAsString(),
                    ea.get("ActivityResponseCnt").getAsLong(), ToAREntityList(OptionalOrDefault(ea, "ActivityResponses",
                    new JsonArray())), OptionalOrDefault(ea, "MinRange", 0L),
                    OptionalOrDefault(ea, "MaxRange", 0L), OptionalOrDefault(ea,
                    "ActivityPictureCode", "")));
        }
        return entityList;
    }
    private List<ActivityResponse> ToAREntityList(JsonArray activityResponse) {
        if (activityResponse == null)
            return null; //default for method misuse
        List<ActivityResponse> entityList = new ArrayList<>();
        for (int i = 0; i < activityResponse.size(); i++) {
            JsonObject ar = activityResponse.get(i).getAsJsonObject();
            entityList.add(new ActivityResponse(ar.get("ProtocolRevEventActivityId").getAsLong(), ar.get("ActRspId").getAsLong(),
                    ar.get("ActId").getAsLong(), ar.get("ActRspSeq").getAsLong(), ar.get("ActRspVal").getAsLong(),
                    ar.get("ActRspTxt").getAsString()));
        }
        return entityList;
    }
    private JsonArray ValidateJsonArray(JsonObject parent, String member) {
        if (parent == null || member == null)
            return null; //default for method misuse
        JsonElement child = parent.get(member);
        if (child.getClass().equals(JsonArray.class))
            return child.getAsJsonArray();
        return new JsonArray();
    }
    private <T extends Object> T OptionalOrDefault(JsonObject object, String optionalProp, T defVal) { //"defVal" should be a "Long", a "String", or a "JsonArray"
        if (object == null || optionalProp == null || defVal == null)
            return null; //default for method misuse
        JsonElement element = object.get(optionalProp);
        T value = defVal;
        if (object.has(optionalProp)) {
            if (defVal instanceof Long)
                value = (T)(Object)element.getAsLong();
            else if (defVal instanceof String)
                value = (T)element.getAsString();
            else
                value = (T)ValidateJsonArray(object, optionalProp);
        }
        return value;
    }

    public String CreateJSON(String outerObjectDbView,
                             String innerObjectsName,
                             String innerObjectsDbView,
                             String innerImagesName, //create applicable views in the DB browser for this method to call, which if they exist, will format the JSON file based on the content of that view (and otherwise save nothing to the file) and save it to the returned internal storage path
                             String innerImagesDbView,
                             long lPatEvtId,
                             Context ctx) {

        JSONObject resultObj = new JSONObject();
        String fileName = ""; //default for method misuse
        String strSQL;

        if (outerObjectDbView == null || innerObjectsName == null || innerObjectsDbView == null || innerImagesName == null
                || innerImagesDbView == null || ctx == null)
            return fileName;

//        try { c = db.rawQuery("SELECT * FROM " + outerObjectDbView + " LIMIT 1;", null); }
        strSQL = "SELECT * FROM " + outerObjectDbView;
        strSQL = strSQL + " WHERE PatEventId = " + lPatEvtId;
        strSQL = strSQL + " ORDER BY PatEventId;";
        try { c = db.rawQuery(strSQL, null); }
        catch (Exception e) {
            Log.e("DA:CreateJSON.outerObjectDbView:Ex", e.toString());
            //todo handle
            return fileName;
        }
        c.moveToFirst();
        for (int i = 0; i < c.getColumnCount(); i++) {                  //get all columns
            try { resultObj.put(c.getColumnName(i), CursorGetObj(i)); }
            catch (Exception e) {
                Log.e("DA:CreateJSON.put:IOEx", e.toString());
                //todo handle
            }
        }
        c.close();

//        try { c = db.rawQuery("SELECT * FROM " + innerObjectsDbView + ";", null); }
        strSQL = "SELECT * FROM " + innerObjectsDbView;
        strSQL = strSQL + " WHERE PatEvtId = " + lPatEvtId;
        strSQL = strSQL + " ORDER BY PatEvtId, ProtocolRevEventActivityId;";
        try { c = db.rawQuery(strSQL, null); }
        catch (Exception e) {
            Log.e("DA:CreateJSON.innerObjectsDbView:IOEx", e.toString());
            //todo handle
            return fileName;
        }
        //todo Warning:(459, 9) Variable is already assigned to this value
        resultObj = CursorGetInnerJSON(resultObj, innerObjectsName);
        c.close();

//        try { c = db.rawQuery("SELECT * FROM " + innerImagesDbView + ";", null); }
        strSQL = "SELECT * FROM " + innerImagesDbView;
        strSQL = strSQL + " WHERE PatEvtId = " + lPatEvtId;
        strSQL = strSQL + " ORDER BY PatEvtId, ProtocolRevEventActivityId;";
        try { c = db.rawQuery(strSQL, null); }
        catch (Exception e) {
            Log.e("DA:CreateJSON.innerImagesDbView:IOEx", e.toString());
            //todo handle
            return fileName;
        }
        //todo Warning:(463, 9) Variable is already assigned to this value
        resultObj = CursorGetInnerJSON(resultObj, innerImagesName);
        c.close();

        Globals glob = new Globals();
        Date dt = Calendar.getInstance().getTime();
        String strDt = glob.GetDateStr(DT_FMT_FULL_FILENAME,dt);

        String strDir = APP_DIR_PARTICIPANTS + "/" + GetStudyPatNumber() + APP_DIR_PARTICIPANT_EVENTS;
        String strFile = GetStudyPatNumber();
        strFile = strFile + "_" + strDt;
        strFile = strFile + ".json";
        String relFileName = strFile;

        File fNewFile = new File(ctx.getExternalFilesDir(strDir), strFile);
        String strNewFile = fNewFile.getAbsolutePath();
//        File fNewFile2 = new File(ctx.getFilesDir(), strFile);
//        String strNewFile2 = fNewFile2.getAbsolutePath();

//        String relFileName = GetStudyPatNumber();       //output JSON file name (timestamp will be appended automatically)
//        String relFileName = GetStudyPatNumber() + "_" + strDt + ".json";       //output JSON file name
//        String relFileName = GetStudyPatNumber() + "_" + strDt + ".json";       //output JSON file name
        try {
//            FileOutputStream fileOutputStream = ctx.openFileOutput(relFileName + ".json", MODE_PRIVATE);
            FileOutputStream fileOutputStream = ctx.openFileOutput(relFileName , MODE_PRIVATE);
            fileOutputStream.write(resultObj.toString(4).getBytes());
            fileOutputStream.close();
        }
        catch (Exception e) {
            Log.e("DA:CreateJSON:fileOutputStream:Ex", e.toString());
            //todo handle
        }

        String spn = GetStudyPatNumber();
        File patsFolder = new File(FormatFileName(APP_DIR_PARTICIPANTS, "", null));
        if (!patsFolder.isDirectory())
            patsFolder.mkdir();
        File patFolder = new File(FormatFileName(APP_DIR_PARTICIPANTS + "/" + spn, "", null));
        if (!patFolder.isDirectory())
            patFolder.mkdir();
        File evtsFolder = new File(FormatFileName(APP_DIR_PARTICIPANTS + "/" + spn + "/Events", "", null));
        if (!evtsFolder.isDirectory())
            evtsFolder.mkdir();

        relFileName = ctx.getFilesDir().toString() + "/" + relFileName;
//        try { MoveTo("files/" + relFileName, strNewFile, true); }
        try { MoveTo(relFileName, strNewFile, true); }
        catch (Exception e) {
            Log.e("DA:CreateJSON:MoveTo:Ex", e.toString());
            //todo handle
        }
//        try { MoveTo("files/" + relFileName, strNewFile2, true); }
//        try { MoveTo(relFileName, strNewFile2, true); }
//        catch (Exception e) {
//            Log.e("DA:CreateJSON:MoveTo:Ex", e.toString());
//            //todo handle
//        }

        return strNewFile;
    }

    public void SavePatEvtActivityResponse(TParticipantEventActivity actResp) {
        if (actResp == null)
            return; //returned for method misuse

        try {
            Globals glob = new Globals();
            String strDt = glob.GetDateStr(DT_FMT_FULL_ACTIVITY, glob.getDate());               //get datetime now

            String strSQL = "INSERT INTO tParticipantEventActivity (PatEvtId, ProtRevEvtActId, ResponseVal, ResponseTxt, ResponsePath, PatEvtActDt) VALUES (";
            strSQL = strSQL + actResp.getPatevtid() + ", ";
            strSQL = strSQL + actResp.getProtrevevtactid() + ", ";
            strSQL = strSQL + actResp.getResponseVal() + ", ";
            strSQL = strSQL + "'" + actResp.getResponseTxt() + "', ";
            strSQL = strSQL + "'" + actResp.getResponsePath() + "', ";
            strSQL = strSQL + "'" + strDt + "');";

            db.execSQL(strSQL);

            String strActTypeCode = "";
            strSQL = "SELECT ActTypeCode FROM tProtRevEventActivity WHERE ProtRevEvtActId = " + actResp.getProtrevevtactid();
            Cursor crs = db.rawQuery(strSQL, null);                     //get cursor to view
            while (crs.moveToNext()) {                                                  //Iterate Codes
                strActTypeCode = crs.getString(crs.getColumnIndex("ActTypeCode"));      //Get Activity Type Code (only one row)
            }
            crs.close();

            strSQL = "";
            boolean bUpdCnt = false;
            if (strActTypeCode.equals("P")) {
                strSQL = "UPDATE tParticipantEvent SET PatEvtPictureCnt = PatEvtPictureCnt + 1";
                bUpdCnt = true;
            } else if (strActTypeCode.equals("Q")){
                strSQL = "UPDATE tParticipantEvent SET PatEvtResponseCnt = PatEvtResponseCnt + 1";
                bUpdCnt = true;
            }
            if (bUpdCnt) {
                strSQL = strSQL + " WHERE PatEvtId = " + actResp.getPatevtid();
                db.execSQL(strSQL);
            }
        }
        catch (Exception e) {
            Log.e("DA:SavePatEvtActivityResponse:Ex", e.toString());
            //todo handle
        }
    }
}
