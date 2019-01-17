package com.zboo.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.CertificateException;

public class LeaderboardService {
    /**
     *
     * */
    LeaderboardServiceConfig config;
    Gson gson;
    Logger logger = LoggerFactory.getLogger(LeaderboardService.class);
    JedisPool jedisPool;
    public LeaderboardService() {
        this.config = LeaderboardServiceConfig.createDefaultConfig();
        this.gson = new GsonBuilder().create();

        logger.info("LeaderboardService.Constructor: {}", gson.toJson(config));
    }


    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;
    static final int MAX_CONTENT_LENGTH = 1024*1024;

    public void start() throws CertificateException, SSLException, InterruptedException {
        this.initJedis();
        this.initNetty();
    }
    public void initNetty() throws CertificateException, SSLException, InterruptedException
    {
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
                    .childHandler(new LeaderboardServiceInitializer(sslCtx, MAX_CONTENT_LENGTH, jedisPool, config.getRedisLeaderboardKey()));

            Channel ch = b.bind(InetAddress.getByName(this.config.getApiHost()),  this.config.getApiPort()).sync().channel();

            logger.info("initNetty: done, web service started on {}:{}"
                , this.config.hasSSL() ? "https" : "http"
                , this.config.getApiHost() + this.config.getApiPort()
            );
        }
        catch (IOException ex){
            logger.error("initNetty: failed!");
            logger.error("initNetty.config: {}", gson.toJson(this.config));
            logger.error("initNetty.exception: {}", ex);
        }
    }
    public void initJedis()
    {
        jedisPool = new JedisPool(this.config.jedisPool, this.config.redisHost);
        logger.info("initJedis: host={}, config={}", this.config.getRedisHost(), this.config.getJedisPool().toString());

    }
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        logger.info("stopped!");
    }

    public LeaderboardServiceConfig getConfig() {
        return config;
    }

    public void setConfig(LeaderboardServiceConfig config) {
        this.config = config;
    }
}
