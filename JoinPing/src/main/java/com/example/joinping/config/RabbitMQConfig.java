package com.example.joinping.config;

import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.joinping.constant.RabbitMQConstants.*;

/**
 * 消息队列RabbitMQ的配置类
 */
@Configuration
public class RabbitMQConfig {
    
    /**
     * 全局消息转换器，使mq能正确传输和接收对象
     *
     * @param objectMapper
     * @return
     */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    
    //声明普通交换机
    @Bean(name = NORMAL_DIRECT_EXCHANGE)
    public DirectExchange normalEx1() {
        //创建持久化交换机
        return new DirectExchange(NORMAL_DIRECT_EXCHANGE, true, false);
    }
    
    //声明死信交换机
    @Bean(name = DEADLETTER_FANOUT_EXCHANGE)
    public FanoutExchange dlEx1() {
        return new FanoutExchange(DEADLETTER_FANOUT_EXCHANGE, true, false);
    }
    
    //声明死信队列
    @Bean(name = DEADLETTER_QUEUE)
    public Queue dlQ1() {
        return QueueBuilder.durable(DEADLETTER_QUEUE).build();
    }
    
    //绑定死信交换机与死信队列
    @Bean
    public Binding dlQ1ToEx1(@Qualifier(DEADLETTER_FANOUT_EXCHANGE) FanoutExchange dl_ex1, @Qualifier(DEADLETTER_QUEUE) Queue dl_q1) {
        return BindingBuilder.bind(dl_q1).to(dl_ex1);
    }
    
    //声明普通队列(绑定死信交换机)
    @Bean(name = NORMAL_QUEUE)
    public Queue normalQ1() {
        //durable创建持久化队列
        return QueueBuilder.durable(NORMAL_QUEUE).deadLetterExchange(DEADLETTER_FANOUT_EXCHANGE).build();
    }
    
