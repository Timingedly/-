package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.NoticeTypeEnum;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知相关请求接口类
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Resource
    private NoticeService noticeService;
    
    /**
     * 分页查询回复我的通知记录
     *
     * @param search
     * @return
     */
    @GetMapping("/reply")
    public Result getUserReplyNoticePage(Search search) {
        return Result.success(noticeService.getUserReplyNoticePage(search));
    }
    
    /**
     * 获取系统通知记录
     *
     * @param search
     * @return
     */
    @GetMapping("/system")
    public Result getSystemNoticePage(Search search) {
        return Result.success(noticeService.getSystemNoticePage(search));
    }
    
    /**
     * 清空系统通知
     *
     * @return
     */
    @DeleteMapping("/system")
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    public Result deleteSystemNotice() {
        noticeService.deleteNotice(NoticeTypeEnum.System.getValue());
        return Result.success();
    }
    
    /**
     * 清空用户回复通知
     *
     * @return
     */
    @DeleteMapping("/reply")
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    public Result deleteUserReplyNotice() {
        noticeService.deleteNotice(NoticeTypeEnum.User.getValue());
        return Result.success();
    }
    
    
    /**
     * 查询是否有未读新通知(0有1没有)
     *
     * @return
     */
    @GetMapping("/status")
    public Result getStatus() {
        return Result.success(noticeService.getStatus());
    }
}
