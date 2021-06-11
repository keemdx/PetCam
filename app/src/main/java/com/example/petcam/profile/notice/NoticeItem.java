package com.example.petcam.profile.notice;

import com.google.gson.annotations.SerializedName;

public class NoticeItem {

    @SerializedName("notice_title") private String notice_title;
    @SerializedName("notice_contents") private String notice_contents;
    @SerializedName("create_at") private String create_at;
    @SerializedName("pin") private String pin;
    @SerializedName("notice_id") private int notice_id;
    @SerializedName("writer_id") private String writer_id;

    public String getNotice_title() {
        return notice_title;
    }

    public void setNotice_title(String notice_title) {
        this.notice_title = notice_title;
    }

    public String getNotice_contents() {
        return notice_contents;
    }

    public void setNotice_contents(String notice_contents) {
        this.notice_contents = notice_contents;
    }

    public String getCreate_at() {
        return create_at;
    }

    public void setCreate_at(String create_at) {
        this.create_at = create_at;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getNotice_id() {
        return notice_id;
    }

    public void setNotice_id(int notice_id) {
        this.notice_id = notice_id;
    }

    public String getWriter_id() {
        return writer_id;
    }

    public void setWriter_id(String writer_id) {
        this.writer_id = writer_id;
    }
}
