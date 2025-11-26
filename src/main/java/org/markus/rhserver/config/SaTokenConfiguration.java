package org.markus.rhserver.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.fun.strategy.SaCorsHandleFunction;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.serializer.impl.SaSerializerTemplateForJdkUseHex;
import cn.dev33.satoken.stp.StpLogic;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class SaTokenConfiguration {
    @PostConstruct
    public void rewriteComponent(){
        SaManager.setSaSerializerTemplate(new SaSerializerTemplateForJdkUseHex());
    }


    @Bean
    public StpLogic getStpLogicJwt(){

        return new StpLogicJwtForSimple();
    }

    @Bean
    public SaCorsHandleFunction corsHandler(){
        return ((_, saResponse, _) ->{
            saResponse
                    .setHeader("Access-Control-Allow-Origin", "*")
                    .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
                    .setHeader("Access-Control-Max-Age", "3600")
                    .setHeader("Access-Control-Allow-Headers", "*");
            SaRouter.match(SaHttpMethod.OPTIONS)
                    .free(_ ->log.info("--------OPTIONS预检请求，不做处理"))
                    .back();
        });
    }

    @Bean
    public Argon2 argon2(){
        return Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }
}
