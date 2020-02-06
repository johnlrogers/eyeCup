package com.ora.android.eyecup.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class OraUtil {

//    public static final int DT_FMT_FULL_DISPLAY = 1;
//    public static final int DT_FMT_FULL_FILENAME = 2;
//    public static final int DT_FMT_DATE = 3;
//    public static final int DT_FMT_TIME = 4;
//

//    public String GetDateStr(int iFormatCode, Date dt) {
//        String str;
//        SimpleDateFormat fmtDt;
//
//        switch (iFormatCode) {
//            case DT_FMT_FULL_DISPLAY:
//                fmtDt = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa");
//                break;
//            case DT_FMT_FULL_FILENAME:
//                fmtDt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//                break;
//            case DT_FMT_DATE:
//                fmtDt = new SimpleDateFormat("yyyy-MM-dd");
//                break;
//            case DT_FMT_TIME:
//                fmtDt = new SimpleDateFormat("HH:mm:ss");
//                break;
//            default:
//                fmtDt = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss aaa");
//                break;
//        }
//
//        str = fmtDt.dateFormat(dt);
//
//        return str;
//    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String strPath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(strPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeFile(strPath, options);
    }

}
