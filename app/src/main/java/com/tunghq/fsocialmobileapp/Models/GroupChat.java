package com.tunghq.fsocialmobileapp.Models;

public class GroupChat {
    String groupId;
    String messageId;
    String message;
    String timeStamp;
    String date;
    String time;
    String type;
    String sender;

    public GroupChat() {
    }


    public GroupChat(String groupId, String messageId, String message, String timeStamp, String date, String time, String type, String sender) {
        this.groupId = groupId;
        this.messageId = messageId;
        this.message = message;
        this.timeStamp = timeStamp;
        this.date = date;
        this.time = time;
        this.type = type;
        this.sender = sender;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
