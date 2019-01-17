package com.zboo.leaderboard.message;

public class LeaderboardRequest {
    static final String EMPTY_STRING = "";
    public String username = EMPTY_STRING;
    public String password = EMPTY_STRING;
    public String token = EMPTY_STRING;

    public LeaderboardRequest()
    {

    }
    public LeaderboardRequest(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
