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

import java.io.IOException;
import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class LeaderboardServiceHandler extends SimpleChannelInboundHandler<HttpObject> {

    public LeaderboardServiceHandler()
    {
        super();
        gsonParser = new GsonBuilder().create();
    }
    static final String ENDPOINT_POINT = "/point";

    /**
     * For parsing JSON, threadsafe
     * */
    Gson gsonParser;


    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!' };

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
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception{
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;
            logger.debug("channelRead0: msg={}", req);


            logger.debug("channelRead0: uri={}", req.uri());
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if(req.uri().equalsIgnoreCase(ENDPOINT_POINT))
            {
                handlePoint(ctx, req, keepAlive);
            }
            else
            {
                responseNotSupportAndClose(ctx);
            }
        }
        else
        {
            ctx.fireChannelRead(msg);
        }
    }

    static final String NOT_SUPPORTED = "Not supported";
    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static Logger handlerLogger = LoggerFactory.getLogger("LeaderboardHandlerLogger");

    ChannelFutureListener LOG_HANDLER = new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) {
            handlerLogger.debug("Channel closed: {}", future.channel().remoteAddress());
        }
    };

    /**
     * Response not support message and close connection, despite of client keep alive or not
     * */
    public void responseNotSupportAndClose(ChannelHandlerContext ctx)
    {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(NOT_SUPPORTED.getBytes(DEFAULT_CHARSET));

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, buf);
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        ctx.write(response).addListener(ChannelFutureListener.CLOSE).addListener(LOG_HANDLER);
    }

    /**
     * Response not support message and close connection, despite of client keep alive or not
     * */
    public void responseBadRequestAndClose(ChannelHandlerContext ctx, String message)
    {
        ByteBuf buf = ctx.alloc().buffer();
        buf.writeBytes(NOT_SUPPORTED.getBytes(DEFAULT_CHARSET));

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, buf);
        response.headers().set(CONTENT_TYPE, "application/json");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        ctx.write(response).addListener(ChannelFutureListener.CLOSE).addListener(LOG_HANDLER);
    }
    public void responseObject(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive)
    {
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
    public void handlePoint(ChannelHandlerContext ctx, FullHttpRequest msg, boolean keepAlive) {

        if(msg.method() == HttpMethod.GET) {
            try {
                gsonParser.fromJson(msg.content().toString(), LeaderboardPointRequest.class);
            }
            catch(JsonSyntaxException e)
            {
                responseBadRequestAndClose(ctx, "Invalid message");
            }

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
        else if(msg.method() == HttpMethod.PUT)
        {

        }

        logger.info("handlePoint: keepAlive={}", keepAlive);
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
