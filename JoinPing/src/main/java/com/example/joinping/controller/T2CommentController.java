package com.example.joinping.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferT2Comment;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.T2CommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 二级评论相关请求接口类
 */
@RestController
@RequestMapping("/t2comment")
public class T2CommentController {
    @Resource
    private T2CommentService t2CommentService;
    
    /**
     * 分页查询一级评论下的二级评论
     *
     * @param t1Comment
     * @return
     */
    @GetMapping
    public Result selectPage(T1Comment t1Comment) {
        return Result.success(t2CommentService.selectPage(t1Comment));
    }
    
    /**
     * 新增二级评论
     *
     * @param bufferT2Comment
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    @PutMapping
    public Result insert(@RequestBody BufferT2Comment bufferT2Comment) {
        t2CommentService.insert(bufferT2Comment);
        return Result.success();
    }
    
    /**
     * 删除二级评论
     *
     * @param id
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        t2CommentService.delete(id, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
    
    /**
     * 根据id返回二级评论所在页面信息
     *
     * @param id
     * @return
     */
    @GetMapping("/index/{id}")
    public Result getT1CommentPageInfo(@PathVariable Long id) {
        return Result.success(t2CommentService.getT1Comment(id));
    }
    
    /**
     * 分页查询我的二级评论记录
     *
     * @param search
     * @return
     */
    @GetMapping("/my")
    public Result selectT2CommentPage(Search search) {
        return Result.success(t2CommentService.selectT2CommentPage(search));
    }
}
