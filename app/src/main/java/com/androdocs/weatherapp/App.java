package com.androdocs.weatherapp;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class App extends Application {
    public static final String CHANNEL_ID = "Warning";

    RelativeLayout viewActRec, viewUserProfile, viewMain, viewPlantRec;
    LinearLayout viewFoodRec;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Warning", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Warning Channel");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}