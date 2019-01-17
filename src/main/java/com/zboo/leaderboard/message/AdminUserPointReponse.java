package com.zboo.leaderboard.message;

public class AdminUserPointReponse {
    public String adminName;
    public String userName;
    public boolean deleted;



    public AdminUserPointReponse(String adminName, String userName, boolean success) {
        this.adminName = adminName;
        this.userName = userName;
        this.deleted = success;
    }
}
