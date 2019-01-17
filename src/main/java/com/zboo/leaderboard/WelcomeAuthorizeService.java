package com.zboo.leaderboard;

/**
 * Welcome authorize owner will welcome everyone!
 * */
public class WelcomeAuthorizeService extends AbstractAuthorizeService{
    public WelcomeAuthorizeService() {
    }


    @Override
    public boolean authorizeUser(String username, String... tokens) {
        return true;
    }
}
