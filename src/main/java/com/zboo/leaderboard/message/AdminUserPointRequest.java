package com.zboo.leaderboard.message;

public class AdminUserPointRequest {
    public String admin;
    public String username;

    public AdminUserPointRequest(String admin, String username) {
        this.admin = admin;
        this.username = username;
    }
}
