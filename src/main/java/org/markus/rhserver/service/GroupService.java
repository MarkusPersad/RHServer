package org.markus.rhserver.service;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.markus.rhserver.entity.dto.CreateGroupInput;
import org.markus.rhserver.entity.dto.GroupSearchInput;
import org.markus.rhserver.model.Group;
import org.markus.rhserver.model.GroupDraft;
import org.markus.rhserver.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GroupService {
    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository){
        this.groupRepository = groupRepository;
    }

    @SaCheckLogin
    public void createGroup(CreateGroupInput input){
        Group group = GroupDraft.$.produce(draft -> {
            draft.setGroupName(input.getGroupName());
            draft.setNotice(input.getNotice());
            draft.setAvatar(input.getAvatar());
            draft.setOwnerId((UUID) StpUtil.getLoginId());
        });
        groupRepository.save(group, SaveMode.INSERT_ONLY);
    }
    public List<Group> findByGroupName(GroupSearchInput input){
        return groupRepository.findByName(input.getInfo());
    }
}
