package com.zboo.leaderboard;

public class LeaderboardPointResponse extends LeaderboardResponse{
    long currentPoint = 0;
    public LeaderboardPointResponse()
    {
        super();
    }
    public LeaderboardPointResponse(boolean success, String error, String version, String username) {
        super(success, error, version, username);
    }

    public long getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(long point) {
        this.currentPoint = point;
    }
}
