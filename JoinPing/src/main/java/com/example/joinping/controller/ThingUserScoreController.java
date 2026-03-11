package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.relaPojo.ThingUserScore;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.ThingUserScoreService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 主体的评分相关请求接口类
 */
@RestController
@RequestMapping("/score")
public class ThingUserScoreController {
    @Resource
    private ThingUserScoreService thingUserScoreService;
    
    /**
     * 获取当前用户对于主体的评分
     *
     * @param thingUserScore
     * @return 返回0代表未评分，正常1-5
     */
    @GetMapping
    public Result getUserScore(ThingUserScore thingUserScore) {
        return Result.success(thingUserScoreService.getUserScore(thingUserScore));
    }
    
    /**
     * 对主体评分
     *
     * @return
     */
    @PostMapping
    @DistributedOperationLock(OperationLockTypeEnum.UpdateLock)
    public Result insertOrUpdateScore(@RequestBody ThingUserScore thingUserScore) {
        thingUserScoreService.insertOrUpdateScore(thingUserScore);
        return Result.success();
    }
    
}
