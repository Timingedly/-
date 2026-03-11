package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.relaPojo.TopicUserFavorite;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.TopicUserFavoriteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 话题的收藏相关请求接口类
 */
@RestController
@RequestMapping("/favorite/topic")
public class TopicUserFavoriteController {
    
    @Resource
    private TopicUserFavoriteService topicUserFavoriteService;
    
    /**
     * 收藏话题
     *
     * @param topicUserFavorite
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.FavoriteLock)
    @PostMapping("/confirm")
    public Result confirmFavorite(TopicUserFavorite topicUserFavorite) {
        topicUserFavoriteService.confirmFavorite(topicUserFavorite);
        return Result.success();
    }
    
    /**
     * 取消收藏话题
     *
     * @param topicUserFavorite
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.FavoriteLock)
    @PostMapping("/cancel")
    public Result cancelLike(TopicUserFavorite topicUserFavorite) {
        topicUserFavoriteService.cancelFavorite(topicUserFavorite);
        return Result.success();
    }
    
    /**
     * 查询用户收藏情况
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable("id") Long id) {
        return Result.success(topicUserFavoriteService.getUserFavoriteStatus(id));
    }
    
    /**
     * 分页查询“我的收藏”话题列表
     *
     * @param search
     * @return
     */
    @GetMapping
    public Result selectPage(Search search) {
        return Result.success(topicUserFavoriteService.selectPage(search));
    }
}
