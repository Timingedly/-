package com.example.joinping.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferT1Comment;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.T1CommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 一级评论相关请求接口类
 */
@RestController
@RequestMapping("/t1comment")
public class T1CommentController {
    @Resource
    private T1CommentService t1CommentService;
    
    /**
     * 分页查询一级评论列表
     *
     * @param thing
     * @return
     */
    @GetMapping
    public Result selectPage(Thing thing) {
        return Result.success(t1CommentService.selectPage(thing));
    }
    
    /**
     * 根据id查询一级评论
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable("id") Long id) {
        return Result.success(t1CommentService.getById(id));
    }
    
    /**
     * 新增一级评论
     *
     * @param bufferT1Comment
     * @return
     */
    @PutMapping
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    public Result insert(@RequestBody BufferT1Comment bufferT1Comment) {
        t1CommentService.insert(bufferT1Comment);
        return Result.success();
    }
    
    /**
     * 删除一级评论
     *
     * @param id
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        t1CommentService.delete(id, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
    
    /**
     * 根据id返回一级评论所在页面信息
     *
     * @param id
     * @return
     */
    @GetMapping("/index/{id}")
    public Result getThingPageInfo(@PathVariable Long id) {
        return Result.success(t1CommentService.getIndexThing(id));
    }
    
    /**
     * 分页查询我的一级评论记录
     *
     * @param search
     * @return
     */
    @GetMapping("/my")
    public Result selectT1CommentPage(Search search) {
        return Result.success(t1CommentService.selectT1CommentPage(search));
    }
}
