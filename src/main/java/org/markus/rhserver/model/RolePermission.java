package org.markus.rhserver.model;

import java.lang.String;
import java.time.LocalDateTime;
import java.util.UUID;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.jetbrains.annotations.Nullable;

@Entity
public interface RolePermission extends BaseEntity {
    @Id
    UUID uuid();

    String role();

    @Nullable
    LocalDateTime updateAt();
}
