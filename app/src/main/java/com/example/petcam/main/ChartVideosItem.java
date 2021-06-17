package com.example.petcam.main;

public class ChartVideosItem {

    private String title, createAt, hits, thumbnail, name, profileImage;
    private int hitsImage;

    public ChartVideosItem(String title, String createAt, String hits, String thumbnail, String name, String profileImage, int hitsImage) {
        this.title = title;
        this.createAt = createAt;
        this.hits = hits;
        this.thumbnail = thumbnail;
        this.name = name;
        this.profileImage = profileImage;
        this.hitsImage = hitsImage;
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

    public String getHits() {
        return hits;
    }

    public void setHits(String hits) {
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

    public int getHitsImage() {
        return hitsImage;
    }

    public void setHitsImage(int hitsImage) {
        this.hitsImage = hitsImage;
    }
}
