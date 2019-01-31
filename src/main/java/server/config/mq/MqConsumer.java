package server.config.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MqConsumer {

    private static final String QUEUE_NAME = "direct-queue1";
    private final MqConnectionConfig mqConnectionConfig;

    @Autowired
    public MqConsumer(MqConnectionConfig mqConnectionConfig) {
        this.mqConnectionConfig = mqConnectionConfig;
    }

    //    SimpleMessageListenerContainer，消费者配置
//    @Bean
//    public SimpleMessageListenerContainer messageContainer() {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(mqConnectionConfig.connectionFactory());
//        container.setQueueNames(QUEUE_NAME);
//        container.setExposeListenerChannel(true);
//        container.setMaxConcurrentConsumers(1);
//        container.setConcurrentConsumers(1);
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
//            byte[] body = message.getBody();
//            log.info("消费端接收到消息 : " + new String(body));
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//        });
//        return container;
//    }

    //自定义Jackson2JsonMessageConverter反序列化
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }
}
