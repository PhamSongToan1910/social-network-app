package com.tunghq.fsocialmobileapp.Models;

import com.google.firebase.database.PropertyName;

public class Chat {
    String message, receiver, sender, timeStamp, type, date, time, publicKey, signature;
    boolean isSeen;
    public Chat(){

    }

    public Chat(String message, String receiver, String sender, String timeStamp, String type, String date, String time, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timeStamp = timeStamp;
        this.type = type;
        this.date = date;
        this.time = time;
        this.isSeen = isSeen;
    }

    public Chat(String message, String publicKey, String receiver, String sender, String signature, String timeStamp, String type) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timeStamp = timeStamp;
        this.type = type;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }


    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    @PropertyName("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    @PropertyName("publicKey")
    public void setPublicKey(String publicKey) {this.publicKey = publicKey;}

    @PropertyName("signature")
    public String getSignature() {
        return signature;
    }

    @PropertyName("signature")
    public void setSignature(String signature) {this.signature = signature;}
}
