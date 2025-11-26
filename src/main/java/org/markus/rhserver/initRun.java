package org.markus.rhserver;

import lombok.extern.slf4j.Slf4j;
import org.markus.rhserver.websocket.netty.NettyWebsocketServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class initRun implements ApplicationRunner {
    private final NettyWebsocketServer nettyWebsocketServer;

    public initRun(NettyWebsocketServer nettyWebsocketServer){
        this.nettyWebsocketServer = nettyWebsocketServer;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            nettyWebsocketServer.nettyStart();
        } catch (Exception e) {
            log.error("netty start error", e);
            throw new RuntimeException(e);
        }
    }
}
