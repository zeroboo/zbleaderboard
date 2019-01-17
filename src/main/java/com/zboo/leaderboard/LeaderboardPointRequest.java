package com.zboo.leaderboard;

class LeaderboardPointRequest{
    static final String EMPTY_STRING = "";
    String username;
    long points;

    public LeaderboardPointRequest()
    {
        username = EMPTY_STRING;
        points = 0;
    }


}
