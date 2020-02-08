package com.ora.android.eyecup;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AsyncThread extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {

        try {
            String attachmentName = strings[0].substring(strings[0].lastIndexOf("/") + 1);
            String attachmentFileName = attachmentName;
            String boundary =  "*****";
            String strJsonOut;
            String strJsonIn;

            HttpsURLConnection httpsUrlConnection = null;
            //URL url = new URL("https://icupapi.lionridgedev.com/");
            URL url = new URL("https://icupapi.lionridgedev.com/v1/diary/1/12345678/");
            httpsUrlConnection = (HttpsURLConnection) url.openConnection();
            httpsUrlConnection.setUseCaches(false);
            httpsUrlConnection.setDoOutput(true);

            httpsUrlConnection.setRequestMethod("POST");
            httpsUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpsUrlConnection.setRequestProperty("Cache-Control", "no-cache");
            httpsUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(httpsUrlConnection.getOutputStream());

            List<Byte> bytesList = new ArrayList<Byte>();
            try (InputStream in = new FileInputStream(strings[0])) {
                byte[] buf = new byte[1024];
                while (in.read(buf) > 0) {
                    for (int i = 0; i < buf.length; i++)
                        bytesList.add(buf[i]);
                }
            }
            byte[] bytes = new byte[bytesList.size()];
            for (int i = 0; i < bytes.length; i++)
                bytes[i] = bytesList.get(i);

            request.write(bytes);       //write the data to the output stream
            request.flush();            //flush output stream
            request.close();            //close output stream

            int iResponseCode = httpsUrlConnection.getResponseCode();
            String strResponseMsg = httpsUrlConnection.getResponseMessage();

//            InputStream responseStream = new BufferedInputStream(httpsUrlConnection.getInputStream());
//            BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
//
//            String line = "";
//            StringBuilder stringBuilder = new StringBuilder();
//
//            while ((line = responseStreamReader.readLine()) != null) {
//                stringBuilder.append(line).append("\n");
//            }
//            responseStreamReader.close();

//            responseStream.close();
            httpsUrlConnection.disconnect();

//            return stringBuilder.toString();
            return strResponseMsg;
        }
        catch (Exception ex) {
            Log.e("AsyncThread:doInBackground:Ex", ex.toString());
            //todo: handle
        }
        return null;
    }
}
