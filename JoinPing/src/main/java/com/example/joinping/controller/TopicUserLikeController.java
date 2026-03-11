package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.relaPojo.TopicUserLike;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.TopicUserLikeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 话题的点赞相关请求接口类
 */
@RestController
@RequestMapping("/like/topic")
public class TopicUserLikeController {
    @Resource
    private TopicUserLikeService topicUserLikeService;
    
    /**
     * 点赞话题
     *
     * @param topicUserLike
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.LikeLock)
    @PostMapping("/confirm")
    public Result confirmLike(TopicUserLike topicUserLike) {
        topicUserLikeService.confirmLike(topicUserLike);
        return Result.success();
    }
    
    /**
     * 取消点赞话题
     *
     * @param topicUserLike
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.LikeLock)
    @PostMapping("/cancel")
    public Result cancelLike(TopicUserLike topicUserLike) {
        topicUserLikeService.cancelLike(topicUserLike);
        return Result.success();
    }
    
    /**
     * 查询用户点赞情况
     *
     * @param topicUserLike
     * @return
     */
    @GetMapping
    public Result getUserLikeStatus(TopicUserLike topicUserLike) {
        return Result.success(topicUserLikeService.userLiked(topicUserLike));
    }
}
