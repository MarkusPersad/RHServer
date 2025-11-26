package org.markus.rhserver.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.markus.rhserver.components.RedisComponent;
import org.markus.rhserver.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChannelContextUtils {
    private final RedisComponent redisComponent;
    private final UserRepository userRepository;
    private static final Map<UUID, Channel> USER_ID_CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final Map<UUID, ChannelGroup> GROUP_ID_CHANNEL_MAP = new ConcurrentHashMap<>();
    public ChannelContextUtils(
            RedisComponent redisComponent,
            UserRepository userRepository
            ) {
        this.redisComponent = redisComponent;
        this.userRepository = userRepository;
    }
    public void addChannel(UUID userId, Channel channel){
        String channelId = channel.id().toString();
        AttributeKey<Object> attributeKey = null;
        if(AttributeKey.exists("channelId")){
            attributeKey = AttributeKey.newInstance(channelId);
        }else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        List<UUID> groupIds = redisComponent.getGroupList(userId);
        for (UUID groupId : groupIds) {
            add2Group(groupId,channel);
        }
        USER_ID_CHANNEL_MAP.put(userId,channel);
        // 保存心跳
        redisComponent.saveHeartbeat(userId);
        // 更新最后登录时间
        var _ = userRepository.updateLastLogin(userId);
    }
    public void removeChannel(Channel channel){
        Attribute<Object> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        Object userId = attribute.get();
        if(userId instanceof UUID uuid){
            USER_ID_CHANNEL_MAP.remove(uuid);
            redisComponent.removeHeartbeat(uuid);
            var _ = userRepository.updateLastOff(uuid);
        }
    }
    private void add2Group(UUID groupId, Channel channel){
        ChannelGroup channelGroup = GROUP_ID_CHANNEL_MAP.get(groupId);
        if (channelGroup == null){
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_ID_CHANNEL_MAP.put(groupId,channelGroup);
        }
        if (channel == null){
            return;
        }
        channelGroup.add(channel);
    }
}
