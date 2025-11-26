package org.markus.rhserver.repository;

import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;
import org.markus.rhserver.model.RolePermission;
import org.markus.rhserver.model.RolePermissionTable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class RolePermissionRepository extends AbstractJavaRepository<RolePermission,UUID> {
    private static final RolePermissionTable table = RolePermissionTable.$;
    public RolePermissionRepository(JSqlClient jSqlClient){
        super(jSqlClient);
    }
    public List<String> findRolesByUuid(UUID uuid){
        return sql.createQuery(table)
                .where(table.uuid().eq(uuid))
                .select(table.role())
                .execute();
    }
}
