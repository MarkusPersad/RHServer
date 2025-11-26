package org.markus.rhserver.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {
    SUCCESS(200, "成功"),
    NOT_FOUND(404, "未找到"),
    BAD_REQUEST(600, "请求错误"),
    INTERNAL_SERVER_ERROR(500, "服务器错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    ALREADY_EXISTS(601, "已存在"),
    PASSWORD_USERNAME_NOT_MATCH(602, "用户名密码不匹配"),
    FRIEND_APPLY_EXISTS(603, "已存在申请");
    private final Integer code;
    private final String message;
}