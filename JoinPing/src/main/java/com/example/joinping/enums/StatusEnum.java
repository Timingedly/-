package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    NORMAL(0, "未删除"),
    DELETED(1, "已删除");
    
    private final Integer value;
    private final String description;
    
}
