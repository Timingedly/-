package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.relaPojo.TopicUserFavorite;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.StatusEnum;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.mapper.TopicUserFavoriteMapper;
import com.example.joinping.service.TopicUserFavoriteService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.RedisUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Slf4j
public class TopicUserFavoriteServiceImpl implements TopicUserFavoriteService {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private TopicUserFavoriteMapper topicUserFavoriteMapper;
    
    @Override
    @Transactional
    public void confirmFavorite(TopicUserFavorite topicUserFavorite) {
        if (!hasRight(true, topicUserFavorite)) {
            return;
        }
        if (redisUtils.existTopic(topicUserFavorite.getTopicId())) {
            //热榜话题
            redisUtils.updateIndexTopicLikeOrFavoriteNum(topicUserFavorite.getTopicId(), false, true);
        } else {
            //普通话题
            Topic topic = topicMapper.selectById(topicUserFavorite.getTopicId());
            topic.setFavoriteNum(topic.getFavoriteNum() + 1);
            topicMapper.updateById(topic);
        }
    }
    
    @Override
    @Transactional
    public void cancelFavorite(TopicUserFavorite topicUserFavorite) {
        if (!hasRight(false, topicUserFavorite)) {
            return;
        }
        if (redisUtils.existTopic(topicUserFavorite.getTopicId())) {
            //热榜话题
            redisUtils.updateIndexTopicLikeOrFavoriteNum(topicUserFavorite.getTopicId(), false, false);
        } else {
            //普通话题
            Topic topic = topicMapper.selectById(topicUserFavorite.getTopicId());
            topic.setFavoriteNum(topic.getFavoriteNum() - 1);
            topicMapper.updateById(topic);
        }
    }
    
    /**
     * 权限校验（防止重复收藏/取消收藏）
     *
     * @param trueConfirm_falseCancel true表示收藏，false表示取消收藏
     * @param topicUserFavorite       收藏关系对象
     * @return 是否有权限执行后续操作(true有权限)
     */
    private Boolean hasRight(Boolean trueConfirm_falseCancel, TopicUserFavorite topicUserFavorite) {
        Assert.notNull(topicUserFavorite.getTopicId(), "收藏的话题id为空");
        if (ObjectUtil.isNull(topicMapper.selectById(topicUserFavorite.getTopicId()))) {
            log.error("要收藏的话题不存在");
            return false;
        }
        CommonUtils.initRelaPojoCommonProperties(topicUserFavorite);
        if (trueConfirm_falseCancel) {
            //想要点赞
            try {
                topicUserFavoriteMapper.insert(topicUserFavorite);
                //不报错就是首次收藏
                return true;
            } catch (DuplicateKeyException e) {
                //当前用户对当前话题已经存在收藏记录
                int updateRows = topicUserFavoriteMapper.insertByUpdate(topicUserFavorite.getTopicId(), topicUserFavorite.getUserId(), topicUserFavorite.getCreateTime());
                if (updateRows > 0) {
                    //恢复收藏
                    return true;
                } else {
                    //重复收藏
                    log.error("收藏失败，重复收藏");
                    return false;
                }
            }
        } else {
            //想要取消收藏
            int updateRows = topicUserFavoriteMapper.delete(topicUserFavorite.getTopicId(), topicUserFavorite.getUserId());
            if (updateRows > 0) {
                return true;
            } else {
                log.error("取消收藏失败，未曾收藏");
                return false;
            }
        }
    }
    
    @Override
    public Boolean getUserFavoriteStatus(Long id) {
        Assert.notNull(id, "话题id为空");
        TopicUserFavorite selectIgnoreStatus = topicUserFavoriteMapper.selectIgnoreStatus(id, StpUtil.getLoginIdAsLong());
        if (ObjectUtil.isNull(selectIgnoreStatus)) {
            return false;
        } else if (ObjectUtil.equals(selectIgnoreStatus.getStatus(), StatusEnum.DELETED.getValue())) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public PageResult selectPage(Search search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        return PageResult.convert(new PageInfo<>(topicUserFavoriteMapper.selectTopicPage(StpUtil.getLoginIdAsLong())));
    }
}