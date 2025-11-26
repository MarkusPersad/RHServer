package org.markus.rhserver.controller;

import org.markus.rhserver.entity.dto.AcceptGroupInput;
import org.markus.rhserver.entity.dto.GetUserInfoInput;
import org.markus.rhserver.entity.dto.GroupSearchInput;
import org.markus.rhserver.entity.vo.FriendList;
import org.markus.rhserver.entity.vo.Response;
import org.markus.rhserver.exception.BusinessException;
import org.markus.rhserver.service.FriendService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/friend")
class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService){
        this.friendService = friendService;
    }

    @PostMapping("/apply")
    public Response<Void> applyFriend(@RequestBody GetUserInfoInput input)throws BusinessException {
        friendService.ApplyFriend(input);
        return Response.getSuccessResponse( null);
    }

    @GetMapping("/list")
    public Response<FriendList> getFriendList(){
        return Response.getSuccessResponse(friendService.getFriendList());
    }

    @PostMapping("/addGroup")
    public Response<Void> addGroup(@RequestBody GroupSearchInput input) throws BusinessException {
        friendService.AddGroup(input);
        return Response.getSuccessResponse(null);
    }

    @PostMapping("/acceptFriend")
    public Response<Void> acceptFriend(@RequestBody GetUserInfoInput input){
        friendService.AcceptFriend( input);
        return Response.getSuccessResponse(null);
    }
    @PostMapping("/acceptGroup")
    public Response<Void>acceptGroup(@RequestBody AcceptGroupInput input) throws BusinessException {
        friendService.AcceptGroup(input);
        return Response.getSuccessResponse(null);
    }
}
