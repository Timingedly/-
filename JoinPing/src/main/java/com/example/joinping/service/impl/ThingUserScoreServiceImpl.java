package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.relaPojo.ThingUserScore;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.mapper.ThingUserScoreMapper;
import com.example.joinping.service.ThingUserScoreService;
import com.example.joinping.utils.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Slf4j
public class ThingUserScoreServiceImpl implements ThingUserScoreService {
    @Resource
    private ThingUserScoreMapper thingUserScoreMapper;
    @Resource
    private ThingMapper thingMapper;
    
    @Override
    public Integer getUserScore(ThingUserScore thingUserScore) {
        Assert.notNull(thingUserScore.getThingId(), "主体id为空");
        ThingUserScore selectOne = thingUserScoreMapper.selectOne(new LambdaQueryWrapper<ThingUserScore>().eq(ThingUserScore::getThingId, thingUserScore.getThingId()).eq(ThingUserScore::getUserId, StpUtil.getLoginIdAsLong()));
        if (ObjectUtil.isNull(selectOne)) {
            return 0;
        }
        return selectOne.getScore();
    }
    
    
    @Override
    @Transactional
    public void insertOrUpdateScore(ThingUserScore thingUserScore) {
        if (ObjectUtil.hasNull(thingUserScore, thingUserScore.getThingId(), thingUserScore.getScore())) {
            throw new BusinessException("评分格式错误");
        }
        Thing thing = thingMapper.selectById(thingUserScore.getThingId());
        Assert.notNull(thing, "要评分的主体不存在");
        if (thingUserScore.getScore() < 1 || thingUserScore.getScore() > 5) {
            throw new BusinessException("评分非法");
        }
        CommonUtils.initRelaPojoCommonProperties(thingUserScore);
        int score = thingUserScore.getScore() * 2;
        try {
            //首次评分
            thingUserScoreMapper.insert(thingUserScore);
            thingMapper.addScore(thingUserScore.getThingId(), score);
        } catch (DuplicateKeyException e) {
            //修改评分
            ThingUserScore selectOne = thingUserScoreMapper.selectOne(new LambdaQueryWrapper<ThingUserScore>().eq(ThingUserScore::getThingId, thingUserScore.getThingId()).eq(ThingUserScore::getUserId, StpUtil.getLoginIdAsLong()));
            thingUserScoreMapper.updateScore(thingUserScore.getThingId(), StpUtil.getLoginIdAsLong(), thingUserScore.getScore(), thingUserScore.getCreateTime());
            thing.setScore(thing.getScore() - selectOne.getScore() + score);
            thingMapper.updateById(thing);
        }
    }
}
