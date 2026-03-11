package com.example.joinping.service;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferT2Comment;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.vo.PageResult;

public interface T2CommentService {
    
    
    /**
     * 新增二级评论
     *
     * @param bufferT2Comment
     */
    void insert(BufferT2Comment bufferT2Comment);
    
    /**
     * 删除一级评论
     *
     * @param id
     */
    void delete(Long id, Long userId);
    
    /**
     * 根据id返回二级评论所在页面信息
     *
     * @param id
     * @return
     */
    T1Comment getT1Comment(Long id);
    
    /**
     * 分页查询二级评论列表
     *
     * @param t1Comment
     * @return
     */
    PageResult selectPage(T1Comment t1Comment);
    
    /**
     * 分页查询我的二级评论记录
     *
     * @param search
     * @return
     */
    PageResult selectT2CommentPage(Search search);
}
