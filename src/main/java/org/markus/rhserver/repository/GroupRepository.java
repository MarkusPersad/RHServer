package org.markus.rhserver.repository;

import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.LikeMode;
import org.markus.rhserver.model.Fetchers;
import org.markus.rhserver.model.Group;
import org.markus.rhserver.model.GroupTable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class GroupRepository extends AbstractJavaRepository<Group, UUID> {
    private static final GroupTable table = GroupTable.$;
    public GroupRepository(JSqlClient sql) {
        super(sql);
    }
    public List<Group> findByName(String info){
        return sql.createQuery(table)
                .where(table.groupName().like(info, LikeMode.ANYWHERE))
                .select(table.fetch(Fetchers.GROUP_FETCHER
                        .avatar()
                        .groupName()
                        .notice()
                        .ownerId()
                )).execute();
    }
}
