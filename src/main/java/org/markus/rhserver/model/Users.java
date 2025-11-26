package org.markus.rhserver.model;

import java.lang.String;
import java.time.LocalDateTime;
import java.util.UUID;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator;
import org.jetbrains.annotations.Nullable;

@Entity
public interface Users extends BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator.class)
    UUID uuid();

    @Key(group = "email")
    String email();

    @Key(group = "userName")
    String userName();

    String password();

    @Nullable
    LocalDateTime updateAt();

    @Nullable
    LocalDateTime deleteAt();

    @Nullable
    String avatar();

    @Nullable
    LocalDateTime lastLogin();

    @Nullable
    LocalDateTime lastOff();
}
