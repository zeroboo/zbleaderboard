package com.zboo.leaderboard;

public class LeaderboardPointNotification {
    String username;
    long point;
    int rank;

    public LeaderboardPointNotification(String username, long point, int rank) {
        this.username = username;
        this.point = point;
        this.rank = rank;
    }

}
