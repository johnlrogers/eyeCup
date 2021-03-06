package com.ora.android.eyecup;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AsyncThread extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate=null;
    private long mlPatEvtId = 0;
    private DatabaseAccess dba;
    private int iResponseCode;
    private String strResponseMsg = "";
    private String attachmentFileName;

    AsyncThread(DatabaseAccess dba) { this.dba = dba; }

//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//
//    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i("AsyncThread:onPostExecute", result); // this is expecting a response code to be sent from your server upon receiving the POST data

        String strResult = "Upload " + attachmentFileName + " result: " + result; //get msg for log
        try {
            delegate.processFinish(strResult);      //send to Interface for AlwaysService
        } catch (Exception e) {
            Log.e("AsyncThread:delegate.processFinish", e.toString());
        }

        if (result.equals("200 OK")) {
            try {
                dba.open();                                                 //open db
                dba.UpdateTParticipantEventDtUpload(mlPatEvtId);            //set upload date
                dba.close();                                                //close db
                Log.d("AsyncThread", "UpdateTParticipantEventDtUpload");
            } catch (NullPointerException e) {
                Log.e("AsyncThread:dba.open:NPEx", e.toString());
                //todo handle, try again?
            }
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        int iResponseCode = 0;
        String strResponseMsg = "";
        HttpsURLConnection httpsUrlConnection = null;

        try {
            URL url = new URL(strings[0]);              //url
            mlPatEvtId = Long.valueOf(strings[1]);      //Pat Evt Id
            String attachmentName = strings[2];         //full path file
            attachmentFileName = strings[3];     //actual name
            String strJson;

            //url = new URL("https://icupapi.lionridgedev.com/");
            //url = new URL("https://icupapi.lionridgedev.com/v1/diary/1/12345678/");
            httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            httpsUrlConnection.setUseCaches(false);
            httpsUrlConnection.setDoOutput(true);

            httpsUrlConnection.setRequestMethod("POST");
            httpsUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpsUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpsUrlConnection.setRequestProperty("Content-Type", "application/json");

            try {
                File file = new File(attachmentName);
                FileInputStream is = new FileInputStream(file);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                strJson = new String(buffer, "UTF-8");
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }

            DataOutputStream request = new DataOutputStream(httpsUrlConnection.getOutputStream());
            request.writeBytes(strJson);
            request.flush();            //flush output stream
            request.close();            //close output stream

            iResponseCode = httpsUrlConnection.getResponseCode();                   //reponses
            strResponseMsg = httpsUrlConnection.getResponseMessage();
            Log.i(attachmentFileName + " Upload Status", iResponseCode + " " + strResponseMsg);
        }
        catch (Exception ex) {
            Log.e("AsyncThread:doInBackground:Ex", ex.toString());
            //todo: handle
        } finally {
            if (httpsUrlConnection != null) {
                httpsUrlConnection.disconnect();
            }
        }
        return iResponseCode + " " + strResponseMsg;
    }
}
