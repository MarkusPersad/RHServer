package org.markus.rhserver.repository;

import org.babyfish.jimmer.spring.repo.support.AbstractJavaRepository;
import org.babyfish.jimmer.sql.JSqlClient;
import org.markus.rhserver.model.Message;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository extends AbstractJavaRepository<Message,Long> {
    public MessageRepository(JSqlClient jSqlClient) {
        super(jSqlClient);
    }
}
