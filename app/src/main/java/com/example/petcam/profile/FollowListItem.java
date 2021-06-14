package com.example.petcam.profile;

import com.google.gson.annotations.SerializedName;

public class FollowListItem {

    @SerializedName("userId") private String userId;
    @SerializedName("userName") private String userName;
    @SerializedName("userProfileImage") private String userProfileImage;

    public FollowListItem(String userId, String userName, String userProfileImage) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }
}
