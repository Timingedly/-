package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交换机与队列的匹配键枚举
 */
@Getter
@AllArgsConstructor
public enum MessageRoutingKeyEnum {
    NORMAL("2", 2, "普通文本"),
    TOPIC("3", 3, "主题文本"),
    THING("4", 4, "主体文本"),
    T1COMMENT("5", 5, "一级评论文本"),
    T2COMMENT("6", 6, "二级评论文本");
    
    private final String value;
    private final Integer type;
    private final String description;
    
}
