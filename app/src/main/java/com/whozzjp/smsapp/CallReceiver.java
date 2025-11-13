package com.whozzjp.smsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.images.WebImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class CallReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "call_receiver_channel";
    private static final int NOTIFICATION_ID = 1;
    private boolean mIsSubscribed = true;
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;
    private static final long MONTH_IN_MILLIS = 6 * 30 * DAY_IN_MILLIS;
    private static final long SUBSCRIPTION_DURATION = 6 * MONTH_IN_MILLIS;
    private static final long UN_SUBSCRIPTION_DURATION = 7 * DAY_IN_MILLIS;
    Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        SharedPreferences sharedPreferences = context.getSharedPreferences("phonenumbers", Context.MODE_PRIVATE);
        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String currentdatetime = dtf.format(now);

            //showToast(context, currentdatetime);

        } else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Uri uriCallLogs = Uri.parse("content://call_log/calls");
            String sortOrder = CallLog.Calls.DATE + " DESC";


            Cursor cursorCallLogs = context.getContentResolver().query(uriCallLogs, null, null, null, sortOrder);
            cursorCallLogs.moveToFirst();
            @SuppressLint("Range") String stringNumber = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER));

            String lastFourDigits = "";


            if (stringNumber.length() > 10) {
                lastFourDigits = stringNumber.substring(stringNumber.length() - 10);
            } else {
                lastFourDigits = stringNumber;
            }

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String currenttime = dateTimeFormatter.format(now);
            long currentTime=System.currentTimeMillis();
            String alreadysavednumberss = sharedPreferences.getString("hashmap", "");

            mIsSubscribed=prefs.getBoolean("Subscribed",false);
            long subscriptionStartTime = prefs.getLong("subscription_start_time", 0);
            boolean isSubscriptionExpired = false;
            if (subscriptionStartTime != 0) {
                long subscriptionDuration = mIsSubscribed ? SUBSCRIPTION_DURATION : UN_SUBSCRIPTION_DURATION;
                isSubscriptionExpired = (currentTime - subscriptionStartTime) >= subscriptionDuration;
                Log.d("isSubscriptionExpired", String.valueOf(isSubscriptionExpired));
            }

            if (isSubscriptionExpired) {
                // Subscription period has expired or has not started yet, display error message
                Log.d("Subscription period", String.valueOf(isSubscriptionExpired));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Subscription period has expired or has not started yet. You can't send SMS anymore.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                String deletedPhoneNumber = prefs.getString("deletedPhoneNumber", "");
                String phoneNumber = prefs.getString( "PhoneNumber" ,  "") ;
                // Check if the deleted phone number matches the number you are trying to send an SMS to
                if (!deletedPhoneNumber.isEmpty() && phoneNumber.equals(deletedPhoneNumber)) {
                    // Number has been deleted, do not send the SMS
                    showToast(context, "Number has been deleted");
                }
                else{


                Log.e("phone",lastFourDigits);
                if (alreadysavednumberss.isEmpty() || alreadysavednumberss == null) {
                    String message = getMessage();
                    String alreadyblocked = prefs.getString( "PhoneNumber" ,  "") ;

                    if ( !alreadyblocked.contains(lastFourDigits) && message!=null) {
                        SmsManager smsManager = SmsManager.getDefault();
                        ArrayList<String> par = smsManager.divideMessage(message);
                        smsManager.sendMultipartTextMessage(lastFourDigits, null, par, null, null);

                        Log.d("SMS get send to", stringNumber);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(lastFourDigits, currenttime);


                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(hashMap);
                        editor.putString("hashmap", json);
                        editor.apply();
                        // update subscription start time
                        if (subscriptionStartTime == 0) {
                            SharedPreferences.Editor editor1 = prefs.edit();
                            editor1.putLong("subscription_start_time", currentTime);
                            editor1.apply();
                        }
                    }else{
                        showToast( context , "number is blocked") ;
                    }
                }
                else {
                        String jsonobject = sharedPreferences.getString("hashmap", "");
                        Gson gson = new Gson();
                        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                        }.getType();
                        HashMap<String, String> oldhashmap = gson.fromJson(jsonobject, type);
                        if (!oldhashmap.containsKey(lastFourDigits)) {
                            String message = getMessage();

                            SmsManager smsManager = SmsManager.getDefault();
                            ArrayList<String> parts = smsManager.divideMessage(message);
                            smsManager.sendMultipartTextMessage(lastFourDigits, null, parts, null, null);

                            Log.d("SMS gets send to ", stringNumber);
                            oldhashmap.put(lastFourDigits, currenttime);

                            String newhashmapstring = gson.toJson(oldhashmap);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("hashmap", newhashmapstring);
                            editor.apply();


                        } else {

                            String messages = getMessage();

                            String olddate = oldhashmap.get(lastFourDigits);

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                            try {
                                Date date = simpleDateFormat.parse(olddate);
                                Date date1 = simpleDateFormat.parse(currenttime);
                                long diffindate = date1.getTime() - date.getTime();

                                long diffindates = TimeUnit.MILLISECONDS.toDays(diffindate) % 365;
                                if (diffindates > 1) {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    ArrayList<String> part = smsManager.divideMessage(messages);
                                    smsManager.sendMultipartTextMessage(lastFourDigits, null, part, null, null);
                                    oldhashmap.put(lastFourDigits, currenttime);
                                    String newtimeadded = gson.toJson(oldhashmap);

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("hashmap", newtimeadded);
                                    editor.apply();

                                }
                            } catch (Exception e) {
                                showToast(context, "exception");

                            }
                        }

                }
            }
            }
        }

    }
    private String getMessage() {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (!mIsSubscribed) {
            if(prefs!=null) {
                return prefs.getString("message", "");
            }
            return null;
        } else {
            if(prefs!=null) {
                String mesg = prefs.getString("message", "");
                String messages = mesg + "\nYou are not Subscribed!!!";
                return messages;
            }
            return null;
        }
    }

    void showToast(Context context,String message){
        Toast toast=Toast.makeText(context,message,Toast.LENGTH_LONG);
        toast.show();
    }
}




