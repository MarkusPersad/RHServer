package org.markus.rhserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * 任务执行器配置类
 * 该配置类用于配置和管理应用程序中的任务执行器，支持异步任务执行和虚拟线程功能。
 * 通过实现 AsyncConfigurer 接口，为 @Async 注解提供默认的执行器配置。
 */
@Configuration
@EnableAsync
@ComponentScan("org.markus.rhserver.websocket.netty")
class TaskExecutorConfiguration implements AsyncConfigurer {

    /**
     * 创建并配置虚拟线程任务执行器Bean
     * 虚拟线程是Java 21引入的轻量级线程实现，相比传统线程具有更低的资源消耗
     * 和更高的并发性能。该执行器使用指定的线程名称前缀创建虚拟线程。
     *
     * @return VirtualThreadTaskExecutor 虚拟线程任务执行器实例
     */
    @Bean
    public VirtualThreadTaskExecutor taskExecutor(){
        return new VirtualThreadTaskExecutor("RHserver-Virtual-");
    }

    /**
     * 获取异步任务执行器
     * 实现 AsyncConfigurer 接口的方法，为Spring的 @Async 注解提供默认的
     * 任务执行器。所有使用 @Async 注解的方法将使用此执行器执行。
     *
     * @return Executor 异步任务执行器实例
     */
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}

