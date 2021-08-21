package com.example.petcam.ui.profile.vod;

import com.google.gson.annotations.SerializedName;

public class VODItem {

    @SerializedName("roomID")
    private long roomID;
    @SerializedName("title")
    private String title;
    @SerializedName("createAt")
    private String createAt;
    @SerializedName("hits")
    private int hits;
    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("name")
    private String name;
    @SerializedName("profileImage")
    private String profileImage;

    public VODItem(long roomID, String title, String createAt, int hits, String thumbnail, String name, String profileImage) {
        this.roomID = roomID;
        this.title = title;
        this.createAt = createAt;
        this.hits = hits;
        this.thumbnail = thumbnail;
        this.name = name;
        this.profileImage = profileImage;
    }

    public long getRoomID() {
        return roomID;
    }

    public void setRoomID(long roomID) {
        this.roomID = roomID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
