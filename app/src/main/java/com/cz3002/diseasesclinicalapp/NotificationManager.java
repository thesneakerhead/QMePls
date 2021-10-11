package com.cz3002.diseasesclinicalapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationManager {
    NotificationManagerCompat notificationManager;
    private Context notificationContext;


    public NotificationManager(Context context) {
        this.notificationContext = context;
    }


    public void sendNotification(){

        notificationManager = NotificationManagerCompat.from(notificationContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Queue Reminder";
            String description = "Queue Reminder";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("one", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            android.app.NotificationManager notificationManager = notificationContext.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        String title = "Reminder";
        String message = "You are next in line. Please ensure that you have arrived at the clinic.";

        Notification notification = new NotificationCompat.Builder(notificationContext, "one")
                .setSmallIcon(R.drawable.ic_one)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();


        notificationManager.notify(1, notification);
    }
}
