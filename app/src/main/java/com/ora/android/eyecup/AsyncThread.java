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

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.e("AsyncThread:onPostExecute", result); // this is expecting a response code to be sent from your server upon receiving the POST data
    }

    @Override
    protected String doInBackground(String... strings) {

        String data = "";
        HttpsURLConnection httpsUrlConnection = null;

        try {
            String attachmentName = strings[1];
            String attachmentFileName = strings[1].substring(strings[0].lastIndexOf("/") + 1);
            String boundary =  "*****";
            String strJson;

            //URL url = new URL("https://icupapi.lionridgedev.com/");
//            URL url = new URL("https://icupapi.lionridgedev.com/v1/diary/1/12345678/");
            URL url = new URL(strings[0]);
            httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            httpsUrlConnection.setUseCaches(false);
            httpsUrlConnection.setDoOutput(true);

            httpsUrlConnection.setRequestMethod("POST");
            httpsUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpsUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpsUrlConnection.setRequestProperty("Content-Type", "application/json");
//            httpsUrlConnection.setRequestProperty("Content-Type", "json/text");

//            httpsUrlConnection.setDoOutput(true);

            try {
//                InputStream is = new InputStream(attachmentName);
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
//            request.writeBytes("PostData=" + strJson);
            request.writeBytes(strJson);
            request.flush();            //flush output stream
            request.close();            //close output stream

            int iResponseCode = httpsUrlConnection.getResponseCode();
            Log.i("STATUS", String.valueOf(iResponseCode));
            String strResponseMsg = httpsUrlConnection.getResponseMessage();
            Log.i("MSG" , strResponseMsg);

            return strResponseMsg;
        }
        catch (Exception ex) {
            Log.e("AsyncThread:doInBackground:Ex", ex.toString());
            //todo: handle
        } finally {
            if (httpsUrlConnection != null) {
                httpsUrlConnection.disconnect();
            }
        }
        return null;
    }
}
