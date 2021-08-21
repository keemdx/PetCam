package com.example.petcam.ui.profile.fanboard;

import com.google.gson.annotations.SerializedName;

public class FanboardItem {

    @SerializedName("fanboard_contents")
    private String fanboard_contents;
    @SerializedName("create_at")
    private String create_at;
    @SerializedName("fanboard_id")
    private String fanboard_id;
    @SerializedName("writer_id")
    private String writer_id;
    @SerializedName("writer_name")
    private String writer_name;
    @SerializedName("writer_photo")
    private String writer_photo;

    public String getFanboard_contents() {
        return fanboard_contents;
    }

    public void setFanboard_contents(String fanboard_contents) {
        this.fanboard_contents = fanboard_contents;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getFanboard_id() {
        return fanboard_id;
    }

    public void setFanboard_id(String fanboard_id) {
        this.fanboard_id = fanboard_id;
    }

    public String getWriter_id() {
        return writer_id;
    }

    public void setWriter_id(String writer_id) {
        this.writer_id = writer_id;
    }

    public String getWriter_name() {
        return writer_name;
    }

    public void setWriter_name(String writer_name) {
        this.writer_name = writer_name;
    }

    public String getWriter_photo() {
        return writer_photo;
    }

    public void setWriter_photo(String writer_photo) {
        this.writer_photo = writer_photo;
    }
}