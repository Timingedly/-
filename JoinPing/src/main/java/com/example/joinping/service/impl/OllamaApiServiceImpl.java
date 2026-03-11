package com.example.joinping.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.joinping.constant.OllamaConstants;
import com.example.joinping.constant.RedisConstants;
import com.example.joinping.entity.vo.OllamaResult;
import com.example.joinping.service.OllamaApiService;
import com.example.joinping.utils.ElasticsearchUtils;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OllamaApiServiceImpl implements OllamaApiService {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${ollama.url}")
    private String ollamaUrl;
    @Value("${ollama.model-name}")
    private String modelName;
    @Resource
    private RBloomFilter<String> bloomFilter;
    @Resource
    private ElasticsearchUtils elasticsearchUtils;
    
    @Override
    public Boolean getFinalReviewResultByResponse(String message, String jsonResponse) {
        //AI审核未通过
        if (OllamaConstants.MESSAGE_INVALID.equals(jsonResponse)) {
            return false;
        }
        //AI审核通过或判断不明确，则用布隆过滤器审核一遍是否存在敏感词
        //假设不存在敏感词
        boolean existBadWords = false;
        //调用ES分词器对message进行分词，再遍历分词结果与过滤器一一匹配
        List<String> testAnalyzeList = elasticsearchUtils.getTestAnalyzeList(message);
        for (String text : testAnalyzeList) {
            if (bloomFilter.contains(text)) {
                //布隆过滤器判断存在敏感词
                existBadWords = true;
            }
        }
        //布隆过滤器特性："存在是可能的,不存在是一定的"。
        if (existBadWords) {
            //为了排除布隆过滤器误判，用哈希表再次过滤
            for (String text : testAnalyzeList) {
                if (RedisConstants.FILTER_WORD_SET.contains(text)) {
                    //哈希表中判断存在敏感词
                    return false;
                }
            }
        }
        //布隆过滤器说不存在敏感词，则一定不存在，审核通过
        return true;
    }
    
    @Override
    public String getJSONResponseByMessage(String message) {
        // 请求地址 - 硬编码的本地Ollama API地址
        String url = ollamaUrl;
        // 构造请求体
        String jsonInputString = OllamaConstants.getJsonInputString(modelName, message);
        // 请求头 - 创建HTTP头对象
        HttpHeaders headers = new HttpHeaders();
        // 设置Content-Type为application/json
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 创建HTTP请求实体，包含请求体和头部
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonInputString, headers);
        try {
            // 使用restTemplate发送POST请求，接收String类型的响应
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            // 调用解析方法处理响应体
            OllamaResult ollamaResponse = getOllamaResultByResponse(response.getBody());
            // 控制台输出创建时间（调试信息）
            System.out.println("创建时间: " + ollamaResponse.getCreatedAt());
            // 控制台输出消息内容（调试信息）
            System.out.println("消息内容: " + ollamaResponse.getMessageContent());
            /* 当message为"今天天气真好啊"时ollamaResponse.getMessageContent()的值为下述字符串：
             * "<think>\n好的，我现在要仔细分析一下这个评论是否符合三个要求。首先，评论的内容是：“今天天气真好啊”。我要逐一检查这三个条件。\n\n第一个要求是不包含违法信息。评论中没有提到任何威胁或违法行为的信息，所以这是满足的。\n\n第二个要求是不包含不良信息。这里用了“真好”，虽然听起来有些口语化，但这个词本身是非负面的，也没有涉及到任何不当的行为，因此没问题。\n\n第三个要求是不包含不友善信息。评论中的语气积极，没有带有贬低或其他不友好的成分，所以也满足。\n\n综上所述，这个评论完全符合所有三个要求，应该回复“Y”。\n</think>\n\nY"
             * 可知，取最后一个字符就是AI审核的结果(Y或N)。
             * */
            int aiAnalyzeResultIndex = ollamaResponse.getMessageContent().length() - 1;
            return String.valueOf(ollamaResponse.getMessageContent().charAt(aiAnalyzeResultIndex));
        } catch (RestClientException e) {
            // 打印异常堆栈跟踪
            e.printStackTrace();
            // 返回错误信息字符串
            return "Error occurred while sending request: " + e.getMessage();
        }
    }
    
    @Override
    public OllamaResult getOllamaResultByResponse(String jsonResponse) {
        // 使用JSONUtil检查输入字符串是否为有效JSON格式
        if (!JSONUtil.isTypeJSON(jsonResponse)) {
            // 如果不是有效JSON，抛出运行时异常
            throw new IllegalArgumentException("Invalid JSON format.");
        }
        // 将JSON字符串解析为JSONObject
        JSONObject jsonObject = JSONUtil.parseObj(jsonResponse);
        // 从JSON对象中获取created_at字段值
        String createdAt = jsonObject.getStr("created_at");
        // 获取message字段的JSON对象
        JSONObject messageObject = jsonObject.getJSONObject("message");
        // 三元表达式：如果message对象不为空则获取content，否则返回null
        String content = messageObject != null ? messageObject.getStr("content") : null;
        // 创建并返回OllamaResult对象
        return new OllamaResult(createdAt, content);
    }
    
}
