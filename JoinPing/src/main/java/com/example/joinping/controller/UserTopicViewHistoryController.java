package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.UserTopicViewHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 话题的浏览记录相关请求接口类
 */
@RestController
@RequestMapping("/history")
public class UserTopicViewHistoryController {
    @Resource
    private UserTopicViewHistoryService userTopicViewHistoryService;
    
    /**
     * 查询我的话题浏览记录
     *
     * @return
     */
    @GetMapping
    public Result list(Search search) {
        return Result.success(userTopicViewHistoryService.page(search));
    }
    
    /**
     * 用户点击某话题，记录浏览记录
     *
     * @param id
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.UpdateLock)
    @PostMapping("/{id}")
    public Result insertOrUpdate(@PathVariable Long id) {
        userTopicViewHistoryService.insertOrUpdate(id);
        return Result.success();
    }
    
    /**
     * 已知主体id记录话题浏览记录
     *
     * @param id
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.UpdateLock)
    @PostMapping("/thing/{id}")
    public Result insertOrUpdateByThingId(@PathVariable Long id) {
        userTopicViewHistoryService.insertOrUpdateByThingId(id);
        return Result.success();
    }
    
    /**
     * 清空浏览历史记录
     *
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    @DeleteMapping
    public Result delete() {
        userTopicViewHistoryService.delete();
        return Result.success();
    }
}
