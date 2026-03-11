package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.joinping.constant.RedisConstants;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.Notice;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.NoticeTypeEnum;
import com.example.joinping.enums.StatusEnum;
import com.example.joinping.mapper.NoticeMapper;
import com.example.joinping.service.NoticeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private RedisTemplate redisTemplate;
    
    @Override
    public PageResult getUserReplyNoticePage(Search search) {
        updateNoticeStatus();
        //返回通知列表
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        List<Notice> noticeList = noticeMapper.getPageWithUserName(StpUtil.getLoginIdAsLong(), NoticeTypeEnum.User.getValue());
        return PageResult.convert(new PageInfo<>(noticeList));
    }
    
    
    @Override
    public PageResult getSystemNoticePage(Search search) {
        updateNoticeStatus();
        return PageResult.convert(noticeMapper.selectPage(
                        new Page<>(search.getPageNum(), search.getPageSize()),
                        new LambdaQueryWrapper<Notice>()
                                .eq(Notice::getUserId, StpUtil.getLoginIdAsLong())
                                .eq(Notice::getType, NoticeTypeEnum.System.getValue())
                                .orderByDesc(Notice::getCreateTime)
                )
        );
    }
    
    @Override
    public Integer getStatus() {
        //先查缓存
        Object key = redisTemplate.opsForValue().get(RedisConstants.KEY_NOTICE + StpUtil.getLoginIdAsLong());
        if (ObjectUtil.isNotNull(key)) {
            return (int) key;
        }
        //再查数据库
        Notice notice = noticeMapper.selectTheNewest(StpUtil.getLoginIdAsLong());
        if (ObjectUtil.isNotNull(notice)) {
            return notice.getHasRead();
        }
        //都没有，则没有新消息
        return StatusEnum.DELETED.getValue();
    }
    
    /**
     * 更新通知状态为已读
     */
    private void updateNoticeStatus() {
        //用户点击了查看通知，所以先更新通知为已读
        redisTemplate.opsForValue().set(RedisConstants.KEY_NOTICE + StpUtil.getLoginIdAsLong(), StatusEnum.DELETED.getValue());
        noticeMapper.updateHasRead(StpUtil.getLoginIdAsLong(), StatusEnum.DELETED.getValue());
    }
    
    @Override
    public void deleteNotice(Integer type) {
        noticeMapper.delete(new LambdaQueryWrapper<Notice>().eq(Notice::getUserId, StpUtil.getLoginIdAsLong()).eq(Notice::getType, type));
    }
    
}
