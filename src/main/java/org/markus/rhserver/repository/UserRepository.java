package org.markus.rhserver.repository;

import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.markus.rhserver.entity.dto.UserInfoSpecification;
import org.markus.rhserver.model.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository extends AbstractJavaRepository<Users, UUID> {
    private static final UsersTable table = UsersTable.$;
    public UserRepository(JSqlClient jSqlClient){
        super(jSqlClient);
    }
    public List<Users> findByEmailOrUserName(String info, Fetcher<Users> fetcher){
        UserInfoSpecification specification = new UserInfoSpecification();
        specification.setSearchInfo(info);
        return sql.createQuery(table)
                .where(specification)
                .select(table.fetch(fetcher))
                .execute();
    }
    public Integer updateLastLogin(UUID userId,int version){
        var userDraftCommand = UsersDraft.$.produce(
                draft -> {
                    draft.setUuid(userId);
                    draft.setLastLogin(LocalDateTime.now(ZoneOffset.ofHours(8)));
                    draft.setVersion(version);
                }
        );
        var result = sql.saveCommand(userDraftCommand)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute();
        return result.getTotalAffectedRowCount();
    }
    
    public Integer updateLastOff(UUID userId){
        var version = sql.createQuery(table)
                .where(table.uuid().eq(userId))
                .select(table.fetch(Fetchers.USERS_FETCHER.version()))
                .execute().getFirst().version();
        var userDraftCommand = UsersDraft.$.produce(
                draft -> {
                    draft.setUuid(userId);
                    draft.setLastOff(LocalDateTime.now(ZoneOffset.ofHours(8)));
                    draft.setVersion(version);
                }
        );
         var result = sql.saveCommand(userDraftCommand)
                 .setMode(SaveMode.UPDATE_ONLY)
                 .execute();
         return result.getTotalAffectedRowCount();
    }
}