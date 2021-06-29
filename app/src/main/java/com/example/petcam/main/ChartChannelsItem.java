package com.example.petcam.main;

import com.google.gson.annotations.SerializedName;

public class ChartChannelsItem {

    @SerializedName("no") private int no;
    @SerializedName("cnt") private int cnt;
    @SerializedName("userId") private String userId;
    @SerializedName("userName") private String userName;
    @SerializedName("userProfileImage") private String userProfileImage;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
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
}
