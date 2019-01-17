package com.zboo.leaderboard;

public abstract class AbstractAuthorizeService {

    public abstract boolean authorizeUser(String username, String... tokens);

}
