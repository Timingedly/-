package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.T1Comment;
import com.example.joinping.mapper.T1CommentMapper;
import com.example.joinping.utils.service.T1CommentServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class T1CommentServiceImplUtils extends ServiceImpl<T1CommentMapper, T1Comment> implements T1CommentServiceUtils {
}
