/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.zboo.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class LeaderboardServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    static final String ENDPOINT_POINT = "/point";
    static final String ENDPOINT_LOGIN = "/login";
    static final String VALID_USERNAME_MATCHER = "[a-zA-Z0-9.\\-_]{4,}";
    static final String DEFAULT_BAD_REQUEST_MESSAGE = "Invalid message";
    static final String EMPTY_STRING = "";
    static final String NOT_SUPPORTED = "Not supported";
    static final String CONTENT_TYPE_JSON = "application/json";
    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static Logger handlerLogger = LoggerFactory.getLogger("LeaderboardHandlerLogger");

    /**
     * For parsing JSON, threadsafe
     */
    Gson gsonParser;

    /**
     * Pool of jedis objects
     */
    JedisPool jedisPool;
    String leaderboardKey;
    String username = EMPTY_STRING;
    LeaderboardService owner = null;
    public LeaderboardServiceHandler(LeaderboardService owner) {
        super();
        this.gsonParser = new GsonBuilder().create();
        this.jedisPool = owner.getJedisPool();
        this.owner = owner;
        this.leaderboardKey = owner.getConfig().getRedisLeaderboardKey();
    }

    private static final byte[] CONTENT = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!'};

    private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
    private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
    private static final AsciiString CONNECTION = AsciiString.cached("Connection");
    private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
    private Logger logger = LoggerFactory.getLogger(LeaderboardServiceHandler.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        ///logger.debug("channelRead0: msg={}", req);


        logger.debug("channelRead0: uri={}", req.uri());
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        if (req.uri().equalsIgnoreCase(ENDPOINT_POINT)) {
            handlePointGet(ctx, req, keepAlive);
        }
        else if (req.uri().equalsIgnoreCase(ENDPOINT_LOGIN)){
            handleLogin(ctx, req, keepAlive);
        }
        else
        {
            responseNotSupportAndClose(ctx);
        }
    }


    ChannelFutureListener LOG_HANDLER = new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) {
            handlerLogger.debug("Channel closed: {}", future.channel().remoteAddress());
        }
    };

    /**
     * Response not support message and close connection, despite of client keep alive or not
     */
    public void responseNotSupportAndClose(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(NOT_SUPPORTED.getBytes(DEFAULT_CHARSET));

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, buf);
        response.headers().set(CONTENT_TYPE, CONTENT_TYPE_JSON);
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        ctx.write(response).addListener(ChannelFutureListener.CLOSE).addListener(LOG_HANDLER);
    }

    /**
     * Response not support message and close connection, despite of client keep alive or not
     */
    public void responseBadRequestAndClose(ChannelHandlerContext ctx, String message) {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(message.getBytes(DEFAULT_CHARSET));

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, buf);
        response.headers().set(CONTENT_TYPE, CONTENT_TYPE_JSON);
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        ctx.write(response).addListener(ChannelFutureListener.CLOSE).addListener(LOG_HANDLER);
    }

    public void responseObject(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(response);
        }
    }

    public static boolean isUsernameValid(String username) {
        return username != null && !username.isEmpty() && username.matches(VALID_USERNAME_MATCHER);
    }

    public static boolean isUserPointValid(long value) {
        return value >= 0;
    }
    public void handleLogin(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleLogin.start: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }

        boolean validRequest = false;
        LeaderboardRequest request = null;
        String content = msg.content().toString(DEFAULT_CHARSET);
        if (msg.method() == HttpMethod.GET) {

            ///Validating request:
            validRequest = false;
            try {
                request = gsonParser.fromJson(content, LeaderboardRequest.class);
                if(request != null) {
                    if (isUsernameValid(request.username)) {
                        validRequest = true;
                    } else {
                        logger.info("handleLogin: invalid username, submit={}", request.username);
                    }
                }
                else
                {
                    logger.debug("handleLogin: content null: {}", content);
                }
            } catch (JsonSyntaxException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("handleLogin: content is not json, {}, {}", content, e.getMessage());
                }
            }

            if (validRequest) {
                logger.info("handleLogin: SUCCESS");
                LeaderboardLoginResponse resp = LeaderboardMessageFactory.createLeaderboardLoginResponse();
                resp.setUsername(request.username);
                resp.setSuccess(true);
                ///Create new user
                LeaderboardUser user = new LeaderboardUser(request.username, ctx);
                username = request.username;
                this.owner.addNewUser(user);

                responseJson(ctx, keepAlive, resp);

            } else {
                responseBadRequestAndClose(ctx, DEFAULT_BAD_REQUEST_MESSAGE);
                logger.info("handleLogin: invalidRequest");
            }
        } else if (msg.method() == HttpMethod.PUT) {
            ///Create or update newPoint on server
            handlePointPut(ctx, msg, keepAlive);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("handleLogin.finish: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }
    }
    public void handlePointGet(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive) {
        if (logger.isDebugEnabled()) {
            logger.debug("handlePointGet.start: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }

        boolean validRequest = false;
        LeaderboardPointRequest request = null;
        String content = msg.content().toString(DEFAULT_CHARSET);
        if (msg.method() == HttpMethod.GET) {

            ///Validating request:
            validRequest = false;
            try {
                request = gsonParser.fromJson(content, LeaderboardPointRequest.class);
                if (isUsernameValid(request.username)) {
                    validRequest = true;
                } else {
                    logger.info("handlePointGET: invalid username, submit={}", request.username);
                }
            } catch (JsonSyntaxException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("handlePointGET: content is not json, {}", content);
                }
            }

            if (validRequest) {
                ///Query by redis pipeline for user newPoint
                Jedis jedis = this.jedisPool.getResource();
                Pipeline pipeline = jedis.pipelined();
                Response<Double> currentPointResponse = pipeline.zscore(this.leaderboardKey, request.username);
                Response<Long> currentRankResponse = pipeline.zrank(this.leaderboardKey, request.username);
                pipeline.sync();
                jedis.close();

                ///Prepare response
                LeaderboardPointResponse resp = LeaderboardMessageFactory.createLeaderboardPointResponse();
                resp.setSuccess(true);
                resp.setUsername(request.username);
                resp.setCurrentPoint(currentPointResponse.get().longValue());
                resp.setCurrentRank(redisRankToUserRank(currentRankResponse.get().longValue()));

                logger.info("handlePointGET: SUCCESS");
                responseJson(ctx, keepAlive, resp);

            } else {
                responseBadRequestAndClose(ctx, DEFAULT_BAD_REQUEST_MESSAGE);
                logger.info("handlePointGET: invalidRequest");
            }
        } else if (msg.method() == HttpMethod.PUT) {
            ///Create or update newPoint on server
            handlePointPut(ctx, msg, keepAlive);

        }

        if (logger.isDebugEnabled()) {
            logger.debug("handlePointGET.finish: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }
    }
    public static int redisRankToUserRank(long value)
    {
        return (int)value+1;
    }
    /**
     * Handler for /point PUT endpoint
     * Create or update user point to leaderboard
     */
    public void handlePointPut(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive) {
        if (logger.isDebugEnabled()) {
            logger.debug("handlePointPut.start: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }

        boolean validRequest = false;
        LeaderboardPointRequest request = null;
        ///Validating request:
        String content = msg.content().toString(DEFAULT_CHARSET);
        validRequest = false;
        try {

            request = gsonParser.fromJson(content, LeaderboardPointRequest.class);
            if (isUsernameValid(request.username)) {
                if (isUserPointValid(request.newPoint)) {
                    validRequest = true;
                } else {
                    logger.info("handlePointPut: invalid point, submit={}", request.newPoint);
                }
            } else {
                logger.info("handlePointPut: invalid username, submit={}", request.username);
            }
        } catch (JsonSyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("handlePointPut: content is not json, {}", content);
            }
        }

        if (validRequest) {
            ///Prepare jedis
            Jedis jedis = this.jedisPool.getResource();
            ///Query by pipeline
            Pipeline pipeline = jedis.pipelined();
            Response<Long> totalRank=pipeline.zadd(this.leaderboardKey, request.newPoint, request.username);
            Response<Double> currentPointResponse = pipeline.zscore(this.leaderboardKey, request.username);
            Response<Long> currentRankResponse = pipeline.zrank(this.leaderboardKey, request.username);
            pipeline.close();
            pipeline.sync();
            jedis.close();

            ///Prepare response
            LeaderboardPointResponse resp = LeaderboardMessageFactory.createLeaderboardPointResponse();
            resp.setSuccess(true);
            resp.setUsername(request.username);
            resp.setCurrentPoint(currentPointResponse.get().longValue());
            resp.setCurrentRank(redisRankToUserRank(currentRankResponse.get().longValue()));

            ///Response client
            responseJson(ctx, keepAlive, resp);

            ///Notify service that new point updated
            this.owner.onHandleNewPointUpdated(request.username, resp.getCurrentPoint(), resp.getCurrentRank());

            logger.info("handlePointPut: SUCCESS, username={},newPoint={}, totalRank={}", request.username, request.newPoint, totalRank.get());
        } else {
            responseBadRequestAndClose(ctx, DEFAULT_BAD_REQUEST_MESSAGE);
            ///logger.info("handlePointGet.GET: invalidRequest");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("handlePointPut.finish: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }
    }

    /***
     * Return content for valid api request as json.
     * Aware of client keep-alive.
     */
    public void responseJson(ChannelHandlerContext ctx, boolean keepAlive, Object content)
    {
        ByteBuf buf = ctx.alloc().buffer();
        String json = gsonParser.toJson(content);
        buf.writeBytes(json.getBytes(DEFAULT_CHARSET));

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, buf);
        httpResponse.headers().set(CONTENT_TYPE, CONTENT_TYPE_JSON);
        httpResponse.headers().setInt(CONTENT_LENGTH, httpResponse.content().readableBytes());

        if (!keepAlive) {
            ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
        } else {
            httpResponse.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.write(httpResponse);
        }
    }

    public void handleLeaderboard(ChannelHandlerContext ctx, HttpObject msg) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public boolean acceptInboundMessage(Object msg) throws Exception {
        return (msg instanceof FullHttpRequest);
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: remote={}", ctx.channel().remoteAddress());
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelInactive: remote={}", ctx.channel().remoteAddress());
        if(this.username!=null && !username.isEmpty())
        {
            this.owner.removeUser(this.username);
        }
    }
}
