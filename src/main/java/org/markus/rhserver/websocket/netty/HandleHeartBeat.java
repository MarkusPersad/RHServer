package org.markus.rhserver.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.markus.rhserver.protobuf.MessageSend;
import org.markus.rhserver.protobuf.MessageType;
import org.markus.rhserver.websocket.ChannelContextUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@ChannelHandler.Sharable
public class HandleHeartBeat extends ChannelDuplexHandler {

    private final ChannelContextUtils channelContextUtils;

    public HandleHeartBeat(
            ChannelContextUtils channelContextUtils
    ){
        this.channelContextUtils = channelContextUtils;
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent){
            Channel channel = ctx.channel();
            UUID userId =(UUID) channel.attr(AttributeKey.valueOf(channel.id().toString())).get();
            if (idleStateEvent.state().equals(IdleState.READER_IDLE)){
                log.info("用户 {} 掉线了", idleStateEvent.state());
                ctx.close();
                channelContextUtils.removeChannel(userId);
            } else if (idleStateEvent.state().equals(IdleState.WRITER_IDLE)){
                channelContextUtils.sendMessage(
                        MessageSend.newBuilder()
                                .setReceiver(userId.toString())
                                .setType(MessageType.HEARTBEAT)
                                .build()
                );
                log.info("向用户发送心跳包");
            }else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
