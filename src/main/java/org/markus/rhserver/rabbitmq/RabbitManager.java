package org.markus.rhserver.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitManager {
    private final DirectMessageListenerContainer container;
    private final RabbitAdmin rabbitAdmin;
    private final DirectExchange directExchange;

    public RabbitManager(
            DirectMessageListenerContainer container,
            RabbitAdmin rabbitAdmin,
            DirectExchange directExchange

    ){
        this.container = container;
        this.rabbitAdmin = rabbitAdmin;
        this.directExchange = directExchange;
        rabbitAdmin.declareExchange(directExchange);
    }

        /**
     * 向RabbitMQ容器中添加一个新的队列
     *
     * @param uuid 用于创建队列名称的UUID标识符
     */
    public void addQueue(UUID uuid){
        // 确保容器处于活跃状态，如果未启动则启动容器
        if (!container.isRunning()){
            container.start();
        }
        // 声明并创建新的队列
        Queue queue = new Queue(uuid.toString(), true);
        rabbitAdmin.declareQueue(queue);
        Binding binding = BindingBuilder.bind(queue).to(directExchange).with(uuid.toString());
        rabbitAdmin.declareBinding(binding);
        // 将新队列名称添加到容器的队列列表中
        container.addQueueNames(uuid.toString());
    }

        /**
     * 移除指定UUID对应的队列
     *
     * @param uuid 队列的唯一标识符
     */
    public void removeQueue(UUID uuid){
        // 从容器中移除队列名称
        container.removeQueueNames(uuid.toString());
        // 删除RabbitMQ中的队列
        rabbitAdmin.deleteQueue(uuid.toString(),true, false);
    }
}
