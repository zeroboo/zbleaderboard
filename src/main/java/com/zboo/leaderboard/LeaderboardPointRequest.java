package com.zboo.leaderboard;

class LeaderboardPointRequest extends LeaderboardRequest{
    long newPoint;
    public LeaderboardPointRequest()
    {
        super();
        newPoint = 0;
    }

    public long getNewPoint() {
        return newPoint;
    }


}
