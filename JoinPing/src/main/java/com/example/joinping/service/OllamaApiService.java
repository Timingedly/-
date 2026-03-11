package com.example.joinping.service;

import com.example.joinping.entity.vo.OllamaResult;

/**
 * Ollama提供的相关操作
 */
public interface OllamaApiService {
    /**
     * 向大模型发送信息并获取响应
     *
     * @param message
     * @return
     */
    String getJSONResponseByMessage(String message);
    
    /**
     * 解析调用大模型后得到的数据并封装为OllamaResult类型
     *
     * @param jsonResponse
     * @return
     */
    OllamaResult getOllamaResultByResponse(String jsonResponse);
    
    /**
     * 得到文本内容最终审核结果(true为审核通过，false为失败)
     *
     * @param message
     * @param jsonResponse
     * @return
     */
    Boolean getFinalReviewResultByResponse(String message, String jsonResponse);
    
}
