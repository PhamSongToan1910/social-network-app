package com.tunghq.fsocialmobileapp.Models;

public class User {
    String userName;
    String email;
    String featuredName;
    String userId;
    String backgroundImg;
    String profileImg;
    String status;

    String onlineStatus;
    String typingTo;

    public User() {
    }

    public User(String userName, String email, String featuredName, String userId, String backgroundImg, String profileImg, String status, String onlineStatus, String typingTo) {
        this.userName = userName;
        this.email = email;
        this.featuredName = featuredName;
        this.userId = userId;
        this.backgroundImg = backgroundImg;
        this.profileImg = profileImg;
        this.status = status;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFeaturedName() {
        return featuredName;
    }

    public void setFeaturedName(String featuredName) {
        this.featuredName = featuredName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBackgroundImg() {
        return backgroundImg;
    }

    public void setBackgroundImg(String backgroundImg) {
        this.backgroundImg = backgroundImg;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }
}
