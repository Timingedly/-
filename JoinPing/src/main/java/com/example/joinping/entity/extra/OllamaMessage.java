package com.example.joinping.entity.extra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文本封装类（Ollama专用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OllamaMessage implements Serializable {
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 类别
     */
    private String routingKey;
    
    /**
     * 文本
     */
    private String message;
    
    /**
     * 关联的用户id
     */
    private Long userId;
}
