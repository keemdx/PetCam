package com.example.petcam.ui.profile.notice;

import com.google.gson.annotations.SerializedName;

public class FixTopNoticeItem {

    @SerializedName("fix_top_notice_title")
    private String notice_title;
    @SerializedName("fix_top_notice_contents")
    private String notice_contents;
    @SerializedName("fix_top_create_at")
    private String create_at;
    @SerializedName("fix_top_pin")
    private String pin;
    @SerializedName("fix_notice_id")
    private int notice_id;
    @SerializedName("fix_top_writer_id")
    private String writer_id;
    @SerializedName("comment_count")
    private int comment_count;

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

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }
}
