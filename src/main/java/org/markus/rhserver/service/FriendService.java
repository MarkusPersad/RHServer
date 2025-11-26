package org.markus.rhserver.service;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.google.protobuf.ByteString;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.markus.rhserver.components.RedisComponent;
import org.markus.rhserver.constants.Constants;
import org.markus.rhserver.entity.dto.AcceptGroupInput;
import org.markus.rhserver.entity.dto.GetUserInfoInput;
import org.markus.rhserver.entity.dto.GroupSearchInput;
import org.markus.rhserver.entity.vo.FriendList;
import org.markus.rhserver.enums.ResponseCodeEnum;
import org.markus.rhserver.exception.BusinessException;
import org.markus.rhserver.model.*;
import org.markus.rhserver.protobuf.MessageSend;
import org.markus.rhserver.protobuf.MessageTo;
import org.markus.rhserver.protobuf.MessageType;
import org.markus.rhserver.repository.FriendRepository;
import org.markus.rhserver.repository.GroupRepository;
import org.markus.rhserver.repository.MessageRepository;
import org.markus.rhserver.repository.UserRepository;
import org.markus.rhserver.utils.UUIDUtils;
import org.markus.rhserver.websocket.ChannelContextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final RedisComponent redisComponent;
    private final GroupRepository groupRepository;
    private final ChannelContextUtils channelContextUtils;
    private final MessageRepository messageRepository;
    public FriendService(
            FriendRepository friendRepository,
            UserRepository userRepository,
            RedisComponent redisComponent,
            GroupRepository groupRepository,
            ChannelContextUtils channelContextUtils,
            MessageRepository messageRepository
    ){
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.redisComponent = redisComponent;
        this.groupRepository = groupRepository;
        this.channelContextUtils = channelContextUtils;
        this.messageRepository = messageRepository;
    }
    @SaCheckLogin
    public void ApplyFriend(GetUserInfoInput input)throws BusinessException{
        Users user = userRepository.findByEmailOrUserName(input.getInfo(), Fetchers.USERS_FETCHER).getFirst();
        Friend friend = friendRepository.findByUserIdAndFriendId((UUID) StpUtil.getLoginId(),user.uuid());
        if (friend != null && !friend.status()){
            throw new BusinessException(ResponseCodeEnum.FRIEND_APPLY_EXISTS);
        } else if (friend != null && friend.status()){
            throw new BusinessException(ResponseCodeEnum.ALREADY_EXISTS);
        }
        friendRepository.save(FriendDraft.$.produce(
                draft -> {
                    draft.setUserId((UUID) StpUtil.getLoginId());
                    draft.setFriendId(user.uuid());
                    draft.setChatId(UUIDUtils.stitchingUUIDs((UUID) StpUtil.getLoginId(),user.uuid()));
                    draft.setStatus(false);
                    draft.setGroup(false);
                }
        ), SaveMode.INSERT_ONLY);
        MessageSend messageSend = MessageSend.newBuilder()
                .setSender(StpUtil.getLoginId().toString())
                .setReceiver(user.uuid().toString())
                .setTo(MessageTo.USER)
                .setType(MessageType.APPLY)
                .build();
        channelContextUtils.sendMessage(messageSend);
    }
    @Transactional(rollbackFor = Exception.class)
    @SaCheckLogin
    public FriendList getFriendList(){
        Object loginIdObj = StpUtil.getLoginId();
        if (!(loginIdObj instanceof UUID loginId)) {
            throw new IllegalStateException("当前用户登录ID非UUID格式");
        }

        List<Friend> friends = friendRepository.getFriendList(loginId);

        // 提前定义映射关系用于复用
        Function<Friend, UUID> getPeerId = friend ->
                friend.userId().equals(loginId) ? friend.friendId() : friend.userId();

        // 构建需要查询的用户ID和群组ID
        List<UUID> userIds = new ArrayList<>();
        List<UUID> groupIds = new ArrayList<>();

        for (Friend friend : friends) {
            if (friend.group()) {
                groupIds.add(friend.friendId());
            } else {
                userIds.add(getPeerId.apply(friend));
            }
        }
        // 设置Redis缓存（无论是否为空）
        redisComponent.setFriendList(loginId, userIds, groupIds);
        List<Users> users = userRepository.findByIds(userIds, Fetchers.USERS_FETCHER.avatar().userName().email());
        List<Group> groups = groupRepository.findByIds(groupIds, Fetchers.GROUP_FETCHER.groupName().avatar().notice());
        return new FriendList(users, groups);
    }
    @SaCheckLogin
    public void AddGroup(GroupSearchInput input)throws BusinessException{
        Group group = groupRepository.findByName(input.getInfo()).getFirst();
        if (group == null){
            throw new BusinessException(ResponseCodeEnum.NOT_FOUND);
        }
        Friend friend = friendRepository.findByUserIdAndFriendId((UUID) StpUtil.getLoginId(),group.uuid());
        if (friend != null && friend.status()){
            throw new BusinessException(ResponseCodeEnum.ALREADY_EXISTS);
        }
        if (friend != null&& !friend.status()){
            throw new BusinessException(ResponseCodeEnum.FRIEND_APPLY_EXISTS);
        }
        friendRepository.save(FriendDraft.$.produce(
                draft -> {
                    draft.setUserId((UUID) StpUtil.getLoginId());
                    draft.setFriendId(group.uuid());
                    draft.setChatId(UUIDUtils.stitchingUUIDs((UUID) StpUtil.getLoginId(),group.uuid()));
                    draft.setStatus(false);
                    draft.setGroup(true);
                }
        ),SaveMode.INSERT_ONLY);
        MessageSend messageSend = MessageSend.newBuilder()
                .setSender(StpUtil.getLoginId().toString())
                .setReceiver(group.ownerId().toString())
                .setType(MessageType.APPLY)
                .setTo(MessageTo.GROUP)
                .setContent(ByteString.copyFromUtf8(group.uuid().toString()))
                .build();
        channelContextUtils.sendMessage(messageSend);
    }
    @SaCheckLogin
    public void AcceptFriend(GetUserInfoInput input){
        var _ = friendRepository.updateFriendStatus((UUID) StpUtil.getLoginId(),UUID.fromString(input.getInfo()),true);
        messageRepository.save(MessageDraft.$.produce(
                draft -> {
                    draft.setSenderUuid((UUID) StpUtil.getLoginId());
                    draft.setRecipientUuid(UUID.fromString(input.getInfo()));
                    draft.setContent(Constants.FRIEND_SAY_HELLO);
                }
        ),SaveMode.INSERT_ONLY);
        MessageSend messageSend = MessageSend.newBuilder()
                .setSender(StpUtil.getLoginId().toString())
                .setReceiver(input.getInfo())
                .setType(MessageType.TEXT)
                .setContent(ByteString.copyFromUtf8(Constants.FRIEND_SAY_HELLO))
                .setTo(MessageTo.USER)
                .build();
        channelContextUtils.sendMessage(messageSend);
    }
    public void AcceptGroup(AcceptGroupInput input) throws BusinessException {

        Group group = groupRepository.findById(UUID.fromString(input.getGroupId()),Fetchers.GROUP_FETCHER.ownerId());
        if (group != null && group.ownerId() != null && !group.ownerId().equals(StpUtil.getLoginId())) {
            throw new BusinessException(ResponseCodeEnum.FORBIDDEN);
        }
        if (group != null) {
            var _ = friendRepository.updateFriendStatus(group.uuid(),UUID.fromString(input.getApplyId()),true);
        }
        Users user = userRepository.findById(UUID.fromString(input.getApplyId()),Fetchers.USERS_FETCHER.userName());
        messageRepository.save(MessageDraft.$.produce(
                draft -> {
                    draft.setSenderUuid((UUID)StpUtil.getLoginId());
                    assert group != null;
                    draft.setRecipientUuid(group.uuid());
                    assert user != null;
                    draft.setContent(Constants.groupWelcome(user.userName()));
                }
        ),SaveMode.INSERT_ONLY);
        assert group != null;
        assert user != null;
        MessageSend messageSend = MessageSend.newBuilder()
                .setSender(group.ownerId().toString())
                .setReceiver(group.uuid().toString())
                .setType(MessageType.TEXT)
                .setTo(MessageTo.GROUP)
                .setContent(ByteString.copyFromUtf8(Constants.groupWelcome(user.userName())))
                .build();
        channelContextUtils.sendMessage(messageSend);
    }
}
