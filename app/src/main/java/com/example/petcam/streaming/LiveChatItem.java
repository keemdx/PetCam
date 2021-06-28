package com.example.petcam.streaming;

import com.google.gson.annotations.SerializedName;

public class LiveChatItem {

    @SerializedName("userId") private String userId;
    @SerializedName("userName") private String userName;
    @SerializedName("userProfileImage") private String userProfileImage;
    @SerializedName("message") private String message;
    @SerializedName("liveTime") private String liveTime;

    public LiveChatItem(String userId, String userName, String userProfileImage, String message) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(String liveTime) {
        this.liveTime = liveTime;
    }
}
