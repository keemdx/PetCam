package com.example.petcam.ui.chatting;

public class ChattingItem {
    private String time;
    private String userID;
    private String userName;
    private String userPhoto;
    private String message;
    private String status;

    public ChattingItem(String time, String userID, String userName, String userPhoto, String message, String status) {
        this.time = time;
        this.userID = userID;
        this.userName = userName;
        this.userPhoto = userPhoto;
        this.message = message;
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
