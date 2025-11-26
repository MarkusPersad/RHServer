package org.markus.rhserver.components;

import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.markus.rhserver.annotation.CheckRoleEnum;
import org.markus.rhserver.enums.RoleEnum;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedElement;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class CheckRoleEnumHandler implements SaAnnotationHandlerInterface<CheckRoleEnum> {
    @Override
    public Class<CheckRoleEnum> getHandlerAnnotationClass() {
        return CheckRoleEnum.class;
    }

    @Override
    public void checkMethod(CheckRoleEnum checkRoleEnum, AnnotatedElement annotatedElement) {
        RoleEnum role = checkRoleEnum.value();
        StpUtil.checkRole(role.getRole());
    }
}
