package com.tunghq.fsocialmobileapp.Models;

public class Posts {
    private String time;
    private String postId;
    private  String postUrl;
    private  String description;
    private  String userId;
    private String profileImg;
    private  String featuredName;
    private  String userName;
    String type;


    public Posts() {
    }

    public Posts(String time, String postId, String postUrl, String description, String userId, String profileImg, String featuredName, String userName, String type) {
        this.time = time;
        this.postId = postId;
        this.postUrl = postUrl;
        this.description = description;
        this.userId = userId;
        this.profileImg = profileImg;
        this.featuredName = featuredName;
        this.userName = userName;
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getFeaturedName() {
        return featuredName;
    }

    public void setFeaturedName(String featuredName) {
        this.featuredName = featuredName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
