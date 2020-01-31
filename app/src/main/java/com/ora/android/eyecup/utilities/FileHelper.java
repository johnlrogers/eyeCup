package com.ora.android.eyecup.utilities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Tan on 2/18/2016.
 */
public class FileHelper {

    private String fileName;    // = "data.txt";
    private String path;        // = Environment.getExternalStorageDirectory().getAbsolutePath() + "/instinctcoder/readwrite/" ;
    private final static String TAG = FileHelper.class.getName();

//    public static String ReadFile( Context context){
//    public static String ReadFile(String path, String fileName){
    public String ReadFile(String path, String fileName){
        String line = null;
        String strFile = null;

        //todo Warning:(28, 45) String values are compared using '!=', not 'equals()'
        if (path.substring(path.length()-1) != "/") {
            path = path + "/";
        }
        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path + fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                //todo Warning:(39, 31) String concatenation as argument to 'StringBuilder.append()' call
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            strFile = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }
        catch(IOException ex) {
            // todo handle
            Log.d(TAG, ex.getMessage());
        }
        return strFile;
    }

//    public static boolean saveToFile( String data){
//    public static boolean saveToFile(String path, String fileName, String data){
    public boolean saveToFile(String path, String fileName, String data){
        try {
            new File(path).mkdir();
            File file = new File(path + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true;
        }  catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }  catch(IOException ex) {
            //todo handle
            Log.d(TAG, ex.getMessage());
        }
        return  false;


    }

    }