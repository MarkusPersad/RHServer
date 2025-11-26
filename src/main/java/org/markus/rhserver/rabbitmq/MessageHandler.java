package org.markus.rhserver.rabbitmq;

import org.markus.rhserver.protobuf.MessageSend;
import org.markus.rhserver.websocket.ChannelContextUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("rabbitMessageHandler")
public class MessageHandler implements MessageListener {

    private final MessageConverter messageConverter;
    private final ChannelContextUtils channelContextUtils;

    public MessageHandler(
            MessageConverter messageConverter,
            ChannelContextUtils channelContextUtils
    ){
        this.messageConverter = messageConverter;
        this.channelContextUtils = channelContextUtils;
    }

    @Override
    public void onMessage(Message message) {
        MessageSend messageSend = (MessageSend) messageConverter.fromMessage(message);
        channelContextUtils.sendMessage(messageSend);
    }

    @Override
    public void containerAckMode(AcknowledgeMode mode) {
        MessageListener.super.containerAckMode(mode);
    }

    @Override
    public boolean isAsyncReplies() {
        return MessageListener.super.isAsyncReplies();
    }

    @Override
    public void onMessageBatch(List<Message> messages) {
        MessageListener.super.onMessageBatch(messages);
    }
}
