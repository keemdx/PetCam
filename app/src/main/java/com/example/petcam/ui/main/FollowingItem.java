package com.example.petcam.ui.main;

import com.google.gson.annotations.SerializedName;

public class FollowingItem {

    @SerializedName("streamer_image")
    private String streamer_image;
    @SerializedName("room_id")
    private String room_id;

    public String getStreamer_image() {
        return streamer_image;
    }

    public void setStreamer_image(String streamer_image) {
        this.streamer_image = streamer_image;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }
}