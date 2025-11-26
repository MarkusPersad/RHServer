package org.markus.rhserver.utils;

import org.babyfish.jimmer.sql.meta.UserIdGenerator;

import java.util.UUID;

public class UserIdGenerate implements UserIdGenerator<UUID> {
    @Override
    public UUID generate(Class<?> entityType) {
        return UUID.nameUUIDFromBytes(entityType.getName().getBytes()) ;
    }
}
