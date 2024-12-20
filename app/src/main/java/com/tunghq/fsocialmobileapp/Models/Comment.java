package com.tunghq.fsocialmobileapp.Models;

public class Comment {
    String commentId;
    String comment;
    String userId;
    String time;

    public Comment() {
    }

    public Comment(String commentId, String comment, String userId, String time) {
        this.commentId = commentId;
        this.comment = comment;
        this.userId = userId;
        this.time = time;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
