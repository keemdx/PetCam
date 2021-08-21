package com.example.petcam.ui.chatting;

import com.google.gson.annotations.SerializedName;

public class ChatroomItem {

    @SerializedName("chatroom_id")
    private String chatroom_id;
    @SerializedName("chatroom_name")
    private String chatroom_name;
    @SerializedName("chatroom_user_profile")
    private String chatroom_user_profile;
    @SerializedName("chatroom_user_num")
    private String chatroom_user_num;
    @SerializedName("last_message")
    private String last_message;
    @SerializedName("last_message_time")
    private String last_message_time;
    @SerializedName("day_check")
    private String day_check;
    @SerializedName("view_type")
    private int view_type;
    @SerializedName("unread_message_num")
    private String unread_message_num;

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public String getChatroom_name() {
        return chatroom_name;
    }

    public void setChatroom_name(String chatroom_name) {
        this.chatroom_name = chatroom_name;
    }

    public String getChatroom_user_profile() {
        return chatroom_user_profile;
    }

    public void setChatroom_user_profile(String chatroom_user_profile) {
        this.chatroom_user_profile = chatroom_user_profile;
    }

    public String getChatroom_user_num() {
        return chatroom_user_num;
    }

    public void setChatroom_user_num(String chatroom_user_num) {
        this.chatroom_user_num = chatroom_user_num;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getLast_message_time() {
        return last_message_time;
    }

    public void setLast_message_time(String last_message_time) {
        this.last_message_time = last_message_time;
    }

    public String getDay_check() {
        return day_check;
    }

    public void setDay_check(String day_check) {
        this.day_check = day_check;
    }

    public int getView_type() {
        return view_type;
    }

    public void setView_type(int view_type) {
        this.view_type = view_type;
    }

    public String getUnread_message_num() {
        return unread_message_num;
    }

    public void setUnread_message_num(String unread_message_num) {
        this.unread_message_num = unread_message_num;
    }
}
