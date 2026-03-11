package com.example.joinping.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型返回结果封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OllamaResult {
    /**
     *
     */
    private String createdAt;
    /**
     *
     */
    private String messageContent;
    
}
