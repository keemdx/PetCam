package com.example.petcam.profile.notice;

import com.google.gson.annotations.SerializedName;

public class NoticeCommentItem {

    @SerializedName("comment_id") private String comment_id;
    @SerializedName("comment_user_id") private String comment_user_id;
    @SerializedName("comment_user_name") private String comment_user_name;
    @SerializedName("comment_profile_url") private String comment_profile_url;
    @SerializedName("comment_text") private String comment_text;
    @SerializedName("comment_create_at") private String comment_create_at;


    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getComment_user_id() {
        return comment_user_id;
    }

    public void setComment_user_id(String comment_user_id) {
        this.comment_user_id = comment_user_id;
    }

    public String getComment_user_name() {
        return comment_user_name;
    }

    public void setComment_user_name(String comment_user_name) {
        this.comment_user_name = comment_user_name;
    }

    public String getComment_profile_url() {
        return comment_profile_url;
    }

    public void setComment_profile_url(String comment_profile_url) {
        this.comment_profile_url = comment_profile_url;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getComment_create_at() {
        return comment_create_at;
    }

    public void setComment_create_at(String comment_create_at) {
        this.comment_create_at = comment_create_at;
    }

}
