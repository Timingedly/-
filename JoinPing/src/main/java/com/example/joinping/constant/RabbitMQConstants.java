package com.example.joinping.constant;

/**
 * RabbitMQ相关常量类
 */
public class RabbitMQConstants {
    
    /**
     * 普通direct交换机名字
     */
    public static final String NORMAL_DIRECT_EXCHANGE = "Normal_d_ex";
    
    /**
     * 普通队列名字
     */
    public static final String NORMAL_QUEUE = "Normal_q";
    
    /**
     * 主题队列名字
     */
    public static final String TOPIC_QUEUE = "Topic_q";
    
    /**
     * 主体队列名字
     */
    public static final String THING_QUEUE = "Thing_q";
    
    /**
     * 一级评论队列名字
     */
    public static final String T1COMMENT_QUEUE = "T1Comment_q";
    
    /**
     * 二级评论队列名字
     */
    public static final String T2COMMENT_QUEUE = "T2Comment_q";
    
    /**
     * 死信fanout交换机名字
     */
    public static final String DEADLETTER_FANOUT_EXCHANGE = "DeadLetter_fo_ex";
    
    /**
     * 死信队列名字
     */
    public static final String DEADLETTER_QUEUE = "DeadLetter_q";
    
    /**
     * 主体Fanout交换机名字（评论清理）
     */
    public static final String THING_ID_FANOUT_EXCHANGE = "ThingId_f_ex";
    
    /**
     * 主体队列名字（评论清理）
     */
    public static final String THING_ID_QUEUE = "ThingId_q";
    
    /**
     * 首页热榜更新交换机名字
     */
    public static final String INDEX_RANKLIST_UPDATE_FANOUT_EXCHANGE = "indexRankListUpdate_f_ex";
    /**
     * 首页热榜更新队列名字
     */
    public static final String INDEX_RANKLIST_UPDATE_QUEUE = "indexRankListUpdate_q";
    
    /**
     * 用户举报Direct交换机名字
     */
    public static final String AGAINST_DIRECT_EXCHANGE = "against_d_ex";
    
    /**
     * 用户举报Topic队列名字
     */
    public static final String AGAINST_TOPIC_QUEUE = "againstTopic_q";
    
    /**
     * 用户举报Thing队列名字
     */
    public static final String AGAINST_THING_QUEUE = "againstThing_q";
    
    /**
     * 用户举报T1Comment队列名字
     */
    public static final String AGAINST_T1COMMENT_QUEUE = "againstT1Comment_q";
    
    /**
     * 用户举报T2Comment队列名字
     */
    public static final String AGAINST_T2COMMENT_QUEUE = "againstT2Comment_q";
    
    /**
     * 图片清理Fanout交换机名字
     */
    public static final String IMAGE_PATH_FANOUT_EXCHANGE = "imagePath_f_ex";
    
    /**
     * 图片清理队列名字
     */
    public static final String IMAGE_PATH_QUEUE = "imagePath_q";
    
}