/*
public class CallReceiver extends BroadcastReceiver {
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;
    private static final long MONTH_IN_MILLIS = 6 * 30 * DAY_IN_MILLIS;
    private static final long SUBSCRIPTION_DURATION = 6 * MONTH_IN_MILLIS;
    private static final long UN_SUBSCRIPTION_DURATION = 7 * DAY_IN_MILLIS;
    private static final String SMS_SENT_ACTION = "com.whozzjp.smsapp.SENT";

    private final Gson gson = new Gson();
    private boolean mIsSubscribed = true;
    private final Handler mHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            // get last called number
            Uri uriCallLogs = CallLog.Calls.CONTENT_URI;
            String sortOrder = CallLog.Calls.DATE + " DESC";
            Cursor cursorCallLogs = context.getContentResolver().query(uriCallLogs, null, null, null, sortOrder);
            if (cursorCallLogs == null || !cursorCallLogs.moveToFirst()) {
                return;
            }
            // rest of the code to extract last 4 digits of number

            @SuppressLint("Range") String stringNumber = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER));
            Log.e("PhoneNumber",stringNumber);
            //cursorCallLogs.close();
            // extract last 4 digits of number
            String lastFourDigits = "";
            if (stringNumber.length() > 10) {
                lastFourDigits = stringNumber.substring(stringNumber.length() - 4);
            } else {
                lastFourDigits = stringNumber;
            }
            // get current time
            long currentTime = System.currentTimeMillis();
            // retrieve last sent times from shared preferences
            SharedPreferences mSharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String json = mSharedPreferences.getString("hashmap", "");
            Type type = new TypeToken<HashMap<String, Long>>() {
            }.getType();
            HashMap<String, Long> mLastSentTimes = gson.fromJson(json, type);
            if (mLastSentTimes == null) {
                mLastSentTimes = new HashMap<>();
            }

            mIsSubscribed = mSharedPreferences.getBoolean("Subscribed", false);
            Log.d("mIsSubscribed", String.valueOf(mIsSubscribed));

            // check if subscription period has expired
            long subscriptionStartTime = mSharedPreferences.getLong("subscription_start_time", 0);
            boolean isSubscriptionExpired = false;
            if (subscriptionStartTime != 0) {
                long subscriptionDuration = mIsSubscribed ? SUBSCRIPTION_DURATION : UN_SUBSCRIPTION_DURATION;
                isSubscriptionExpired = (currentTime - subscriptionStartTime) >= subscriptionDuration;
            }

            if (isSubscriptionExpired) {
                // Subscription period has expired or has not started yet, display error message
                Log.d("Subscription period", String.valueOf(true));
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Subscription period has expired or has not started yet. You can't send SMS anymore.", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
// check if it's time to send the SMS
            Long lastSentTime = mLastSentTimes.get(lastFourDigits);
            long interval = mIsSubscribed ?  SUBSCRIPTION_DURATION : UN_SUBSCRIPTION_DURATION;
            Log.d("INTERVAL", String.valueOf(interval));
            //Log.d("USERTIME", String.valueOf(currentTime - lastSentTime));
            if(!mLastSentTimes.containsKey(lastFourDigits)){
            if (lastSentTime==null||currentTime - lastSentTime <= interval) {
                // Send SMS
                PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT_ACTION), PendingIntent.FLAG_IMMUTABLE);

                String message = getMessage();
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(stringNumber, null, message, null, null);
                Log.d("SMS get send to ", stringNumber);
                // Save last sent time
                mLastSentTimes.put(lastFourDigits, currentTime);
                json = gson.toJson(mLastSentTimes);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("hashmap", json);
                editor.apply();
                // update subscription start time
                if (subscriptionStartTime == 0) {
                    SharedPreferences.Editor editor1 = mSharedPreferences.edit();
                    editor1.putLong("subscription_start_time", currentTime);
                    editor1.apply();
                }
            }


            }
            // Set timer to send next SMS after DAY_IN_MILLIS
            if (lastSentTime!=null && currentTime - lastSentTime >= DAY_IN_MILLIS) {
                Log.d("24 Hours", String.valueOf(DAY_IN_MILLIS));
                HashMap<String, Long> finalMLastSentTimes = mLastSentTimes;
                String finalLastFourDigits = lastFourDigits;
                mHandler.postDelayed(new Runnable() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void run() {
                        Long lastSentTime = finalMLastSentTimes.get(finalLastFourDigits);
                        if (lastSentTime == null) {
                            // Send next SMS
                            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT_ACTION), PendingIntent.FLAG_IMMUTABLE);
                            String message = getMessage();
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(stringNumber, null, message, null, null);
                            Log.d("SMS get send after 24 hours to ", stringNumber);
                            // Update lastSentTime
                            finalMLastSentTimes.put(finalLastFourDigits, System.currentTimeMillis());
                            String json = gson.toJson(finalMLastSentTimes);
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("hashmap", json);
                            editor.apply();
                        }
                    }
                }, DAY_IN_MILLIS);
            }
        }


    }


    private String getMessage() {
        if (!mIsSubscribed) {
            return "Thank You For Contacting Us!!!\nYou're subscribed!";
        } else {
            return "Thank You For Contacting Us!!!\nYou're not subscribed!";
        }
    }
}
*/



