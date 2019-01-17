package com.zboo.leaderboard.message;

public class LeaderboardMessageFactory {
    static final String EMPTY_STRING = "";
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
    public static AdminUserPointRequest createAdminDeleteUserPointRequest()
    {
        return new AdminUserPointRequest(EMPTY_STRING, EMPTY_STRING);
    }
    public static AdminUserPointReponse createAdminDeleteUserPointResponse()
    {
        return new AdminUserPointReponse(EMPTY_STRING, EMPTY_STRING, false);
    }
}
