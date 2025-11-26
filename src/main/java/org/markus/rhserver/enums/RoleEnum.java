package org.markus.rhserver.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleEnum {
    USER("user"),
    ADMIN("admin");
    private final String role;
}
