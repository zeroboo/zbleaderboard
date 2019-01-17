package com.zboo.leaderboard;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;
/**
 * Store config of a leaderboard owner
 * */

public class LeaderboardServiceConfig {
    public static final String EMPTY_STRING = "";
    public static final int NETTY_WORKER_THREAD = 8;
    public static final String DEFAULT_LEADERBOARD_KEY = "zbleaderboard";
    public static final String DEFAULT_LEADERBOARD_UPDATE_COUNTER = "zbleaderboard_update_counter";
    public static final int DEFAULT_REDIS_PORT = 6379;
    public static final int DEFAULT_API_PORT = 8080;
    public static final String DEFAULT_REDIS_PASSWORD = "";
    public static final int DEFAULT_REDIS_TIMEOUT_SECOND = 30;
    /***
     * Api endpoint's host
     */
    String apiHost;
    /***
     * Api endpoint's port
     */
    int apiPort;

    /**
     * Workers thread of netty.
     * Default is 8
     * */
    int nettyWorkerThread;

    ///For redis
    String redisHost;
    int redisPort = 6379;
    String redisPassword;
    int redisTimeoutSecond;

    String redisLeaderboardKey;
    String redisLeaderboardUpdateCounterKey;
    boolean ssl = false;
    JedisPoolConfig jedisPool;



    public LeaderboardServiceConfig()
    {
        this(EMPTY_STRING, DEFAULT_API_PORT, EMPTY_STRING, DEFAULT_REDIS_PORT, DEFAULT_REDIS_PASSWORD, DEFAULT_REDIS_TIMEOUT_SECOND, false);
    }

    public LeaderboardServiceConfig(String apiHost, int apiPort, String redisHost, int redisPort, String redisPassword, int redisTimeoutSecond, boolean ssl) {
        this.apiHost = apiHost;
        this.apiPort = apiPort;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.jedisPool = new JedisPoolConfig();
        this.redisPassword = redisPassword;
        this.redisTimeoutSecond = redisTimeoutSecond;

        this.ssl = ssl;
        this.nettyWorkerThread = NETTY_WORKER_THREAD;
        this.redisLeaderboardKey = DEFAULT_LEADERBOARD_KEY;
        this.redisLeaderboardUpdateCounterKey = DEFAULT_LEADERBOARD_UPDATE_COUNTER;
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
        LeaderboardServiceConfig config = new LeaderboardServiceConfig("127.0.0.1", DEFAULT_API_PORT
                , "127.0.0.1", DEFAULT_REDIS_PORT, DEFAULT_REDIS_PASSWORD, DEFAULT_REDIS_TIMEOUT_SECOND
                , false);
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

    public String getRedisLeaderboardKey() {
        return redisLeaderboardKey;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public void setApiPort(int apiPort) {
        this.apiPort = apiPort;
    }

    public void setRedisLeaderboardKey(String redisLeaderboardKey) {
        this.redisLeaderboardKey = redisLeaderboardKey;
    }

    public String getRedisLeaderboardUpdateCounterKey() {
        return redisLeaderboardUpdateCounterKey;
    }

    public void setRedisLeaderboardUpdateCounterKey(String redisLeaderboardUpdateCounterKey) {
        this.redisLeaderboardUpdateCounterKey = redisLeaderboardUpdateCounterKey;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisTimeoutSecond() {
        return redisTimeoutSecond;
    }

    public boolean isSsl() {
        return ssl;
    }
}
