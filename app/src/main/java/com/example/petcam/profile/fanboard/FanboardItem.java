package com.example.petcam.profile.fanboard;

public class FanboardItem {

    private String name, text, time, commentCount, imageUrl;
    private int commentImage;

    public FanboardItem(String name, String text, String time, String commentCount, String imageUrl, int commentImage) {
        this.name = name;
        this.text = text;
        this.time = time;
        this.commentCount = commentCount;
        this.imageUrl = imageUrl;
        this.commentImage = commentImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCommentImage() {
        return commentImage;
    }

    public void setCommentImage(int commentImage) {
        this.commentImage = commentImage;
    }
}
