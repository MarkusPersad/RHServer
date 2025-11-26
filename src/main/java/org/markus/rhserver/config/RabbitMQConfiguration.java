package org.markus.rhserver.config;

import org.markus.rhserver.protobuf.MessageSend;
import org.markus.rhserver.rabbitmq.MessageHandler;
import org.markus.rhserver.rabbitmq.ProtoBufMessageConvert;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration
@EnableRabbit
public class RabbitMQConfiguration {

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

        /**
     * 创建并配置消息转换器Bean
     *
     * @return 返回配置好的ProtoBuf消息转换器实例
     */
    @Bean
    public MessageConverter messageConverter(){
        return ProtoBufMessageConvert.of(MessageSend.class);
    }


        /**
     * 创建并配置DirectMessageListenerContainer消息监听容器Bean
     * 该容器用于监听和处理RabbitMQ队列中的消息
     *
     * @param factory 连接工厂，用于创建与RabbitMQ服务器的连接
     * @param taskExecutor 虚拟线程任务执行器，用于执行消息处理任务，提高并发性能
     * @return 配置完成的DirectMessageListenerContainer实例
     */
    @Bean
    public DirectMessageListenerContainer messageListenerContainer(
            ConnectionFactory factory,
            VirtualThreadTaskExecutor taskExecutor,
            MessageHandler rabbitMessageHandler
    ){
        // 创建DirectMessageListenerContainer实例，用于直接监听队列消息
        DirectMessageListenerContainer container = new DirectMessageListenerContainer();

        // 设置连接工厂，用于建立与RabbitMQ的连接
        container.setConnectionFactory(factory);

        // 设置每个队列的消费者数量为1，确保消息处理的顺序性
        container.setConsumersPerQueue(1);

        // 设置预取计数为10，控制每个消费者在确认消息前可以获取的消息数量
        container.setPrefetchCount(30);

        container.setAcknowledgeMode(AcknowledgeMode.AUTO);

        container.setDefaultRequeueRejected(false);

        // 设置任务执行器为虚拟线程执行器，提高并发处理能力
        container.setTaskExecutor(taskExecutor);

        container.setMessageListener(rabbitMessageHandler);

        return container;
    }


        /**
     * 创建并配置一个持久化的直连交换机(DirectExchange)
     *
     * @return 配置好的DirectExchange实例，该交换机具有持久化特性
     */
    @Bean
    public DirectExchange directExchange(){
        // 使用ExchangeBuilder构建一个直连交换机，设置其名称和持久化属性
        return new DirectExchange(exchangeName,true,false);
    }


        /**
     * 创建并配置RabbitMQ消息模板Bean
     *
     * @param factory 连接工厂，用于建立与RabbitMQ服务器的连接
     * @param messageConverter 消息转换器，用于序列化和反序列化消息内容
     * @param taskExecutor 虚拟线程任务执行器，用于异步处理消息发送任务
     * @return 配置完成的RabbitTemplate实例
     */
    @Bean("vrabbitTemplate")
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory factory,
            MessageConverter messageConverter,
            VirtualThreadTaskExecutor taskExecutor
    ){
        // 创建RabbitTemplate实例并设置基础连接工厂
        RabbitTemplate template = new RabbitTemplate(factory);
        // 配置消息转换器
        template.setMessageConverter(messageConverter);
        // 设置任务执行器
        template.setTaskExecutor(taskExecutor);
        return template;
    }


        /**
     * 创建并配置RabbitAdmin实例
     *
     * @param connectionFactory 连接工厂，用于建立与RabbitMQ服务器的连接
     * @param taskExecutor 虚拟线程任务执行器，用于执行异步任务
     * @param directExchange 直连交换机，用于消息路由
     * @return 配置好的RabbitAdmin实例
     */
    @Bean
    public RabbitAdmin rabbitAdmin(
            ConnectionFactory connectionFactory,
            VirtualThreadTaskExecutor taskExecutor,
            DirectExchange directExchange
            ) {
        // 创建RabbitAdmin实例并设置连接工厂
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 配置任务执行器
        rabbitAdmin.setTaskExecutor(taskExecutor);
        // 声明直连交换机
        rabbitAdmin.declareExchange(directExchange);
        return rabbitAdmin;
    }
}
