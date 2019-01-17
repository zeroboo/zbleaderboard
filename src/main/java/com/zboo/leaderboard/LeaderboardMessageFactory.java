package com.zboo.leaderboard;

public class LeaderboardMessageFactory {
    public static LeaderboardPointResponse createLeaderboardPointResponse()
    {
        LeaderboardPointResponse resp = new LeaderboardPointResponse();
        resp.setVersion(LeaderboardResponse.DEFAULT_VERSION);
        return resp;
    }
    public static LeaderboardLoginResponse createLeaderboardLoginResponse()
    {
        LeaderboardLoginResponse resp = new LeaderboardLoginResponse();
        resp.setVersion(LeaderboardResponse.DEFAULT_VERSION);
        return resp;
    }
}
