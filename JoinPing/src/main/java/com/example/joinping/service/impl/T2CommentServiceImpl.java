package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferT2Comment;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.pojo.T2Comment;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.example.joinping.mapper.BufferT2CommentMapper;
import com.example.joinping.mapper.T1CommentMapper;
import com.example.joinping.mapper.T2CommentMapper;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.service.T2CommentService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.RabbitMQUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class T2CommentServiceImpl implements T2CommentService {
    @Resource
    private T2CommentMapper t2CommentMapper;
    @Resource
    private BufferT2CommentMapper bufferT2CommentMapper;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Resource
    private T1CommentMapper t1CommentMapper;
    @Resource
    private ThingMapper thingMapper;
    @Value("${limit.max.length.comment}")
    private Integer commentMaxLength;
    
    
    @Override
    @Transactional
    public void insert(BufferT2Comment bufferT2Comment) {
        if (ObjectUtil.hasNull(bufferT2Comment, bufferT2Comment.getT1commentId(), bufferT2Comment.getReplyUserId())) {
            throw new BusinessException("发送的评论格式错误");
        }
        ;
        if (StrUtil.isBlank(bufferT2Comment.getContent())) {
            throw new BusinessException("二级评论内容为空");
        }
        if (bufferT2Comment.getContent().length() > commentMaxLength) {
            throw new BusinessException("评论超过最大字数");
        }
        CommonUtils.initPojoCommonProperties(bufferT2Comment);
        bufferT2CommentMapper.insert(bufferT2Comment);
        rabbitMQUtils.sendOllamaMessageToNormalDirectExchange(new OllamaMessage(
                bufferT2Comment.getId(),
                MessageRoutingKeyEnum.T2COMMENT.getValue(),
                bufferT2Comment.getContent(),
                StpUtil.getLoginIdAsLong()
        ));
    }
    
    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Assert.notNull(id, "传入二级评论id为空");
        //判断当前用户是否有权限删除评论
        T2Comment t2Comment = t2CommentMapper.selectById(id);
        if (ObjectUtil.notEqual(userId, t2Comment.getUserId())) {
            throw new BusinessException("当前用户无权限删除该评论");
        }
        t2CommentMapper.deleteById(id);
        t1CommentMapper.changeCommentSum(t2Comment.getT1commentId(), -1);
        thingMapper.changeCommentSum(t2Comment.getThingId(), -1);
    }
    
    @Override
    public T1Comment getT1Comment(Long id) {
        Assert.notNull(id, "传入二级评论id为空");
        T2Comment t2Comment = t2CommentMapper.selectById(id);
        Assert.notNull(t2Comment, "查询的二级评论不存在");
        Integer beforeNum = t2CommentMapper.getIndexById(id, t2Comment.getT1commentId());
        int pageNum = (int) Math.ceil(beforeNum / 10);
        int pageSize = 10;
        T1Comment t1Comment = new T1Comment();
        t1Comment.setId(t2Comment.getT1commentId());
        t1Comment.setThingId(t2Comment.getThingId());
        t1Comment.setPageNum(pageNum);
        t1Comment.setPageSize(pageSize);
        t1Comment.setIndex((beforeNum % 10) + 1);
        return t1Comment;
    }
    
    @Override
    public PageResult selectPage(T1Comment t1Comment) {
        if (ObjectUtil.hasNull(t1Comment, t1Comment.getId(), t1Comment.getThingId())) {
            throw new BusinessException("评论不存在");
        }
        if (t2CommentMapper.checkT1CommentIsExisting(t1Comment.getThingId(), t1Comment.getId()) == 0) {
            throw new BusinessException("评论不存在");
        }
        PageHelper.startPage(t1Comment.getPageNum(), t1Comment.getPageSize());
        return PageResult.convert(new PageInfo<>(t2CommentMapper.getByT1CommentId(t1Comment.getId())));
    }
    
    @Override
    public PageResult selectT2CommentPage(Search search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        return PageResult.convert(
                new PageInfo<>(
                        t2CommentMapper.selectUserReplyWithReplyUserName(StpUtil.getLoginIdAsLong())
                )
        );
    }
}
