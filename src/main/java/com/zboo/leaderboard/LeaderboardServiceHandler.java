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
import com.google.gson.internal.LinkedTreeMap;
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

import java.io.IOException;
import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class LeaderboardServiceHandler extends SimpleChannelInboundHandler<HttpObject> {
    static final String ENDPOINT_POINT = "/point";
    static final String VALID_USERNAME_MATCHER = "[a-zA-Z0-9.\\-_]{4,}";
    static final String DEFAULT_BAD_REQUEST_MESSAGE = "Invalid message";
    static final String EMPTY_STRING = "";
    static final String NOT_SUPPORTED = "Not supported";
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
    public LeaderboardServiceHandler(JedisPool jedisPool, String leaderboardKey) {
        super();
        this.gsonParser = new GsonBuilder().create();
        this.jedisPool = jedisPool;
        this.leaderboardKey = leaderboardKey;
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
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            ///logger.debug("channelRead0: msg={}", req);


            logger.debug("channelRead0: uri={}", req.uri());
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if (req.uri().equalsIgnoreCase(ENDPOINT_POINT)) {
                handlePoint(ctx, req, keepAlive);
            } else {
                responseNotSupportAndClose(ctx);
            }
        } else {
            ctx.fireChannelRead(msg);
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
        response.headers().set(CONTENT_TYPE, "application/json");
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
        response.headers().set(CONTENT_TYPE, "application/json");
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

    public void handlePoint(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive) {
        if (logger.isDebugEnabled()) {
            logger.debug("handlePoint.start: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
        }

        boolean validRequest = false;
        LeaderboardPointRequest request = null;
        if (msg.method() == HttpMethod.GET) {

            ///Validating request:
            validRequest = false;
            try {
                request = gsonParser.fromJson(msg.content().toString(), LeaderboardPointRequest.class);
                if (isUsernameValid(request.username)) {
                    validRequest = true;
                } else {
                    logger.info("handlePoint: invalid username, submit={}", request.username);
                    responseBadRequestAndClose(ctx, DEFAULT_BAD_REQUEST_MESSAGE);
                }
            } catch (JsonSyntaxException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("HandlePoint.GET: content is not json, {}", msg.content().toString());
                }
            }


            if (validRequest) {
                ///Query redis for user points
                Jedis jedis = this.jedisPool.getResource();
                long point = jedis.zscore(this.leaderboardKey, request.username).longValue();

                LeaderboardPointResponse resp = LeaderboardResponse.createLeaderboardPointResponse();
                resp.setSuccess(true);
                resp.setUsername(request.username);
                resp.setCurrentPoint(point);

                logger.info("handlePoint.GET: SUCCESS");


                FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
                httpResponse.headers().set(CONTENT_TYPE, "text/plain");
                httpResponse.headers().setInt(CONTENT_LENGTH, httpResponse.content().readableBytes());

                if (!keepAlive) {
                    ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    httpResponse.headers().set(CONNECTION, KEEP_ALIVE);
                    ctx.write(httpResponse);
                }

            } else {
                responseBadRequestAndClose(ctx, DEFAULT_BAD_REQUEST_MESSAGE);
                logger.info("handlePoint.GET: invalidRequest");
            }

        } else if (msg.method() == HttpMethod.PUT) {

        }

        if (logger.isDebugEnabled()) {
            logger.debug("handlePoint.finish: keepAlive={}, remote={}", keepAlive, ctx.channel().remoteAddress());
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
}
