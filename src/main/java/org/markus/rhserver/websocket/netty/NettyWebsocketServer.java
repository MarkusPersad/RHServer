package org.markus.rhserver.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyWebsocketServer {
    @Value("${ws-netty.heartbeat-interval}")
    private long heartbeatTimeout;
    @Value("${ws-netty.path}")
    private String path;
    @Value("${ws-netty.port}")
    private int port;
    private static final EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
    private static final EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    private final HandleHeartBeat handleHeartBeat;
    private final HandleWebsocket handleWebsocket;

    public NettyWebsocketServer(
            HandleHeartBeat handleHeartBeat,
            HandleWebsocket handleWebsocket
    ){
        this.handleHeartBeat = handleHeartBeat;
        this.handleWebsocket = handleWebsocket;
    }
    @Async
    public void nettyStart(){
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.ERROR))
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(1024*64))
                                    .addLast(new IdleStateHandler(heartbeatTimeout, heartbeatTimeout/2, 0, TimeUnit.SECONDS))
                                    .addLast(handleHeartBeat)
                                    .addLast(new WebSocketServerCompressionHandler(6))
                                    .addLast(new WebSocketServerProtocolHandler(path, null, true,64*1024,true,true,10000L))
                                    .addLast(handleWebsocket);
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            log.info("Netty Websocket Server Started on port {}",port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Error starting Netty Websocket Server",e);
            throw new RuntimeException(e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    @PreDestroy
    public void stop(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
