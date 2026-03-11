package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论回复类型枚举
 */
@Getter
@AllArgsConstructor
public enum ReplyTypeEnum {
    ReplyT1Comment(1, "回复一级评论"),
    ReplyT2Comment(2, "回复二级评论");
    
    private final Integer value;
    private final String description;
    
}
