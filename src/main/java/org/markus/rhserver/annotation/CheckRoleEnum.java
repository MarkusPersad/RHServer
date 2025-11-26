package org.markus.rhserver.annotation;

import cn.dev33.satoken.annotation.SaCheckRole;
import org.markus.rhserver.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SaCheckRole
public @interface CheckRoleEnum {
    RoleEnum value();
}
