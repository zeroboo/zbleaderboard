package com.zboo.leaderboard;

class LeaderboardPointRequest{
    static final String EMPTY_STRING = "";
    String username;
    long newPoint;

    public LeaderboardPointRequest()
    {
        username = EMPTY_STRING;
        newPoint = 0;
    }


}