    //绑定普通交换机与普通队列
    @Bean
    public Binding normalQ1ToEx1(@Qualifier(NORMAL_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(NORMAL_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.NORMAL.getValue());
    }
    
    //=====================================================评论清理=================================================================
    //声明评论清理交换机
    @Bean(name = THING_ID_FANOUT_EXCHANGE)
    public FanoutExchange thingIdEx1() {
        //创建持久化交换机
        return new FanoutExchange(THING_ID_FANOUT_EXCHANGE, true, false);
    }
    
    //声明评论清理队列
    @Bean(name = THING_ID_QUEUE)
    public Queue thingIdQ1() {
        return QueueBuilder.durable(THING_ID_QUEUE).build();
    }
    
    //绑定评论清理交换机与评论清理队列
    @Bean
    public Binding thingIdQ1ToThingIdEx1(@Qualifier(THING_ID_FANOUT_EXCHANGE) FanoutExchange ex1, @Qualifier(THING_ID_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1);
    }
    
    //=====================================================图片清理=================================================================
    //声明图片清理交换机
    @Bean(name = IMAGE_PATH_FANOUT_EXCHANGE)
    public FanoutExchange imagePathEx1() {
        //创建持久化交换机
        return new FanoutExchange(IMAGE_PATH_FANOUT_EXCHANGE, true, false);
    }
    
    //声明图片清理队列
    @Bean(name = IMAGE_PATH_QUEUE)
    public Queue imagePathQ1() {
        return QueueBuilder.durable(IMAGE_PATH_QUEUE).build();
    }
    
    //绑定图片清理交换机与图片清理队列
    @Bean
    public Binding imagePathQ1ToImagePathEx1(@Qualifier(IMAGE_PATH_FANOUT_EXCHANGE) FanoutExchange ex1, @Qualifier(IMAGE_PATH_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1);
    }
    
    //=====================================================用户举报=================================================================
    
    //声明举报信息交换机
    @Bean(name = AGAINST_DIRECT_EXCHANGE)
    public DirectExchange againstEx1() {
        //创建持久化交换机
        return new DirectExchange(AGAINST_DIRECT_EXCHANGE, true, false);
    }
    
    //声明举报Topic队列
    @Bean(name = AGAINST_TOPIC_QUEUE)
    public Queue againstTopicQ1() {
        return QueueBuilder.durable(AGAINST_TOPIC_QUEUE).build();
    }
    
    //声明举报Thing队列
    @Bean(name = AGAINST_THING_QUEUE)
    public Queue againstThingQ1() {
        return QueueBuilder.durable(AGAINST_THING_QUEUE).build();
    }
    
    //声明举报T1Comment队列
    @Bean(name = AGAINST_T1COMMENT_QUEUE)
    public Queue againstT1CommentQ1() {
        return QueueBuilder.durable(AGAINST_T1COMMENT_QUEUE).build();
    }
    
    //声明举报T2Comment队列
    @Bean(name = AGAINST_T2COMMENT_QUEUE)
    public Queue againstT2CommentQ1() {
        return QueueBuilder.durable(AGAINST_T2COMMENT_QUEUE).build();
    }
    
    
    //绑定举报信息交换机与举报Topic队列
    @Bean
    public Binding againstTopicQ1ToAgainstEx1(@Qualifier(AGAINST_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(AGAINST_TOPIC_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.TOPIC.getValue());
    }
    
    //绑定举报信息交换机与举报Thing队列
    @Bean
    public Binding againstThingQ1ToAgainstEx1(@Qualifier(AGAINST_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(AGAINST_THING_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.THING.getValue());
    }
    
    //绑定举报信息交换机与举报T1Comment队列
    @Bean
    public Binding againstT1CommentQ1ToAgainstEx1(@Qualifier(AGAINST_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(AGAINST_T1COMMENT_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.T1COMMENT.getValue());
    }
    
    //绑定举报信息交换机与举报T2Comment队列
    @Bean
    public Binding againstT2CommentQ1ToAgainstEx1(@Qualifier(AGAINST_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(AGAINST_T2COMMENT_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.T2COMMENT.getValue());
    }
    
    //=====================================================首页热榜=================================================================
    
    //声明首页热榜更新交换机
    @Bean(name = INDEX_RANKLIST_UPDATE_FANOUT_EXCHANGE)
    public FanoutExchange indexRankListUpdateEx1() {
        //创建持久化交换机
        return new FanoutExchange(INDEX_RANKLIST_UPDATE_FANOUT_EXCHANGE, true, false);
    }
    
    //声明首页热榜更新队列
    @Bean(name = INDEX_RANKLIST_UPDATE_QUEUE)
    public Queue indexRankListUpdateQ1() {
        return QueueBuilder.durable(INDEX_RANKLIST_UPDATE_QUEUE).build();
    }
    
    //绑定首页热榜更新交换机与首页热榜更新队列
    @Bean
    public Binding indexRankListUpdateQ1ToIndexRankListUpdateEx1(@Qualifier(INDEX_RANKLIST_UPDATE_FANOUT_EXCHANGE) FanoutExchange ex1, @Qualifier(INDEX_RANKLIST_UPDATE_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1);
    }
    
    
    //声明话题队列
    @Bean(name = TOPIC_QUEUE)
    public Queue topicQ1() {
        return QueueBuilder.durable(TOPIC_QUEUE).build();
    }
    
    //声明主体队列
    @Bean(name = THING_QUEUE)
    public Queue thingQ1() {
        return QueueBuilder.durable(THING_QUEUE).build();
    }
    
    //声明一级评论队列
    @Bean(name = T1COMMENT_QUEUE)
    public Queue t1CommentQ1() {
        return QueueBuilder.durable(T1COMMENT_QUEUE).build();
    }
    
    //声明二级评论队列
    @Bean(name = T2COMMENT_QUEUE)
    public Queue t2CommentQ1() {
        return QueueBuilder.durable(T2COMMENT_QUEUE).build();
    }
    
    //绑定普通交换机与话题队列
    @Bean
    public Binding topicQ1ToEx1(@Qualifier(NORMAL_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(TOPIC_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.TOPIC.getValue());
    }
    
    //绑定普通交换机与主体队列
    @Bean
    public Binding thingQ1ToEx1(@Qualifier(NORMAL_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(THING_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.THING.getValue());
    }
    
    //绑定普通交换机与一级评论队列
    @Bean
    public Binding t1CommentQ1ToEx1(@Qualifier(NORMAL_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(T1COMMENT_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.T1COMMENT.getValue());
    }
    
    //绑定普通交换机与二级评论队列
    @Bean
    public Binding t2CommentQ1ToEx1(@Qualifier(NORMAL_DIRECT_EXCHANGE) DirectExchange ex1, @Qualifier(T2COMMENT_QUEUE) Queue q1) {
        return BindingBuilder.bind(q1).to(ex1).with(MessageRoutingKeyEnum.T2COMMENT.getValue());
    }
    
}
