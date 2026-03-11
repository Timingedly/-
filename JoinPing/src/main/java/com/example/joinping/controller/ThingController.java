package com.example.joinping.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.pojo.BufferThing;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.ThingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 主体相关请求接口类
 */
@RestController
@RequestMapping("/thing")
public class ThingController {
    @Resource
    private ThingService thingService;
    
    /**
     * 根据id查询主体
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        return Result.success(thingService.getById(id));
    }
    
    
    /**
     * 新增主体
     *
     * @param bufferThing
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.InsertLock)
    @PutMapping
    public Result insert(@RequestBody BufferThing bufferThing) {
        thingService.insert(bufferThing);
        return Result.success();
    }
    
    /**
     * 删除主体
     *
     * @param id
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.DeleteLock)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        thingService.delete(id, StpUtil.getLoginIdAsLong());
        return Result.success();
    }
    
    /**
     * 分页查询主体列表
     *
     * @param topic
     * @return
     */
    @GetMapping
    public Result selectPage(Topic topic) {
        return Result.success(thingService.selectPage(topic));
    }
}
