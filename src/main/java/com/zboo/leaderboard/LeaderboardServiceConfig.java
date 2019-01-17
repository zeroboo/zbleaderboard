package com.zboo.leaderboard;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;
/**
 * Store config of a leaderboard service
 * */

public class LeaderboardServiceConfig {
    private static final String EMPTY_STRING = "";
    private static final int NETTY_WORKER_THREAD = 8;
    String apiHost;
    int apiPort;
    String redisHost;
    boolean ssl = false;
    GenericObjectPoolConfig jedisPool;
    int nettyWorkerThread;

    public LeaderboardServiceConfig()
    {
        this(EMPTY_STRING, 0, EMPTY_STRING, false);
    }

    public LeaderboardServiceConfig(String apiHost, int apiPort, String redisHost, boolean ssl) {
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        this.redisHost = redisHost;
        this.jedisPool = new JedisPoolConfig();
        this.ssl = ssl;
        this.nettyWorkerThread = NETTY_WORKER_THREAD;
    }

    public String getApiHost() {
        return apiHost;
    }

    public int getApiPort() {
        return apiPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public static LeaderboardServiceConfig createDefaultConfig()
    {
        LeaderboardServiceConfig config = new LeaderboardServiceConfig("127.0.0.1", 8080, "127.0.0.1", false);
        return config;
    }

    public boolean hasSSL() {
        return ssl;
    }

    public int getNettyWorkerThread() {
        return nettyWorkerThread;
    }

    public GenericObjectPoolConfig getJedisPool() {
        return jedisPool;
    }
}
