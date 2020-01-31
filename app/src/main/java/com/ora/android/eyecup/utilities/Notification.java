package com.ora.android.eyecup.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ora.android.eyecup.MainActivity;
import com.ora.android.eyecup.R;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static com.ora.android.eyecup.Globals.EYECUP_NOTIFY_CHANNEL;

public class Notification {
    private PendingIntent notificationPendingIntent;

    public android.app.Notification setNotification(Context context, String title, String text, int icon) {

        if (notificationPendingIntent == null) {
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        }

        android.app.Notification notification;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the support library
        CharSequence name = "Permanent Notification";
        //mContext.getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_LOW;

        String CHANNEL_ID = EYECUP_NOTIFY_CHANNEL;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        //String description = mContext.getString(R.string.notifications_description);
        String description = "eye Cup Notification:";
        channel.setDescription(description);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
        notification = notificationBuilder
                //the log is PNG file format with a transparent background
                .setSmallIcon(icon)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(notificationPendingIntent)
                .build();

        return notification;
    }
}
