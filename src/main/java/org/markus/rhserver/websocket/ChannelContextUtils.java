package org.markus.rhserver.websocket;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.markus.rhserver.components.RedisComponent;
import org.markus.rhserver.protobuf.MessageSend;
import org.markus.rhserver.protobuf.MessageTo;
import org.markus.rhserver.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelContextUtils {
    private final RedisComponent redisComponent;
    private final RabbitTemplate rabbitTemplate;
    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;
    private static final Map<UUID, Channel> USER_ID_CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, ChannelGroup> GROUP_ID_CHANNEL_MAP = new ConcurrentHashMap<>();
    public ChannelContextUtils(
            RedisComponent redisComponent,
            RabbitTemplate vrabbitTemplate
            ) {
        this.redisComponent = redisComponent;
        this.rabbitTemplate = vrabbitTemplate;
    }
    /**
     * 添加用户通道并初始化相关配置
     * @param userId 用户唯一标识符
     * @param channel 通信通道对象
     */
    public void addChannel(UUID userId, Channel channel){
        String channelId = channel.id().toString();
        AttributeKey<Object> attributeKey = null;
        // 获取或创建通道属性键
        if(AttributeKey.exists("channelId")){
            attributeKey = AttributeKey.newInstance(channelId);
        }else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        // 将用户加入所有群组
        List<UUID> groupIds = redisComponent.getGroupList(userId);
        for (UUID groupId : groupIds) {
            add2Group(groupId,channel);
        }
        USER_ID_CHANNEL_MAP.put(userId,channel);
        // 保存心跳
        redisComponent.saveHeartbeat(userId);
    }

    /**
     * 移除指定的通道连接，并清理相关的用户状态信息
     *
     * @param userId 需要移除的通道对象
     */
    public void removeChannel(Object userId){

        // 如果用户ID存在且为UUID类型，则执行清理操作
        if(userId instanceof UUID uuid){
            Channel channel = USER_ID_CHANNEL_MAP.get(uuid);
            if (channel == null){
                return;
            }
            // 从用户ID通道映射表中移除该用户
            USER_ID_CHANNEL_MAP.remove(uuid);

            // 从Redis中移除该用户的心跳记录
            redisComponent.removeHeartbeat(uuid);

            // 获取用户所在的所有群组，并将用户从这些群组中移除
            List<UUID> groupIds = redisComponent.getGroupList(uuid);
            for (UUID groupId : groupIds) {
                removeFromGroup(groupId,channel);
            }
        }
    }

    /**
     * 发送消息到指定的目标
     * 根据消息目标类型判断是发送给群组还是单个用户，并调用相应的发送方法
     *
     * @param messageSend 消息发送对象，包含消息内容和目标信息
     */
    public void sendMessage(MessageSend messageSend){
        // 根据消息目标类型选择发送方式
        if (messageSend.getTo().equals(MessageTo.GROUP)){
            sendToGroup(messageSend);
        } else {
            sendToUser(messageSend);
        }
    }

    /**
     * 将指定的通道添加到对应的通道组中
     * @param groupId 通道组的唯一标识符
     * @param channel 需要添加到通道组的通道对象
     */
    private void add2Group(UUID groupId, Channel channel){
        // 获取指定ID的通道组
        ChannelGroup channelGroup = GROUP_ID_CHANNEL_MAP.get(groupId);
        if (channelGroup == null){
            // 如果通道组不存在，则创建新的通道组并存入映射表
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_ID_CHANNEL_MAP.put(groupId,channelGroup);
        }
        // 如果通道为空，则直接返回
        if (channel == null){
            return;
        }
        // 将通道添加到通道组中
        channelGroup.add(channel);
    }

    /**
     * 向指定用户发送消息
     * @param messageSend 要发送的消息对象，包含接收者信息和消息内容
     */
    private void sendToUser(MessageSend messageSend){
        // 根据消息中的接收者ID获取对应的用户通道
        UUID receiverId =UUID.fromString( messageSend.getReceiver());
        Channel userChannel = USER_ID_CHANNEL_MAP.get(receiverId);
        if (userChannel == null) {
            rabbitTemplate.convertAndSend(exchangeName,receiverId.toString(),messageSend);
            return;
        }
        // 通过WebSocket通道向用户发送二进制消息帧
        userChannel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(messageSend.toByteArray())));
    }

    /**
     * 发送消息到指定群组
     * @param messageSend 要发送的消息对象，包含接收者信息和消息内容
     */
    private void sendToGroup(MessageSend messageSend){
        // 从消息接收者字段解析群组ID
        UUID groupId =UUID.fromString( messageSend.getReceiver());
        // 根据群组ID获取对应的ChannelGroup
        ChannelGroup channelGroup = GROUP_ID_CHANNEL_MAP.get(groupId);
        if (channelGroup == null) {
            rabbitTemplate.convertAndSend(exchangeName,groupId.toString(),messageSend);
            return;
        }
        // 将消息转换为二进制格式并通过WebSocket发送给群组中的所有成员
        channelGroup.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(messageSend.toByteArray())));
    }

        /**
     * 从指定的组中移除通道
     *
     * @param groupId 组的唯一标识符
     * @param channel 要移除的通道
     */
    private void removeFromGroup(UUID groupId, Channel channel){
        // 获取指定ID的通道组
        ChannelGroup channelGroup = GROUP_ID_CHANNEL_MAP.get(groupId);
        if (channelGroup == null) {
            return;
        }
        // 从通道组中移除指定通道
        channelGroup.remove(channel);
        // 如果通道组为空，则从映射中移除该组
        if (channelGroup.isEmpty())
            GROUP_ID_CHANNEL_MAP.remove(groupId);
    }
}
