package com.ora.android.eyecup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Context.MODE_PRIVATE;
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
            catch (Exception ex) {
                //todo handle
            }
            innerArray.put(innerObject);
            c.moveToNext();
        }
        try { resultObj.put(innerObjectName, innerArray); }
        catch (Exception ex) {
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
        catch (Exception ex) {
            //todo handle
        }
        return success;
    }
    public Object[][] GetParticipantInfo() { //returns null if no participant exists
        try { c = db.rawQuery("SELECT * FROM tParticipant LIMIT 1;", new String[] { }); }
        catch (Exception ex) {
            //todo handle
        }
        Object[][] selectedVals = new Object[2][c.getCount()];
        //todo Warning:(133, 13) Condition 'selectedVals.length == 0' is always 'false'
        if (selectedVals.length == 0)
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
        catch (Exception ex) {
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
        catch (Exception ex) {
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
        catch (Exception ex) {
            //todo handle
        }
        return fileName;
    }
    private void ClearTable(String name) {
        if (name == null)
            return; //returned for method misuse
        try { db.execSQL("DELETE FROM " + name + ";"); }
        catch (Exception ex) {
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
        catch (Exception ex) {
            //todo handle
        }
    }
    public void UpdateParticipantEventChildCnt(long patEvtId, boolean isResponse, int count) { //edit: call when child count is updated //set "isResponse" to "false" for picture count
        String colName = "PatEvtResponseCnt";
        if (!isResponse)
            colName = "PatEvtPictureCnt";
        try { db.execSQL("UPDATE tParticipantEvent SET " + colName + " = " + count + " WHERE PatEvtId = " + patEvtId + ";"); }
        catch (Exception ex) {
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
        catch (Exception ex) {
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
        catch (Exception ex) {
            //todo handle
        }
    }
    public void MarkParticipantEventEnded(long patEvtId, String date) { //edit: call when ended
        try { db.execSQL("UPDATE tParticipantEvent SET PatEvtDtEnd = '" + date + "' WHERE PatEvtId = " + patEvtId + ";"); }
        catch (Exception ex) {
            //todo handle
        }
    }
    public void MarkParticipantEventUploaded(ParticipantEvent newEvent) { //edit: call when uploaded
        if (newEvent == null)
            return; //returned for method misuse
        try { db.execSQL("UPDATE tParticipantEvent WHERE PatEvtId = " + newEvent.getPatEventId().toString() + " SET PatEvtDtUpload = '" +
                newEvent.getPatEventDtUpload() + "';"); }
        catch (Exception ex) {
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
        catch (Exception ex) {
            //todo handle
        }
    }

    //todo add eventDaysDuration
    private void InsertProtocolRevEvent(ProtocolRevEvent entity) {
        if (entity == null)
            return; //returned for method misuse
        try {
            db.execSQL("INSERT INTO tProtRevEvent (ProtRevEvtId, ProtRevId, ProtRevEvtName, EvtFreq, EvtStart, EvtTimeOpen, " +
                    "EvtTimeWarn, EvtTimeClose) VALUES (" + entity.getProtocolRevEventId().toString() + ", " +
                    entity.getProtocolRevId().toString() + ", '" + entity.getProtocolRevEventName() + "', '" + entity.getFrequencyCode()
                    + "', " + entity.getEventDayStart().toString() + ", '" + entity.getEventTimeOpen() + "', '" +
                    entity.getEventTimeWarn() + "', '" + entity.getEventTimeClose() + "');");
        }
        catch (Exception ex) {
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
        catch (Exception ex) {
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
            db.execSQL("INSERT INTO tActivityResponseAvail (ActRspId, ActId, ActRspSeq, ActRspValue) VALUES (" +
                    entity.getActRspId().toString() + ", " + entity.getActId().toString() + ", " + entity.getActRspSeq().toString() +
                    ", " + entity.getActRspValue().toString() + ");");
        }
        catch (Exception ex) {
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

    //todo add eventDaysDuration
    private List<ProtocolRevEvent> ToPREEntityList(JsonArray protocolRevEvents) {
        if (protocolRevEvents == null)
            return null; //default for method misuse
        List<ProtocolRevEvent> entityList = new ArrayList<>();
        for (int i = 0; i < protocolRevEvents.size(); i++) {
            JsonObject pre = protocolRevEvents.get(i).getAsJsonObject();
            entityList.add(new ProtocolRevEvent(pre.get("ProtocolRevEventId").getAsLong(),
                    pre.get("ProtocolRevId").getAsLong(), OptionalOrDefault(pre, "ProtocolRevEventName", ""),
                    pre.get("FrequencyCode").getAsString(), pre.get("EventDayStart").getAsLong(),
                    pre.get("EventTimeOpen").getAsString(), pre.get("EventTimeWarn").getAsString(),
                    //todo Warning:(344, 105) Use `Long.valueOf(0)` instead
                    pre.get("EventTimeClose").getAsString(), OptionalOrDefault(pre, "EventActivityCnt", new Long(0)),
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
                    new JsonArray())), OptionalOrDefault(ea, "MinRange", new Long(0)),
                    OptionalOrDefault(ea, "MaxRange", new Long(0)), OptionalOrDefault(ea,
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
                    ar.get("ActId").getAsLong(), ar.get("ActRspSeq").getAsLong(), ar.get("ActRspValue").getAsLong(),
                    ar.get("ActRspText").getAsString()));
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
    public String CreateJSON(String outerObjectDbView, String innerObjectsName, String innerObjectsDbView, String innerImagesName, //create applicable views in the DB browser for this method to call, which if they exist, will format the JSON file based on the content of that view (and otherwise save nothing to the file) and save it to the returned internal storage path
            String innerImagesDbView, AppCompatActivity caller) {
        String fileName = ""; //default for method misuse
        if (outerObjectDbView == null || innerObjectsName == null || innerObjectsDbView == null || innerImagesName == null
                || innerImagesDbView == null || caller == null)
            return fileName;
        try { c = db.rawQuery("SELECT * FROM " + outerObjectDbView + " LIMIT 1;", null); }
        catch (Exception ex) { return fileName; }
        JSONObject resultObj = new JSONObject();
        c.moveToFirst();
        for (int i = 0; i < c.getColumnCount(); i++) {
            try { resultObj.put(c.getColumnName(i), CursorGetObj(i)); }
            catch (Exception ex) {
                //todo handle
            }
        }
        c.close();
        try { c = db.rawQuery("SELECT * FROM " + innerObjectsDbView + ";", null); }
        catch (Exception ex) { return fileName; }

        //todo Warning:(459, 9) Variable is already assigned to this value
        resultObj = CursorGetInnerJSON(resultObj, innerObjectsName);
        c.close();
        try { c = db.rawQuery("SELECT * FROM " + innerImagesDbView + ";", null); }
        catch (Exception ex) { return fileName; }

        //todo Warning:(463, 9) Variable is already assigned to this value
        resultObj = CursorGetInnerJSON(resultObj, innerImagesName);
        c.close();
        String relFileName = "Output"; //output JSON file name (timestamp will be appended automatically)
        try {
            FileOutputStream fileOutputStream = caller.openFileOutput(relFileName + ".json", MODE_PRIVATE);
            fileOutputStream.write(resultObj.toString(4).getBytes());
            fileOutputStream.close();
        }
        catch (Exception ex) {
            //todo handle
        }
        fileName = FormatFileName("files/Patient_Identifier/Events/" + relFileName, ".json",
                new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss"));
        try { MoveTo(FormatFileName("files/" + relFileName, ".json", null), fileName, false); }
        catch (Exception ex) {
            //todo handle
        }
        return fileName;
    }
}
