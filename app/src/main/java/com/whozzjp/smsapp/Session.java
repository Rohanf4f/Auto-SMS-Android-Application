package com.whozzjp.smsapp;

public class Session {

    private  String userID;
    private  String currentDeviceId;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCurrentDeviceId() {
        return currentDeviceId;
    }

    public void setCurrentDeviceId(String currentDeviceId) {
        this.currentDeviceId = currentDeviceId;
    }

    public Session(String userId, String currentDeviceId) {
        this.userID=userId;
        this.currentDeviceId=currentDeviceId;
    }
}
