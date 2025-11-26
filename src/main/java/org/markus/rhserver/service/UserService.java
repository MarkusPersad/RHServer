package org.markus.rhserver.service;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.markus.rhserver.components.Argon2Id;
import org.markus.rhserver.entity.dto.GetUserInfoInput;
import org.markus.rhserver.entity.dto.LoginInput;
import org.markus.rhserver.entity.dto.RegisterInput;
import org.markus.rhserver.entity.vo.UserInfo;
import org.markus.rhserver.enums.ResponseCodeEnum;
import org.markus.rhserver.exception.BusinessException;
import org.markus.rhserver.model.Fetchers;
import org.markus.rhserver.model.Users;
import org.markus.rhserver.model.UsersDraft;
import org.markus.rhserver.rabbitmq.RabbitManager;
import org.markus.rhserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service

public class UserService {
    private final UserRepository userRepository;
    private final Argon2Id argon2Id;
    private final RabbitManager rabbitManager;

    public UserService(
            UserRepository userRepository,
            Argon2Id argon2Id,
            RabbitManager rabbitManager
            ){
        this.userRepository = userRepository;
        this.argon2Id = argon2Id;
        this.rabbitManager = rabbitManager;
    }
    public void register(RegisterInput input){
        Users user = UsersDraft.$.produce(draft -> {
            draft.setPassword(argon2Id.encode(input.getPassword()));
            draft.setEmail(input.getEmail());
            draft.setUserName(input.getUserName());
            draft.setAvatar(input.getAvatar());
            draft.setVersion(0);
        });
        SimpleSaveResult<Users> _ = userRepository.save(user,SaveMode.INSERT_ONLY);
    }
    @Transactional(rollbackFor = Exception.class)
    public UserInfo login(LoginInput  input) throws BusinessException{
        Users user = userRepository.findByEmailOrUserName(input.getEmail(),
                Fetchers.USERS_FETCHER
                        .password()
                        .userName()
                        .email()
                        .version()
        ).getFirst();

        if (!argon2Id.verify(user.password(),input.getPassword())){
            throw new BusinessException(ResponseCodeEnum.PASSWORD_USERNAME_NOT_MATCH);
        }
        StpUtil.login(user.uuid());
        var _ = userRepository.updateLastLogin(user.uuid(), user.version());
        rabbitManager.addQueue(user.uuid());
        return new UserInfo(user.uuid(),user.userName(),user.email(),StpUtil.getTokenValue());
    }
    @SaCheckLogin
    public List<Users> getUserInfo(GetUserInfoInput input){
        return userRepository.findByEmailOrUserName(input.getInfo(),
                Fetchers.USERS_FETCHER
                        .avatar()
                        .userName()
                        .email()
        );
    }
    @SaCheckLogin
    @Transactional(rollbackFor = Exception.class)
    public void logout(){
        UUID userId = UUID.fromString((String) StpUtil.getLoginId());
        rabbitManager.removeQueue(userId);
        var _ = userRepository.updateLastOff(userId);
        StpUtil.logout();
    }
}
