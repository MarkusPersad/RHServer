package org.markus.rhserver.controller;

import org.markus.rhserver.entity.dto.CreateGroupInput;
import org.markus.rhserver.entity.dto.GroupSearchInput;
import org.markus.rhserver.entity.vo.Response;
import org.markus.rhserver.model.Group;
import org.markus.rhserver.service.GroupService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/group")
class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService){
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public Response<Void> createGroup(@RequestBody CreateGroupInput input){
        groupService.createGroup(input);
        return Response.getSuccessResponse(null);
    }

    @PostMapping("/search")
    public Response<List<Group>> searchGroup(@RequestBody GroupSearchInput input){
        return Response.getSuccessResponse(groupService.findByGroupName(input));
    }
}
