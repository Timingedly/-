package com.example.joinping.service;

import com.example.joinping.entity.extra.Search;
import com.example.joinping.entity.pojo.BufferT1Comment;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.entity.vo.PageResult;

public interface T1CommentService {
    
    /**
     * 根据id查询一级评论
     *
     * @param id
     * @return
     */
    T1Comment getById(Long id);
    
    /**
     * 新增一级评论
     *
     * @param bufferT1Comment
     */
    void insert(BufferT1Comment bufferT1Comment);
    
    /**
     * 删除一级评论
     *
     * @param id
     */
    void delete(Long id, Long userId);
    
    /**
     * 根据id返回评论所在页信息
     *
     * @param id
     * @return
     */
    Thing getIndexThing(Long id);
    
    /**
     * 分页查询一级评论列表
     *
     * @param thing
     * @return
     */
    PageResult<T1Comment> selectPage(Thing thing);
    
    /**
     * 分页查询我的一级评论记录
     *
     * @param search
     * @return
     */
    PageResult selectT1CommentPage(Search search);
}
