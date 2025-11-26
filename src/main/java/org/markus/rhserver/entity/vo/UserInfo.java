package org.markus.rhserver.entity.vo;

import java.util.UUID;

public record UserInfo(
        UUID userId,
        String userName,
        String email,
        String token) {
}
