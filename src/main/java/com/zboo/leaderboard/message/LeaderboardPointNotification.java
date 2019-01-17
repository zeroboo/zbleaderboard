package com.zboo.leaderboard.message;

public class LeaderboardPointNotification {
    public String username;
    public long point;
    public int rank;

    public LeaderboardPointNotification(String username, long point, int rank) {
        this.username = username;
        this.point = point;
        this.rank = rank;
    }

}