/*
public class CallReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "call_receiver_channel";

    private static final String SMS_SENT_ACTION = "com.example.sms.SENT";
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final long WEEK_IN_MILLIS = 7 * DAY_IN_MILLIS;
    private static final long MONTH_IN_MILLIS = 30 * DAY_IN_MILLIS;
    private HashMap<String, Long> mLastSentTimes;
    private boolean mIsSubscribed = false;
    Gson gson = new Gson();
    private SharedPreferences mSharedPreferences;

    private Context context;
    private Handler mHandler = new Handler();


    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE)){


         */
/*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Call Receiver Channel",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Notification channel for CallReceiver");

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);
            }

            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification)
                    .setContentTitle("Call Receiver")
                    .setContentText("Listening for phone state changes")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);*//*




            Uri uriCallLogs = Uri.parse("content://call_log/calls");
            String sortOrder = CallLog.Calls.DATE + " DESC";
            Cursor cursorCallLogs = context.getContentResolver().query(uriCallLogs, null, null, null, sortOrder);

            if (cursorCallLogs != null && cursorCallLogs.moveToFirst()) {
                @SuppressLint("Range") String number = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER));
                String lastFourDigits = number.length() >= 4 ? number.substring(number.length() - 4) : number;


                // Check if we've already sent an SMS to this number in the past 7 days or 6 months depending on subscription
                mSharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                final String[] json = {mSharedPreferences.getString("last_sent_times", "")};
                if (!json[0].isEmpty()) {
                    //Gson gson = new Gson();
                    Type type = new TypeToken<HashMap<String, Long>>() {}.getType();
                    mLastSentTimes = gson.fromJson(json[0], type);
                }

                if (mLastSentTimes == null) {
                    mLastSentTimes = new HashMap<>();
                }

                long currentTime = System.currentTimeMillis();
                Long lastSentTime = mLastSentTimes.get(lastFourDigits);

                if (lastSentTime == null || (currentTime - lastSentTime) >= (mIsSubscribed ? MONTH_IN_MILLIS : WEEK_IN_MILLIS)) {
                    final String[] message = {""};
                    try {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        String id = auth.getCurrentUser().getUid();


                    DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
                    root.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mIsSubscribed = Boolean.TRUE.equals(snapshot.child("Subscribed").getValue(Boolean.class));
                            Log.d("Firebase", String.valueOf(mIsSubscribed));
                            if (mIsSubscribed) {
                                message[0] = "Thank You For Contacting Us!!!\nYou're subscribed!";
                            } else {
                                message[0] = "Thank You For Contacting Us!!!\nYou're not subscribed!";
                            }

                            // Update the last sent time for this number
                            mLastSentTimes.put(lastFourDigits, currentTime);

                            SharedPreferences.Editor editor = mSharedPreferences.edit();

                            json[0] = gson.toJson(mLastSentTimes);
                            editor.putString("last_sent_times", json[0]);
                            editor.apply();

                            sendSms(context, number, message[0]);
                            Log.d("Message",message[0]);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    }catch (Exception e){
                        Log.d("Exception",e.getMessage());
                    }
                } else {
                    // Set a timer to send SMS after 24 hours
                    long delay = 24 * 60 * 60 * 1000 - (currentTime - lastSentTime);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onReceive(context, intent);
                        }
                    }, delay);
                }


            }

        }
    }
    public void clearHashMapData() {
        mLastSentTimes.clear();
    }

  */
