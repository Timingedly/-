package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.T2Comment;
import com.example.joinping.mapper.T2CommentMapper;
import com.example.joinping.utils.service.T2CommentServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class T2CommentServiceImplUtils extends ServiceImpl<T2CommentMapper, T2Comment> implements T2CommentServiceUtils {
}
