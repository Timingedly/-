package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {
    System(1, "系统通知"),
    User(2, "用户通知");
    
    private final Integer value;
    private final String description;
    
}