/*  void clearSharedPreferences() {
        if(mSharedPreferences != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.apply();
        }else {
            Log.d("SharedPreferences ","Null");
        }
    }*//*

    */
/*private void sendSmsToFirstNumberInCallLog(Context context, String phoneNumber) {

        Uri uriCallLogs = Uri.parse("content://call_log/calls");
        String sortOrder = CallLog.Calls.DATE + " DESC";
        Cursor cursorCallLogs = context.getContentResolver().query(uriCallLogs, null, null, null, sortOrder);

        if (cursorCallLogs != null && cursorCallLogs.moveToFirst()) {
            @SuppressLint("Range") String number = cursorCallLogs.getString(cursorCallLogs.getColumnIndex(CallLog.Calls.NUMBER));
            String lastFourDigits = number.length() >= 4 ? number.substring(number.length() - 4) : number;


            // Check if we've already sent an SMS to this number in the past 7 days or 6 months depending on subscription
            mSharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            final String[] json = {mSharedPreferences.getString("last_sent_times", "")};
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Long>>() {}.getType();
            mLastSentTimes = gson.fromJson(json[0], type);

            if (mLastSentTimes == null) {
                mLastSentTimes = new HashMap<>();
            }

            long currentTime = System.currentTimeMillis();
            Long lastSentTime = mLastSentTimes.get(lastFourDigits);

            if (lastSentTime == null || (currentTime - lastSentTime) >= (mIsSubscribed ? MONTH_IN_MILLIS : WEEK_IN_MILLIS)) {
                final String[] message = {""};
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String id = auth.getCurrentUser().getUid();

                DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
                root.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mIsSubscribed = Boolean.TRUE.equals(snapshot.child("Subscribed").getValue(Boolean.class));
                        Log.d("Firebase", String.valueOf(mIsSubscribed));
                        if (mIsSubscribed) {
                            message[0] = "You're subscribed!";
                        } else {
                            message[0] = "You're not subscribed!";
                        }

                        // Update the last sent time for this number
                        mLastSentTimes.put(lastFourDigits, currentTime);

                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        json[0] = gson.toJson(mLastSentTimes);
                        editor.putString("last_sent_times", json[0]);
                        editor.apply();

                        sendSms(context, number, message[0]);
                        Log.d("Message",message[0]);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            } else {
                // Set a timer to send SMS after 24 hours
                long delay = (mIsSubscribed ? MONTH_IN_MILLIS : WEEK_IN_MILLIS) - (currentTime - lastSentTime);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendSmsToFirstNumberInCallLog(context, number);
                    }
                }, delay);
            }
        }
    }*//*

    private void sendSms(Context context, String phoneNumber, String message) {
        try {
            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(SMS_SENT_ACTION), 0);

            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> par = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, par, null, null);
            Log.d("SMS get Sended", phoneNumber);
        }catch (Exception e){
           Log.d("SMSManger",e.getMessage());
        }
    }
}*/
