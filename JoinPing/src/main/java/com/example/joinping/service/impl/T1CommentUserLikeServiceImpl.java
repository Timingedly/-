package com.example.joinping.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.relaPojo.T1CommentUserLike;
import com.example.joinping.mapper.T1CommentMapper;
import com.example.joinping.mapper.T1CommentUserLikeMapper;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.service.T1CommentUserLikeService;
import com.example.joinping.utils.CommonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@Slf4j
public class T1CommentUserLikeServiceImpl implements T1CommentUserLikeService {
    @Resource
    private T1CommentUserLikeMapper t1CommentUserLikeMapper;
    @Resource
    private T1CommentMapper t1CommentMapper;
    @Resource
    private ThingMapper thingMapper;
    
    @Override
    @Transactional
    public void confirmLike(T1CommentUserLike t1CommentUserLike) {
        if (!hasRight(true, t1CommentUserLike)) {
            return;
        }
        T1Comment t1Comment = t1CommentMapper.selectById(t1CommentUserLike.getT1CommentId());
        t1Comment.setLikeNum(t1Comment.getLikeNum() + 1);
        t1CommentMapper.updateById(t1Comment);
        
    }
    
    @Override
    @Transactional
    public void cancelLike(T1CommentUserLike t1CommentUserLike) {
        if (!hasRight(false, t1CommentUserLike)) {
            return;
        }
        T1Comment t1Comment = t1CommentMapper.selectById(t1CommentUserLike.getT1CommentId());
        t1Comment.setLikeNum(t1Comment.getLikeNum() - 1);
        t1CommentMapper.updateById(t1Comment);
    }
    
    /**
     * 权限校验（防止重复点赞/取消点赞）
     *
     * @param trueConfirm_falseCancel
     * @param t1CommentUserLike
     * @return 是否有权限执行后续操作(true有权限)
     */
    private Boolean hasRight(Boolean trueConfirm_falseCancel, T1CommentUserLike t1CommentUserLike) {
        Assert.notNull(t1CommentUserLike.getT1CommentId(), "点赞的一级评论id为空");
        T1Comment t1Comment = t1CommentMapper.selectById(t1CommentUserLike.getT1CommentId());
        if (ObjectUtil.isNull(t1Comment)) {
            log.error("要点赞的评论不存在");
            return false;
        }
        if (ObjectUtil.isNull(thingMapper.selectById(t1Comment.getThingId()))) {
            log.error("要点赞的评论不存在");
            return false;
        }
        CommonUtils.initRelaPojoCommonProperties(t1CommentUserLike);
        if (trueConfirm_falseCancel) {
            //想要点赞
            try {
                t1CommentUserLikeMapper.insert(t1CommentUserLike);
                //不报错就是首次点赞
                return true;
            } catch (DuplicateKeyException e) {
                //当前用户对当前话题已经存在点赞记录
                int updateRows = t1CommentUserLikeMapper.insertByUpdate(t1CommentUserLike.getT1CommentId(), t1CommentUserLike.getUserId(), t1CommentUserLike.getCreateTime());
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
            int updateRows = t1CommentUserLikeMapper.delete(t1CommentUserLike.getT1CommentId(), t1CommentUserLike.getUserId());
            if (updateRows > 0) {
                return true;
            } else {
                log.error("取消点赞失败，未曾点赞");
                return false;
            }
        }
    }
}
