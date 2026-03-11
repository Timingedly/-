package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.relaPojo.UserTopicViewHistory;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.StatusEnum;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.mapper.UserTopicViewHistoryMapper;
import com.example.joinping.service.UserTopicViewHistoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Service
public class UserTopicViewHistoryServiceImpl implements UserTopicViewHistoryService {
    @Resource
    private UserTopicViewHistoryMapper userTopicViewHistoryMapper;
    @Resource
    private ThingMapper thingMapper;
    
    @Override
    public PageResult<Topic> page(Search search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        return PageResult.convert(new PageInfo<>(userTopicViewHistoryMapper.list(StpUtil.getLoginIdAsLong())));
    }
    
    @Override
    @Transactional
    public void insertOrUpdate(Long id) {
        Assert.notNull(id, "浏览的话题的id为空");
        UserTopicViewHistory anyStatus = userTopicViewHistoryMapper.getAnyStatus(id, StpUtil.getLoginIdAsLong());
        LocalDateTime now = LocalDateTime.now();
        if (ObjectUtil.isNull(anyStatus)) {
            //从未浏览过，生成一条浏览记录
            userTopicViewHistoryMapper.insert(new UserTopicViewHistory(StpUtil.getLoginIdAsLong(), StatusEnum.NORMAL.getValue(), id, now));
        } else {
            //重复浏览，更新浏览时间即可
            userTopicViewHistoryMapper.updateUpdateTime(id, StpUtil.getLoginIdAsLong(), now);
        }
    }
    
    @Override
    public void insertOrUpdateByThingId(Long id) {
        Assert.notNull(id, "传入的主体id为空");
        Thing thing = thingMapper.selectById(id);
        if (ObjectUtil.hasNull(thing, thing.getTopicId())) {
            throw new BusinessException("话题不存在或已被删除");
        }
        insertOrUpdate(thing.getTopicId());
    }
    
    @Override
    public void delete() {
        userTopicViewHistoryMapper.delete(new LambdaQueryWrapper<UserTopicViewHistory>().eq(UserTopicViewHistory::getUserId, StpUtil.getLoginIdAsLong()));
    }
}
