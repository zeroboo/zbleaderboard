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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class LeaderboardServiceInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    int maxContentLength = 0;
    JedisPool jedisPool;
    String leaderboardKey = null;
    public LeaderboardServiceInitializer(SslContext sslCtx, int maxContentLength, JedisPool jedisPool, String leaderboardKey) {
        this.sslCtx = sslCtx;
        this.maxContentLength = maxContentLength;
        this.jedisPool = jedisPool;
        this.leaderboardKey = leaderboardKey;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpResponseEncoder());
        p.addLast(new HttpObjectAggregator(maxContentLength));
        p.addLast(new HttpServerExpectContinueHandler());

        p.addLast(new LeaderboardServiceHandler(jedisPool, leaderboardKey));
    }
}
