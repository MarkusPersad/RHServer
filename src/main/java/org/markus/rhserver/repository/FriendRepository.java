package org.markus.rhserver.repository;

import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.markus.rhserver.model.Fetchers;
import org.markus.rhserver.model.Friend;
import org.markus.rhserver.model.FriendTable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class FriendRepository extends AbstractJavaRepository<Friend, UUID> {
    private static final FriendTable table = FriendTable.$;
    public FriendRepository(JSqlClient jSqlClient) {
        super(jSqlClient);
    }
        /**
     * 根据用户ID和好友ID查找好友关系记录
     *
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 返回匹配的好友关系记录，如果不存在则返回null
     */
    public Friend findByUserIdAndFriendId(UUID userId, UUID friendId){
        // 构造查询条件：用户ID和好友ID可以互换位置，同时排除群组关系和已删除记录
        return sql.createQuery(table)
                .where(Predicate.or(
                        Predicate.and(table.userId().eq(userId),table.friendId().eq(friendId)),
                        Predicate.and(table.userId().eq(friendId),table.friendId().eq(userId))
                ))
                .where(table.group().eq(false))
                .where(table.deleteAt().isNull())
                .select(table.fetch(Fetchers.FRIEND_FETCHER
                        .chatId()
                        .userId()
                        .friendId()
                        .status()
                )).execute().getFirst();
    }

        /**
     * 根据用户ID获取好友列表
     *
     * @param userId 用户唯一标识符
     * @return 返回指定用户的好友列表，包含所有状态为有效且未删除的好友关系
     */
    public List<Friend> getFriendList(UUID userId){
        // 查询好友关系表，筛选条件：用户ID匹配且好友关系状态为有效且未删除
        return sql.createQuery(table)
                .where(Predicate.or(table.userId().eq(userId),table.friendId().eq(userId)))
                .where(table.status().eq( true))
                .where(table.deleteAt().isNull())
                .select(table.fetch(Fetchers.FRIEND_FETCHER
                        .group()
                        .userId()
                        .friendId()
                )).execute();
    }
    public Integer updateFriendStatus(UUID userId, UUID friendId, boolean status){
        // 构造更新条件：用户ID和好友ID可以互换位置
        return sql.createUpdate(table)
                .where(Predicate.or(
                        Predicate.and(table.userId().eq(friendId),table.friendId().eq(userId)),
                        Predicate.and(table.userId().eq(userId),table.friendId().eq(friendId))
                ))
                .set(table.status(),status)
                .execute();
    }

}
