package com.zboo.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zboo.leaderboard.message.LeaderboardPointNotification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static com.zboo.leaderboard.LeaderboardServiceHandler.CONTENT_TYPE_JSON;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class NofityUpdatePointRunner implements Runnable {
    Logger logger = LoggerFactory.getLogger(NofityUpdatePointRunner.class);

    Gson gson;
    LeaderboardService owner;

    public NofityUpdatePointRunner(LeaderboardService owner) {
        super();
        this.owner = owner;

        this.gson = new GsonBuilder().create();
    }

    @Override
    public void run() {
        logger.info("Run, onlineUsers={}, newPoint={}", owner.getOnlineUsers().size(), owner.getUpdatedPoints().size());

        ///Prepare response: collect all points entry
        Iterator<Map.Entry<String, LeaderboardPointNotification>> itPoints = owner.getUpdatedPoints().entrySet().iterator();
        ArrayList<LeaderboardPointNotification> points = new ArrayList<>();
        while (itPoints.hasNext()) {
            points.add(itPoints.next().getValue());
            itPoints.remove();
        }
        logger.info("pointList={}", points.size());
        if(points.size() > 0) {
            String notifyMessage = gson.toJson(points);
            ///Notify all users
            Iterator<Map.Entry<String, LeaderboardUser>> itUser = owner.getOnlineUsers().entrySet().iterator();

            int sent = 0;
            int total = 0;

            while (itUser.hasNext()) {
                Map.Entry<String, LeaderboardUser> entry = itUser.next();
                if (responseClient(entry.getKey(), entry.getValue().getContext(), notifyMessage)) {
                    sent++;
                }
                total++;
            }
            logger.info("pointList={}, sent={}, total={}", points.size(), sent, total);
        }

    }

    public boolean responseClient(String username, ChannelHandlerContext ctx, String content) {
        boolean sent = false;
        if (ctx != null && ctx.channel().isActive()) {
            ByteBuf buf = ctx.alloc().buffer();
            buf.writeBytes(content.getBytes(owner.getCharset()));

            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, buf);
            httpResponse.headers().set(CONTENT_TYPE, CONTENT_TYPE_JSON);
            httpResponse.headers().setInt(CONTENT_LENGTH, httpResponse.content().readableBytes());

            httpResponse.headers().set(CONNECTION, KEEP_ALIVE);
            ctx.writeAndFlush(httpResponse);
            sent = true;
        } else {
            logger.info("sentFail: ownerUser={}");
        }
        return sent;
    }
}
