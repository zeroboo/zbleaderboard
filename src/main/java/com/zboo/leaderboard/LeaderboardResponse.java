package com.zboo.leaderboard;

public class LeaderboardResponse {
    static final String DEFAULT_VERSION = "1";
    static final String EMPTY_STRING = "";
    boolean success;
    String error;
    String version;
    String username;


    public LeaderboardResponse(boolean success, String error, String version, String username) {
        this.success = success;
        this.error = error;
        this.version = version;
        this.username = username;
    }

    public LeaderboardResponse() {
        this(false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
