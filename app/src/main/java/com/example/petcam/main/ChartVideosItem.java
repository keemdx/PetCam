package com.example.petcam.main;

import com.google.gson.annotations.SerializedName;

public class ChartVideosItem {

    @SerializedName("no") private int no;
    @SerializedName("roomID") private String roomID;
    @SerializedName("viewer") private int viewer;
    @SerializedName("roomTitle") private String roomTitle;
    @SerializedName("thumbnail") private String thumbnail;
    @SerializedName("userName") private String userName;
    @SerializedName("userProfileImage") private String userProfileImage;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public int getViewer() {
        return viewer;
    }

    public void setViewer(int viewer) {
        this.viewer = viewer;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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