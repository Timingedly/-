package com.example.joinping.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结果封装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    
    private int code;
    private String message;
    private T data;
    private Boolean hasSuccessed;
    
    public Result(T data) {
        this.code = 200;
        this.message = "success";
        this.data = data;
        this.hasSuccessed = true;
    }
    
    public Result(T data, boolean success, String message) {
        if (success) {
            this.code = 200;
            this.message = "success";
            this.hasSuccessed = true;
        } else {
            this.code = 500;
            this.message = message;
            this.hasSuccessed = false;
        }
        this.data = data;
    }
    
    public Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
        this.hasSuccessed = false;
    }
    
    public static Result getResult(boolean hasSuccessed) {
        if (hasSuccessed) {
            return Result.success();
        } else {
            return Result.fail("操作失败");
        }
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }
    
    public static Result success() {
        return new Result(null, true, null);
    }
    
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message);
    }
    
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message);
    }
}
