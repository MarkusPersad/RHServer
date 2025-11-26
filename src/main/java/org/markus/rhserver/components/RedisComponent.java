package org.markus.rhserver.components;

import org.markus.rhserver.constants.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisComponent {
    private final RedisTemplate<String,Object> jsonRedisTemplate;
    @Value("${sa-token.timeout}")
    private long expireTime;
    @Value("${ws-netty.heartbeat-interval}")
    private long heartbeatTime;
    public RedisComponent(RedisTemplate<String,Object> jsonRedisTemplate){
        this.jsonRedisTemplate = jsonRedisTemplate;
    }
    public void setFriendList(UUID userId, List<UUID> userIds, List<UUID> groupIds){
    // 参数校验
    if (userId == null) {
        throw new IllegalArgumentException("userId cannot be null");
    }

    try {
        // 处理用户好友列表
        processFriendList(Constants.FRIENDLIST_USER, userId, userIds);
        // 处理群组好友列表
        processFriendList(Constants.FRIENDLIST_GROUP, userId, groupIds);
    } catch (Exception e) {
        // 记录日志或进行适当的异常处理
        throw new RuntimeException("Failed to set friend list for user: " + userId, e);
    }
}

private void processFriendList(String prefix, UUID userId, List<UUID> ids) {
    if (ids == null) {
        return;
    }

    String key = prefix + userId.toString();

    // 避免重复调用boundListOps
    BoundListOperations<String, Object> listOps = jsonRedisTemplate.boundListOps(key);
    listOps.leftPushAll(ids);
    listOps.expire(expireTime, TimeUnit.SECONDS);
}

    public List<UUID> getFriendList(UUID userId){
        List<Object> objects = jsonRedisTemplate.boundListOps(Constants.FRIENDLIST_USER + userId.toString()).range(0,-1);
        assert objects != null;
        return objects.stream().map(o -> {
            if (o instanceof UUID uuid){
                return uuid;
            } else if (o instanceof String uuid){
                return UUID.fromString(uuid);
            } else {
                throw new IllegalArgumentException("Unexpected type: " + o.getClass());
            }
        }).toList();
    }
    public List<UUID> getGroupList(UUID userId){
        List<Object> objects = jsonRedisTemplate.boundListOps(Constants.FRIENDLIST_GROUP + userId.toString()).range(0,-1);
        assert objects != null;
        return objects.stream()
                .map(o -> {
                    if (o instanceof UUID uuid){
                        return uuid;
                    } else if (o instanceof String uuid){
                        return UUID.fromString(uuid);
                    } else {
                        throw new IllegalArgumentException("Unexpected type: " + o.getClass());
                    }
                }).toList();
    }
    public void saveHeartbeat(UUID userId){
        jsonRedisTemplate.opsForValue().set(Constants.HEARTBEAT + userId.toString(),System.currentTimeMillis(),heartbeatTime);
    }
    public void removeHeartbeat(UUID userId){
        jsonRedisTemplate.delete(Constants.HEARTBEAT + userId.toString());
    }
}
