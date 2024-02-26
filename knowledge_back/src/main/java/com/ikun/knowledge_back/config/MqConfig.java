package com.ikun.knowledge_back.config;

import com.ikun.knowledge_back.common.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MqConfig {
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.ARTICLE_EXCHANGE,true,false);
        /**
         * “durable”（持久性）：如果设置为true，则表示交换机在RabbitMQ服务器重启后仍然存在。如果设置为false，则表示交换机在RabbitMQ服务器重启后会被自动删除。
         * “autoDelete”（自动删除）：如果设置为true，则表示当所有绑定到此交换机的队列都完成了对此交换机的使用（即，都已解绑），则自动删除该交换机。如果设置为false，则即使所有绑定队列都已解绑，交换机也不会被自动删除。
         */
    }
    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.ARTICLE_INSERT_QUEUE,true);
    }

    @Bean
    public Queue deleteQueue(){
        return new Queue(MqConstants.ARTICLE_DELETE_QUEUE,true);
    }

    @Bean
    public Binding insertQueueBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.ARTICLE_INSERT_KEY);
    }

    @Bean
    public Binding deleteQueueBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.ARTICLE_DELETE_KEY);
    }
}
