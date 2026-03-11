package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.joinping.entity.pojo.Topic;
import com.example.joinping.entity.relaPojo.TopicUserLike;
import com.example.joinping.enums.StatusEnum;
import com.example.joinping.mapper.TopicMapper;
import com.example.joinping.mapper.TopicUserLikeMapper;
import com.example.joinping.service.TopicUserLikeService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.RedisUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Slf4j
public class TopicUserLikeServiceImpl implements TopicUserLikeService {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private TopicUserLikeMapper topicUserLikeMapper;
    
    @Override
    @Transactional
    public void confirmLike(TopicUserLike topicUserLike) {
        if (!hasRight(true, topicUserLike)) {
            return;
        }
        if (redisUtils.existTopic(topicUserLike.getTopicId())) {
            //热榜话题
            redisUtils.updateIndexTopicLikeOrFavoriteNum(topicUserLike.getTopicId(), true, true);
        } else {
            //普通话题
            Topic topic = topicMapper.selectById(topicUserLike.getTopicId());
            topic.setLikeNum(topic.getLikeNum() + 1);
            topicMapper.updateById(topic);
        }
    }
    
    @Override
    @Transactional
    public void cancelLike(TopicUserLike topicUserLike) {
        if (!hasRight(false, topicUserLike)) {
            return;
        }
        if (redisUtils.existTopic(topicUserLike.getTopicId())) {
            //热榜话题
            redisUtils.updateIndexTopicLikeOrFavoriteNum(topicUserLike.getTopicId(), true, false);
        } else {
            //普通话题
            Topic topic = topicMapper.selectById(topicUserLike.getTopicId());
            topic.setLikeNum(topic.getLikeNum() - 1);
            topicMapper.updateById(topic);
        }
    }
    
    /**
     * 权限校验（防止重复点赞/取消点赞）
     *
     * @param trueConfirm_falseCancel
     * @param topicUserLike
     * @return 是否有权限执行后续操作(true有权限)
     */
    private Boolean hasRight(Boolean trueConfirm_falseCancel, TopicUserLike topicUserLike) {
        Assert.notNull(topicUserLike.getTopicId(), "点赞的话题id为空");
        if (ObjectUtil.isNull(topicMapper.selectById(topicUserLike.getTopicId()))) {
            log.error("要点赞的话题不存在");
            return false;
        }
        CommonUtils.initRelaPojoCommonProperties(topicUserLike);
        if (trueConfirm_falseCancel) {
            //想要点赞
            try {
                topicUserLikeMapper.insert(topicUserLike);
                //不报错就是首次点赞
                return true;
            } catch (DuplicateKeyException e) {
                //当前用户对当前话题已经存在点赞记录
                int updateRows = topicUserLikeMapper.insertByUpdate(topicUserLike.getTopicId(), topicUserLike.getUserId(), topicUserLike.getCreateTime());
                if (updateRows > 0) {
                    //恢复点赞
                    return true;
                } else {
                    //重复点赞
                    log.error("点赞失败，重复点赞");
                    return false;
                }
            }
        } else {
            //想要取消点赞
            int updateRows = topicUserLikeMapper.delete(topicUserLike.getTopicId(), topicUserLike.getUserId());
            if (updateRows > 0) {
                return true;
            } else {
                log.error("取消点赞失败，未曾点赞");
                return false;
            }
        }
    }
    
    @Override
    public Boolean userLiked(TopicUserLike topicUserLike) {
        Assert.notNull(topicUserLike.getTopicId(), "话题id为空");
        TopicUserLike selectIgnoreStatus = topicUserLikeMapper.selectIgnoreStatus(topicUserLike.getTopicId(), StpUtil.getLoginIdAsLong());
        if (ObjectUtil.isNull(selectIgnoreStatus)) {
            return false;
        } else if (ObjectUtil.equals(selectIgnoreStatus.getStatus(), StatusEnum.DELETED.getValue())) {
            return false;
        } else {
            return true;
        }
    }
}
