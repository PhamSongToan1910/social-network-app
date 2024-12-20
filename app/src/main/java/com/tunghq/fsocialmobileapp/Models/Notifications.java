package com.tunghq.fsocialmobileapp.Models;

public class Notifications {
    String affectedPersonId;
    String groupId;
    String notificationId;
    String userId;
    String textNotifications;
    String postId;
    String check;
    String status;


    public Notifications(){}

    public Notifications(String affectedPersonId, String groupId, String notificationId, String userId, String textNotifications, String postId, String check, String status) {
        this.affectedPersonId = affectedPersonId;
        this.groupId = groupId;
        this.notificationId = notificationId;
        this.userId = userId;
        this.textNotifications = textNotifications;
        this.postId = postId;
        this.check = check;
        this.status = status;
    }

    public String getAffectedPersonId() {
        return affectedPersonId;
    }

    public void setAffectedPersonId(String affectedPersonId) {
        this.affectedPersonId = affectedPersonId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTextNotifications() {
        return textNotifications;
    }

    public void setTextNotifications(String textNotifications) {
        this.textNotifications = textNotifications;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
