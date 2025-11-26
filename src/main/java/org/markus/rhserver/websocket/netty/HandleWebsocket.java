package org.markus.rhserver.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.markus.rhserver.components.RedisComponent;
import org.markus.rhserver.protobuf.MessageSend;
import org.markus.rhserver.protobuf.MessageType;
import org.markus.rhserver.websocket.ChannelContextUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@ChannelHandler.Sharable
public class HandleWebsocket extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {

    private final RedisComponent  redisComponent;
    private final ChannelContextUtils channelContextUtils;

    public HandleWebsocket(
            RedisComponent redisComponent,
            ChannelContextUtils channelContextUtils
    ) {
        this.redisComponent = redisComponent;
        this.channelContextUtils = channelContextUtils;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BinaryWebSocketFrame binaryWebSocketFrame) throws Exception {
        byte[] body = binaryWebSocketFrame.content().array();
        MessageSend message = MessageSend.parseFrom(body);
        if (message.getType().equals(MessageType.OFFLINE)){
            channelContextUtils.removeChannel(UUID.fromString(message.getSender()));
        }
        channelContextUtils.sendMessage( message);
        Channel channel = channelHandlerContext.channel();
        Attribute<UUID> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
       UUID userId = attr.get();
       if (userId != null) {
           redisComponent.saveHeartbeat(userId);
       }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // 清理连接资源
        Channel channel = ctx.channel();
        Attribute<UUID> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        UUID userId = attr.get();
        if (userId != null) {
            channelContextUtils.removeChannel(userId);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete){
            String url = handshakeComplete.requestUri();
            String userId = getUserUUID(url);
            if (userId == null || userId.isEmpty()){
                ctx.channel().close();
                return;
            }
            channelContextUtils.addChannel(UUID.fromString(userId), ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }
    
    private String getUserUUID(String url){
        if (url == null || !url.contains("userId=")){
            return null;
        }
        String[] split = url.split("\\?");
        if (split.length < 2){
            return null;
        }
        String[] params = split[1].split("=");
        if (params.length < 2 || !"userId".equals(params[0])){
            return null;
        }
        return params[1];
    }
}
