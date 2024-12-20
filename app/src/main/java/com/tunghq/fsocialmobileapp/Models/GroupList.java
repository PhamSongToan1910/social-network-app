package com.tunghq.fsocialmobileapp.Models;

public class GroupList {
    String groupId;
    String groupTitle;
    String groupDesc;
    String timeStamp;
    String creatorId;
    String groupImg;

    public GroupList() {
    }

    public GroupList(String groupId, String groupTitle, String groupDesc, String timeStamp, String creatorId, String groupImg) {
        this.groupId = groupId;
        this.groupTitle = groupTitle;
        this.groupDesc = groupDesc;
        this.timeStamp = timeStamp;
        this.creatorId = creatorId;
        this.groupImg = groupImg;
    }
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getGroupImg() {
        return groupImg;
    }

    public void setGroupImg(String groupImg) {
        this.groupImg = groupImg;
    }


}
