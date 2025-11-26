package org.markus.rhserver.model;

import java.lang.String;
import java.util.UUID;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Key;
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator;
import org.jetbrains.annotations.Nullable;
import org.markus.rhserver.utils.UserIdGenerate;

@Entity
public interface Group extends BaseEntity {
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator.class)
    UUID uuid();

    @Key(group = "name")
    String groupName();

    UUID ownerId();

    @Nullable
    String notice();

    @Nullable
    String avatar();
}
