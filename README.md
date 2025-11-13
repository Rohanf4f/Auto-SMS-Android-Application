# 📱 Auto SMS Android Application

**Auto SMS App** automatically sends pre-set SMS messages when you receive, miss, or make calls — perfect for when you’re busy, driving, or in meetings.

---

## 🚀 Overview

The **Auto SMS App** listens to incoming, outgoing, and missed calls, then automatically sends an SMS message that the user has configured for each call type.

This app runs in the background and uses **Android BroadcastReceivers** to detect call states and **SmsManager** to send messages.

---

## ✨ Features

✅ **Automatic SMS replies**
- **Incoming Call:** Send a personalized message when a call is received.  
- **Missed Call:** Send a follow-up message after the user misses a call.  
- **Outgoing Call:** Send a message to the contact you just called.

✅ **Customizable messages**
- Users can set their own templates for each event:
  - Incoming call message  
  - Missed call message  
  - Outgoing call message  

✅ **Smart control**
- Option to enable/disable auto SMS for each call type.  
- Works silently in the background.  
- Saves sent messages in the app for review.  

✅ **Permissions handling**
- Request required permissions at runtime for Android 6.0+:
  - `READ_PHONE_STATE`
  - `READ_CALL_LOG`
  - `SEND_SMS`
  - `READ_CONTACTS`
  - `RECEIVE_SMS`

---

## 📁 Project Structure

```
AutoSMSApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/autosms/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SmsSender.java
│   │   │   │   ├── CallReceiver.java
│   │   │   │   └── PreferencesManager.java
│   │   │   └── res/
│   │   │       ├── layout/activity_main.xml
│   │   │       ├── values/strings.xml
│   │   │       └── values/colors.xml
│   ├── build.gradle
└── README.md
```

---

## 🧠 Core Logic

### 1️⃣ Detect Call Events

```java
public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            // Incoming call
            SmsSender.sendAutoSms(context, incomingNumber, "incoming");
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            // Missed call
            SmsSender.sendAutoSms(context, incomingNumber, "missed");
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            // Outgoing call
            SmsSender.sendAutoSms(context, incomingNumber, "outgoing");
        }
    }
}
```

### 2️⃣ Send SMS Automatically

```java
public class SmsSender {
    public static void sendAutoSms(Context context, String phoneNumber, String callType) {
        String message = PreferencesManager.getMessage(context, callType);
        if (message == null || message.isEmpty()) return;

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
```

### 3️⃣ User Settings

Users can define custom SMS messages for:
- Incoming calls  
- Missed calls  
- Outgoing calls  

These preferences are stored in `SharedPreferences` via `PreferencesManager`.

---

## 🛠️ Permissions in Manifest

```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.READ_CALL_LOG"/>
<uses-permission android:name="android.permission.SEND_SMS"/>
<uses-permission android:name="android.permission.READ_CONTACTS"/>
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
```

Register the receiver:

```xml
<receiver android:name=".CallReceiver" android:enabled="true" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.PHONE_STATE"/>
        <action android:name="android.intent.action.NEW_OUTGOING_CALL"/>
    </intent-filter>
</receiver>
```

---

## ⚙️ Setup & Build

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/AutoSMSApp.git
   cd AutoSMSApp
   ```
2. Open in **Android Studio**
3. Allow all permissions on your test device
4. Build & run the app on a physical Android device

---

## 💡 Future Enhancements

🔹 Add message scheduling  
🔹 Add contact-specific templates  
🔹 Enable/disable auto SMS by time (e.g., work hours)  
🔹 Add smart reply suggestions using ML Kit  
🔹 Integration with WhatsApp or Telegram auto replies  

---

## 🪪 License

This project is licensed under the **MIT License** — you may freely use, modify, and distribute it with attribution.

---

## 🙌 Author

**Developer:** Rohan Patil 
**Email:** rohanatil4002@gmail.com  
**Version:** 1.0  
**Release Year:** 2023

---

Thank you for using the Auto SMS Android App! 💬

