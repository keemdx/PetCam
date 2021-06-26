package com.example.petcam.streaming;

import com.google.gson.annotations.SerializedName;

public class ViewersItem {
    @SerializedName("viewer_image") private String viewer_image;
    @SerializedName("thumbnail_image") private String thumbnail_image;

    public ViewersItem(String viewer_image) {
        this.viewer_image = viewer_image;
    }

    public String getViewer_image() {
        return viewer_image;
    }

    public void setViewer_image(String viewer_image) {
        this.viewer_image = viewer_image;
    }

    public String getThumbnail_image() {
        return thumbnail_image;
    }

    public void setThumbnail_image(String thumbnail_image) {
        this.thumbnail_image = thumbnail_image;
    }
}