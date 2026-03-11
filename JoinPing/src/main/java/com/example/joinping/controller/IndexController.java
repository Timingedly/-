package com.example.joinping.controller;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.service.IndexService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主页请求接口类
 */
@RestController
@RequestMapping("/index")
public class IndexController {
    @Resource
    private IndexService indexService;
    
    
    /**
     * 查找首页话题排行榜
     *
     * @return
     */
    @GetMapping("/rank")
    public Result rankingList(String key) {
        return Result.success(indexService.rankingList(key));
    }
    
    /**
     * 根据用户输入文本查询相关话题
     *
     * @param search
     * @return
     */
    @GetMapping("/search")
    public Result search(Search search) {
        return Result.success(indexService.search(search));
    }
    
}
