package com.ora.android.eyecup;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AsyncPicThread extends AsyncTask<String, Void, String> {

    public AsyncResponse delegate=null;
    private long mlPatEvtActId = 0;
    private DatabaseAccess dba;
    private int iResponseCode;
    private String strResponseMsg = "";
    private String attachmentFileName;

    AsyncPicThread(DatabaseAccess dba) { this.dba = dba; }

//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//
//    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.i("AsyncPicThread:onPostExecute", result); // this is expecting a response code to be sent from your server upon receiving the POST data

        String strResult = "Upload " + attachmentFileName + " result: " + result; //get msg for log
        try {
            delegate.processFinish(strResult);      //send to Interface for AlwaysService
        } catch (Exception e) {
            Log.e("AsyncPicThread:delegate.processFinish", e.toString());
        }

        if (result.equals("200 OK")) {
            try {
                dba.open();                                                 //open db
                dba.UpdateTParticipantEventActivityDtUpload(mlPatEvtActId); //set upload date
                dba.close();                                                //close db
                Log.d("AsyncPicThread", "UpdateTParticipantEventActivityDtUpload");
            } catch (NullPointerException e) {
                Log.e("AsyncPicThread:dba.open:NPEx", e.toString());
                //todo handle, try again?
            }
        }
    }

    @Override
    protected String doInBackground(String... strings) {

        HttpsURLConnection httpsUrlConnection = null;

        // sample https://stackoverflow.com/questions/11766878/sending-files-using-post-with-httpurlconnection
        try {
            URL url = new URL(strings[0]);              //url
            mlPatEvtActId = Long.valueOf(strings[1]);   //Pat Evt Id
            String attachmentName = strings[2];         //full path file
            attachmentFileName = strings[3];     //actual name
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            //url = new URL("https://icupapi.lionridgedev.com/");
            //url = new URL("https://icupapi.lionridgedev.com/v1/photo/1/12345678/");
            httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            httpsUrlConnection.setUseCaches(false);
            httpsUrlConnection.setDoOutput(true);

            httpsUrlConnection.setRequestMethod("POST");
            httpsUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpsUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpsUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);


            DataOutputStream request = new DataOutputStream(httpsUrlConnection.getOutputStream());  //new output stream

            request.writeBytes(twoHyphens + boundary + crlf);                   //send content wrapper
            request.writeBytes("Content-Disposition: form-data; name=\"" +
//                    attachmentName + "\";filename=\""
                    "photo" + "\";filename=\""
                    + attachmentFileName + "\"" + crlf);
            request.writeBytes(crlf);

            FileInputStream is = new FileInputStream(attachmentName);               //send content
            byte[] buff = getBytes(is);
            request.write(buff);

            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);      //send end content wrapper

            request.flush();                                                        //flush output stream
            request.close();                                                        //close output stream

            iResponseCode = httpsUrlConnection.getResponseCode();                   //reponses
            strResponseMsg = httpsUrlConnection.getResponseMessage();
            Log.i(attachmentFileName + " Upload Status", iResponseCode + " " + strResponseMsg);
        }
        catch (Exception ex) {
            Log.e("AsyncPicThread:doInBackground:Ex", ex.toString());
            //todo: handle
        } finally {
            if (httpsUrlConnection != null) {
                httpsUrlConnection.disconnect();
            }
        }
        return strResponseMsg;
    }

    private byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 1024;
        byte[] buff = new byte[buffSize];

        int len;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }
}
