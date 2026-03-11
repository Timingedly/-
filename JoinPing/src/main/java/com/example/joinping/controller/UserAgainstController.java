package com.example.joinping.controller;

import com.example.joinping.aop.anotation.DistributedOperationLock;
import com.example.joinping.entity.relaPojo.UserAgainst;
import com.example.joinping.entity.vo.Result;
import com.example.joinping.enums.OperationLockTypeEnum;
import com.example.joinping.service.UserAgainstService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 举报相关请求接口类类
 */
@RestController
@RequestMapping("/against")
public class UserAgainstController {
    @Resource
    private UserAgainstService userAgainstService;
    
    
    /**
     * 用户对内容进行举报
     *
     * @param userAgainst
     * @return
     */
    @DistributedOperationLock(OperationLockTypeEnum.AgainstLock)
    @PutMapping
    public Result against(UserAgainst userAgainst) {
        userAgainstService.against(userAgainst);
        return Result.success();
    }
    
    /**
     * 获取举报记录
     *
     * @param id
     * @return 0无举报记录1有举报记录
     */
    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        return Result.success(userAgainstService.get(id));
    }
    
}
