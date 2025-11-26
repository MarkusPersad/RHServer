package org.markus.rhserver.model;

import org.babyfish.jimmer.sql.MappedSuperclass;
import org.babyfish.jimmer.sql.Version;

import java.time.LocalDateTime;

@MappedSuperclass
public interface BaseEntity {
    @Version
    int version();
    LocalDateTime createAt();
}
