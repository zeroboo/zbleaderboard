package com.zboo.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zboo.leaderboard.message.LeaderboardPointNotification;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LeaderboardService {
    /**
     *
     * */
    static final String EMPTY_STRING = "";
    LeaderboardServiceConfig config;
    Gson gson;
    Logger logger = LoggerFactory.getLogger(LeaderboardService.class);
    JedisPool jedisPool;
    AbstractAuthorizeService authorizeService;
    ConcurrentHashMap<String, LeaderboardUser> onlineUsers;
    ConcurrentHashMap<String, LeaderboardPointNotification> updatedPoints;
    ScheduledExecutorService notifyServiceScheduler;
    NofityUpdatePointRunner notifyRunner;
    Charset charset;
    public LeaderboardService() {
        this.config = LeaderboardServiceConfig.createDefaultConfig();
        this.gson = new GsonBuilder().create();
        this.authorizeService = new WelcomeAuthorizeService();
        this.onlineUsers = new ConcurrentHashMap<>();
        this.updatedPoints = new ConcurrentHashMap<>();
        this.notifyServiceScheduler = Executors.newScheduledThreadPool(1);

        this.notifyRunner = new NofityUpdatePointRunner(this);

        this.notifyServiceScheduler.scheduleAtFixedRate(this.notifyRunner, 0, 10, TimeUnit.SECONDS);
        this.charset = Charset.forName("UTF-8");

    }


    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;
    static final int MAX_CONTENT_LENGTH = 1024 * 1024;

    public void start() throws CertificateException, SSLException, InterruptedException {
        logger.info("Start with config: {}", gson.toJson(config));
        this.initJedis();
        this.initNetty();
    }

    public void initNetty() throws CertificateException, SSLException, InterruptedException {
        // Configure SSL.
        final SslContext sslCtx;
        if (this.config.hasSSL()) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(this.config.getNettyWorkerThread());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new LeaderboardServiceInitializer(sslCtx, MAX_CONTENT_LENGTH, this));

            Channel ch = b.bind(InetAddress.getByName(this.config.getApiHost()), this.config.getApiPort()).sync().channel();

            logger.info("initNetty: done, web service started on {}:{}"
                    , this.config.hasSSL() ? "https" : "http"
                    , this.config.getApiHost() + this.config.getApiPort()
            );
        } catch (IOException ex) {
            logger.error("initNetty: failed!");
            logger.error("initNetty.config: {}", gson.toJson(this.config));
            logger.error("initNetty.exception: {}", ex);
        }
    }

    public void initJedis() {
        if(config.getRedisPassword()!=null && !config.getRedisPassword().isEmpty())
        {
            jedisPool = new JedisPool(this.config.jedisPool
                    , this.config.getRedisHost()
                    , this.config.getRedisPort()
                    , this.config.getRedisTimeoutSecond()
                    , this.config.getRedisPassword());
        }
        else
        {
            jedisPool = new JedisPool(this.config.jedisPool
                    , this.config.getRedisHost()
                    , this.config.getRedisPort()
                    , this.config.getRedisTimeoutSecond()
                    );
        }
        logger.info("initJedis: host={}, config={}", this.config.getRedisHost(), this.config.getJedisPool().toString());
        logger.info("initJedis: leaderboardKey={}", this.config.getRedisLeaderboardKey());

    }

    public void stop() throws InterruptedException {
        logger.info("closing boss group");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        logger.info("closing worker group");
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("closing jedis");
        this.jedisPool.close();
        logger.info("closing notify scheduler");
        this.notifyServiceScheduler.shutdown();
        logger.info("stopped!");
    }

    public LeaderboardServiceConfig getConfig() {
        return config;
    }

    public void setConfig(LeaderboardServiceConfig config) {
        this.config = config;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void addNewUser(LeaderboardUser user) {
        this.onlineUsers.put(user.getUsername(), user);
        logger.info("newUser: ownerUser={}, remote={}, online={}", user.getUsername(), user.getContext().channel().remoteAddress(), this.onlineUsers.size());
    }

    public void removeUser(String username) {
        LeaderboardUser user = this.onlineUsers.remove(username);
        logger.info("removeUser: ownerUser={}, removed={}, online={}"
                , username
                , user!=null
                , this.onlineUsers.size());
    }

    public void onHandleNewPointUpdated(String username, long currentPoint, int currentRank)
    {
        LeaderboardPointNotification noti = new LeaderboardPointNotification(username, currentPoint, currentRank);
        this.updatedPoints.put(username, noti);
        logger.info("onHandleNewPointUpdated: ownerUser={}, currentPoint={}, currentRank={}, onlineUsers={}, updatedPoints={}"
                , username
                , currentPoint
                , currentRank
                , this.onlineUsers.size()
                , this.updatedPoints.size());
    }

    public Charset getCharset() {
        return charset;
    }

    public ConcurrentHashMap<String, LeaderboardUser> getOnlineUsers() {
        return onlineUsers;
    }

    public ConcurrentHashMap<String, LeaderboardPointNotification> getUpdatedPoints() {
        return updatedPoints;
    }
}
