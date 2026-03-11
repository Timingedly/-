package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.relaPojo.T1CommentUserLike;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.T1CommentUserLikeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 一级评论的点赞相关请求接口类
 */
@RestController
@RequestMapping("/like/t1Comment")
public class T1CommentUserLikeController {
    @Resource
    private T1CommentUserLikeService t1CommentUserLikeService;
    
    /**
     * 点赞一级评论
     *
     * @param t1CommentUserLike
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.LikeLock)
    @PostMapping("/confirm")
    public Result confirmLike(T1CommentUserLike t1CommentUserLike) {
        t1CommentUserLikeService.confirmLike(t1CommentUserLike);
        return Result.success();
    }
    
    /**
     * 取消点赞一级评论
     *
     * @param t1CommentUserLike
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.LikeLock)
    @PostMapping("/cancel")
    public Result cancelLike(T1CommentUserLike t1CommentUserLike) {
        t1CommentUserLikeService.cancelLike(t1CommentUserLike);
        return Result.success();
    }
}
