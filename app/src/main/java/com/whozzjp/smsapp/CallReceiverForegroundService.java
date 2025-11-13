package com.whozzjp.smsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class CallReceiverForegroundService extends Service {

    private static final String CHANNEL_ID = "call_receiver_channel";
    private static final int NOTIFICATION_ID = 1;

    private CallReceiver mCallReceiver;


    @Override
    public void onCreate() {
        super.onCreate();

        // Create and register the CallReceiver
        mCallReceiver = new CallReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mCallReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create and show the notification
        createNotificationChannel();
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        // Return START_STICKY to keep the service running in the background
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the CallReceiver
        unregisterReceiver(mCallReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Creates the notification channel for the foreground service.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Receiver Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notification channel for CallReceiver");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Creates the persistent notification for the foreground service.
     *
     * @return the notification object
     */
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Call Receiver")
                .setContentText("Listening for phone state changes")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        return builder.build();
    }
}

