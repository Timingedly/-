package com.example.joinping.controller;

import com.example.joinping.entity.vo.Result;
import com.example.joinping.service.OllamaApiService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大模型相关请求接口类
 */
@RequestMapping("/ollama/api")
@RestController
public class OllamaApiController {
    @Resource
    private OllamaApiService ollamaApiService;
    
    @GetMapping("/json")
    public Result getJSONResponseByMessage(String message) {
        return Result.success(ollamaApiService.getJSONResponseByMessage(message));
    }
    
    @GetMapping("/class")
    public Result getOllamaResultByResponse(String message) {
        return Result.success(ollamaApiService.getOllamaResultByResponse(ollamaApiService.getJSONResponseByMessage(message)));
    }
    
    @GetMapping("/boolean")
    public Result getFinalReviewResultByResponse(String message) {
        return Result.success(ollamaApiService.getFinalReviewResultByResponse(message, ollamaApiService.getJSONResponseByMessage(message)));
    }
    
}


