package com.zboo.leaderboard;

import io.netty.channel.ChannelHandlerContext;

public class LeaderboardUser {
    String username;
    ChannelHandlerContext context;

    public LeaderboardUser(String username, ChannelHandlerContext context) {
        this.username = username;
        this.context = context;
    }

    public String getUsername() {
        return username;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }
}
