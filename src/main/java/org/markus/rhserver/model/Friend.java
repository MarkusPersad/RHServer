package org.markus.rhserver.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator;
import org.jetbrains.annotations.Nullable;
import org.markus.rhserver.utils.UserIdGenerate;

@Entity
public interface Friend extends BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator.class)
    UUID uuid();

    @Key(group = "ship")
    UUID userId();

    @Key(group = "ship")
    UUID friendId();

    String chatId();

    boolean status();

    boolean group();

    @Nullable
    LocalDateTime updateAt();

    @Nullable
    LocalDateTime deleteAt();
}
