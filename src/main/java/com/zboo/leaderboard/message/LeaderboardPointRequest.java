package com.zboo.leaderboard.message;

public class LeaderboardPointRequest extends LeaderboardRequest{
    public long newPoint;
    public LeaderboardPointRequest()
    {
        super();
        newPoint = 0;
    }

    public long getNewPoint() {
        return newPoint;
    }


}
