package com.example.joinping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异常枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    
    REQUEST_FREQUENT_ERROR(500, "请求过于频繁，请稍后再试"),
    
    // 业务异常码
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_EXIST(1002, "用户已存在"),
    INVALID_PASSWORD(1003, "密码错误");
    
    private final Integer code;
    private final String message;
}
