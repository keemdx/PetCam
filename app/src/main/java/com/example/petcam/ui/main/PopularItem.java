package com.example.petcam.ui.main;

import com.google.gson.annotations.SerializedName;

public class PopularItem {

    @SerializedName("streamer_title")
    private String streamer_title;
    @SerializedName("thumbnail_image")
    private String thumbnail_image;
    @SerializedName("streamer_name")
    private String streamer_name;
    @SerializedName("create_at")
    private String create_at;
    @SerializedName("room_id")
    private String room_id;

    public String getStreamer_title() {
        return streamer_title;
    }

    public void setStreamer_title(String streamer_title) {
        this.streamer_title = streamer_title;
    }

    public String getThumbnail_image() {
        return thumbnail_image;
    }

    public void setThumbnail_image(String thumbnail_image) {
        this.thumbnail_image = thumbnail_image;
    }

    public String getStreamer_name() {
        return streamer_name;
    }

    public void setStreamer_name(String streamer_name) {
        this.streamer_name = streamer_name;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }
}