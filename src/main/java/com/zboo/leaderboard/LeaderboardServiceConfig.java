package com.zboo.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPoolConfig;
/**
 * Store config of a leaderboard owner
 * */

public class LeaderboardServiceConfig {
    public static final String EMPTY_STRING = "";

    public static final String DEFAULT_REDIS_KEY_LEADERBOARD = "zbleaderboard";
    public static final String DEFAULT_REDIS_KEY_LEADERBOARD_UPDATE = "zbleaderboard_update_counter";
    public static final int DEFAULT_REDIS_PORT = 6379;
    public static final int DEFAULT_API_USER_PORT = 8080;
    public static final int DEFAULT_API_ADMIN_PORT = 8081;
    public static final String DEFAULT_REDIS_PASSWORD = "";
    public static final int DEFAULT_REDIS_TIMEOUT_SECOND = 30;
    public static final int DEFAULT_NETTY_USER_WORKER_THREAD = 8;
    public static final int DEFAULT_NETTY_ADMIN_WORKER_THREAD = 1;

    /***
     * Host of api user
     */
    String apiUserHost = EMPTY_STRING;
    /***
     * Port of api user
     */
    int apiUserPort;


    /**
     * User ssl for User admin?
     */
    boolean apiUserSSL;

    /**
     * Workers thread of netty, used for users's api.
     * Default is 8
     * */
    int apiUserNettyWorkerThread;
    /**
     * Workers thread of netty, used for admin's api.
     * Default is 1
     * */
    int apiAdminNettyWorkerThread;

    /*
    * Host of api user
    */
    String apiAdminHost = EMPTY_STRING;
    /***
     * Port of api user
     */
    int apiAdminPort;

    /**
     * User ssl for api admin?
     * */
    boolean apiAdminSSL;

    ///For redis
    String redisHost;
    int redisPort = 6379;
    boolean redisSSL;
    String redisPassword;
    int redisTimeoutSecond;

    String redisLeaderboardKey;
    String redisLeaderboardUpdateCounterKey;
    JedisPoolConfig jedisPool;


    public LeaderboardServiceConfig(String apiUserHost, int apiUserPort, boolean apiUserSSL, int apiUserNettyWorkerThread
            , String apiAdminHost, int apiAdminPort, boolean apiAdminSSL, int apiAdminNettyWorkerThread
            , String redisHost, int redisPort, boolean redisSSL, String redisPassword, int redisTimeoutSecond
            , JedisPoolConfig jedisPool
            , String redisLeaderboardKey, String redisLeaderboardUpdateCounterKey
            ) {
        this.apiUserHost = apiUserHost;
        this.apiUserPort = apiUserPort;
        this.apiUserSSL = apiUserSSL;
        this.apiUserNettyWorkerThread = apiUserNettyWorkerThread;
        this.apiAdminNettyWorkerThread = apiAdminNettyWorkerThread;
        this.apiAdminHost = apiAdminHost;
        this.apiAdminPort = apiAdminPort;
        this.apiAdminSSL = apiAdminSSL;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisSSL = redisSSL;
        this.redisPassword = redisPassword;
        this.redisTimeoutSecond = redisTimeoutSecond;
        this.jedisPool = jedisPool;

        this.redisLeaderboardKey = redisLeaderboardKey;
        this.redisLeaderboardUpdateCounterKey = redisLeaderboardUpdateCounterKey;
    }

    public LeaderboardServiceConfig()
    {
        createDefaultConfig();
    }

    public String getApiUserHost() {
        return apiUserHost;
    }

    public int getApiUserPort() {
        return apiUserPort;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public static LeaderboardServiceConfig createDefaultConfig()
    {
        JedisPoolConfig defaultJedisPool = new JedisPoolConfig();

        LeaderboardServiceConfig config = new LeaderboardServiceConfig("127.0.0.1", DEFAULT_API_USER_PORT, false, DEFAULT_NETTY_USER_WORKER_THREAD
                , "127.0.0.1", DEFAULT_API_ADMIN_PORT, false, DEFAULT_NETTY_ADMIN_WORKER_THREAD
                , "127.0.0.1", DEFAULT_REDIS_PORT, false, DEFAULT_REDIS_PASSWORD, DEFAULT_REDIS_TIMEOUT_SECOND
                , defaultJedisPool, DEFAULT_REDIS_KEY_LEADERBOARD, DEFAULT_REDIS_KEY_LEADERBOARD_UPDATE);

        return config;
    }

    public int getApiUserNettyWorkerThread() {
        return apiUserNettyWorkerThread;
    }

    public GenericObjectPoolConfig getJedisPool() {
        return jedisPool;
    }

    public String getRedisLeaderboardKey() {
        return redisLeaderboardKey;
    }

    public void setApiUserHost(String apiUserHost) {
        this.apiUserHost = apiUserHost;
    }

    public void setApiUserPort(int apiUserPort) {
        this.apiUserPort = apiUserPort;
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

    public boolean hasApiUserSSL() {
        return apiUserSSL;
    }

    public int getApiAdminNettyWorkerThread() {
        return apiAdminNettyWorkerThread;
    }

    public String getApiAdminHost() {
        return apiAdminHost;
    }

    public int getApiAdminPort() {
        return apiAdminPort;
    }

    public boolean isApiAdminSSL() {
        return apiAdminSSL;
    }

    public boolean isRedisSSL() {
        return redisSSL;
    }


    public static void main(String[] args)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        System.out.println("Default Config: " + (gson.toJson(createDefaultConfig())));
    }
}
