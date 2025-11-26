package org.markus.rhserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.Valid;
import org.markus.rhserver.entity.dto.GetUserInfoInput;
import org.markus.rhserver.entity.dto.LoginInput;
import org.markus.rhserver.entity.dto.RegisterInput;
import org.markus.rhserver.entity.vo.Response;
import org.markus.rhserver.entity.vo.UserInfo;
import org.markus.rhserver.exception.BusinessException;
import org.markus.rhserver.model.Users;
import org.markus.rhserver.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Response<Void> register(@Valid @RequestBody RegisterInput input){
        userService.register(input);
        return Response.getSuccessResponse(null);
    }
    @PostMapping("/login")
    public Response<UserInfo> login(@Valid @RequestBody LoginInput input) throws BusinessException {
        var userInfo = userService.login(input);
        return Response.getSuccessResponse(userInfo);
    }
    @PostMapping("/info")
    public Response<List<Users>> getUserInfo(@RequestBody GetUserInfoInput input){
        return Response.getSuccessResponse(userService.getUserInfo(input));
    }
    @GetMapping("/logout")
    @SaCheckLogin
    public Response<Void> logout(){
        userService.logout();
        return Response.getSuccessResponse(null);
    }
}
