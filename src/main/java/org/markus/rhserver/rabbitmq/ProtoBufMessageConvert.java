package org.markus.rhserver.rabbitmq;

import com.google.protobuf.MessageLite;
import org.jetbrains.annotations.NotNull;
import org.markus.rhserver.constants.Constants;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.lang.reflect.Method;

public class ProtoBufMessageConvert<T extends MessageLite> extends AbstractMessageConverter {

    private final Class<T> clazz;

    public ProtoBufMessageConvert(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    protected @NotNull Message createMessage(@NotNull Object o, @NotNull MessageProperties messageProperties) throws MessageConversionException {
        if (!(o instanceof MessageLite protoMessage)) {
            throw new MessageConversionException("Object of type " + o.getClass().getName() +
                    " cannot be converted to Protocol Buffers message");
        }

        try {
            // 直接调用 toByteArray 方法，避免反射
            byte[] body = protoMessage.toByteArray();
            messageProperties.setContentType(Constants.APPLICATION_X_PROTOBUF);
            messageProperties.setContentLength(body.length);
            return new Message(body, messageProperties);
        } catch (Exception exception) {
            throw new MessageConversionException("Failed to convert Protocol Buffers message", exception);
        }
    }

    @Override
    public @NotNull Object fromMessage(@NotNull Message message) throws MessageConversionException {
        try {
            byte[] body = message.getBody();
            // 使用 getDefaultInstance 获取 parser 实例更安全
            Method defaultInstanceMethod = clazz.getMethod("getDefaultInstance");
            MessageLite defaultInstance = (MessageLite) defaultInstanceMethod.invoke(null);
            return defaultInstance.getParserForType().parseFrom(body);
        } catch (Exception exception) {
            throw new MessageConversionException("Failed to convert Protocol Buffers message", exception);
        }
    }

    /**
     * 创建指定类型的 ProtoBuf 消息转换器的便捷方法
     */
    public static <T extends MessageLite> ProtoBufMessageConvert<T> of(Class<T> clazz) {
        return new ProtoBufMessageConvert<>(clazz);
    }
}
