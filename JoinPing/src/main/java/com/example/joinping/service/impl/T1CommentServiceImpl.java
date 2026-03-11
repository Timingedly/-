package com.example.joinping.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.joinping.entity.extra.BusinessException;
import com.example.joinping.entity.extra.OllamaMessage;
import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferT1Comment;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.vo.PageResult;
import com.example.joinping.enums.MessageRoutingKeyEnum;
import com.example.joinping.mapper.BufferT1CommentMapper;
import com.example.joinping.mapper.T1CommentMapper;
import com.example.joinping.mapper.T2CommentMapper;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.service.T1CommentService;
import com.example.joinping.utils.CommonUtils;
import com.example.joinping.utils.RabbitMQUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class T1CommentServiceImpl implements T1CommentService {
    @Resource
    private T1CommentMapper t1CommentMapper;
    @Resource
    private T2CommentMapper t2CommentMapper;
    @Resource
    private BufferT1CommentMapper bufferT1CommentMapper;
    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Resource
    private ThingMapper thingMapper;
    @Value("${limit.max.length.comment}")
    private Integer commentMaxLength;
    
    @Override
    public T1Comment getById(Long id) {
        Assert.notNull(id, "一级评论id为空");
        T1Comment t1Comment = t1CommentMapper.getById(id, StpUtil.getLoginIdAsLong());
        Assert.notNull(t1Comment, "一级评论不存在");
        if (ObjectUtil.hasNull(t1Comment.getThingId(), thingMapper.selectById(t1Comment.getThingId()))) {
            throw new BusinessException("评论不存在");
        }
        return t1Comment;
    }
    
    
    @Override
    @Transactional
    public void insert(BufferT1Comment bufferT1Comment) {
        Assert.notNull(bufferT1Comment, "传入一级评论为空");
        if (StrUtil.isBlank(bufferT1Comment.getContent())) {
            throw new BusinessException("一级评论内容为空");
        }
        if (bufferT1Comment.getContent().length() > commentMaxLength) {
            throw new BusinessException("评论超过最大字数");
        }
        CommonUtils.initPojoCommonProperties(bufferT1Comment);
        //往缓冲表里插入记录
        bufferT1CommentMapper.insert(bufferT1Comment);
        rabbitMQUtils.sendOllamaMessageToNormalDirectExchange(new OllamaMessage(
                bufferT1Comment.getId(),
                MessageRoutingKeyEnum.T1COMMENT.getValue(),
                bufferT1Comment.getContent(),
                StpUtil.getLoginIdAsLong()
        ));
    }
    
    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Assert.notNull(id, "传入一级评论id为空");
        //判断当前用户是否有权限删除评论
        T1Comment t1Comment = t1CommentMapper.selectById(id);
        if (ObjectUtil.notEqual(userId, t1Comment.getUserId())) {
            throw new BusinessException("当前用户无权限删除该评论");
        }
        //删除该一级评论
        t1CommentMapper.deleteById(id);
        //根据一级评论id删除所有关联的二级评论
        t2CommentMapper.deleteByT1CommentId(id);
        //减少总评论数
        thingMapper.changeCommentSum(t1Comment.getThingId(), -(t1Comment.getReplyNum() + 1));
    }
    
    @Override
    public Thing getIndexThing(Long id) {
        Assert.notNull(id, "传入一级评论id为空");
        T1Comment t1Comment = t1CommentMapper.selectById(id);
        Assert.notNull(t1Comment, "查询的一级评论不存在");
        Integer beforeNum = t1CommentMapper.getIndexById(id, t1Comment.getThingId());
        int pageNum = (int) Math.ceil(beforeNum / 10);
        int pageSize = 10;
        Thing thing = new Thing();
        thing.setId(t1Comment.getThingId());
        thing.setPageNum(pageNum);
        thing.setPageSize(pageSize);
        thing.setIndex((beforeNum % 10) + 1);
        return thing;
    }
    
    
    @Override
    public PageResult<T1Comment> selectPage(Thing thing) {
        if (ObjectUtil.hasNull(thing, thing.getId(), thingMapper.selectById(thing.getId()))) {
            throw new BusinessException("查询的主体不存在");
        }
        PageHelper.startPage(thing.getPageNum(), thing.getPageSize());
        List<T1Comment> t1CommentList;
        if (thing.getOrderByCreateTimeDesc()) {
            t1CommentList = t1CommentMapper.getByThingIdNewFirst(thing.getId(), StpUtil.getLoginIdAsLong());
        } else {
            t1CommentList = t1CommentMapper.getByThingIdHotFirst(thing.getId(), StpUtil.getLoginIdAsLong());
        }
        return PageResult.convert(new PageInfo<>(t1CommentList));
    }
    
    @Override
    public PageResult selectT1CommentPage(Search search) {
        PageHelper.startPage(search.getPageNum(), search.getPageSize());
        return PageResult.convert(
                new PageInfo<>(
                        t1CommentMapper.selectUserReplyWithThingName(StpUtil.getLoginIdAsLong())
                )
        );
    }
    
}
