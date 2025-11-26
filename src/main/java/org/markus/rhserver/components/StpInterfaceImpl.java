package org.markus.rhserver.components;

import cn.dev33.satoken.stp.StpInterface;
import org.markus.rhserver.repository.RolePermissionRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
class StpInterfaceImpl implements StpInterface {

    private final RolePermissionRepository repository;

    public StpInterfaceImpl(RolePermissionRepository repository){
        this.repository = repository;
    }

    @Override
    public List<String> getPermissionList(Object o, String s) {
        return List.of();
    }

    @Override
public List<String> getRoleList(Object o, String s) {
    if (!(o instanceof UUID uuid)) {
        throw new IllegalArgumentException("参数o必须是UUID类型");
    }

        List<String> roles = repository.findRolesByUuid(uuid);

    if (roles == null) {
        return new ArrayList<>();
    }

    return roles.stream()
            .flatMap(role -> Arrays.stream(role.split(",")))
            .collect(Collectors.toList());
}

}
