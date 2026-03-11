package com.example.joinping.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferTopic;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.TopicService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 话题相关请求接口类
 */
@RestController
@RequestMapping("/topic")
public class TopicController {
    @Resource
    private TopicService topicService;
    
    /**
     * 根据id获取话题详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        return Result.success(topicService.getById(id));
    }
    
    /**
     * 根据分区id分页查询话题列表
     *
     * @param topic
     * @return
     */
    @GetMapping("/areaId")
    public Result getByAreaId(Topic topic) {
        return Result.success(topicService.getByAreaId(topic));
    }
    
    /**
     * 新增话题
     *
     * @param bufferTopic
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    @PutMapping
    public Result insert(@RequestBody BufferTopic bufferTopic) {
        topicService.insert(bufferTopic);
        return Result.success();
    }
    
    
    /**
     * 删除话题
     *
     * @param id
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        topicService.delete(id, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
    
    /**
     * 获取话题类别列表
     *
     * @return
     */
    @GetMapping("/area")
    public Result getAreaList() {
        return Result.success(topicService.getAreaList());
    }
    
    /**
     * 分页查询我创建的话题列表
     *
     * @return
     */
    @GetMapping("/my")
    public Result getMyTopicList(Search search) {
        return Result.success(topicService.getMyTopicList(search));
    }
}
