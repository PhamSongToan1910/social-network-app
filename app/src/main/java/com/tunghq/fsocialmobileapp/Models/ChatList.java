package com.tunghq.fsocialmobileapp.Models;

public class ChatList {
    String chatListId; //we will need this id to get chat list, sender/receiver uid

    public ChatList() {
    }

    public ChatList(String chatListId) {
        this.chatListId = chatListId;
    }

    public String getChatListId() {
        return chatListId;
    }

    public void setChatListId(String chatListId) {
        this.chatListId = chatListId;
    }
}
