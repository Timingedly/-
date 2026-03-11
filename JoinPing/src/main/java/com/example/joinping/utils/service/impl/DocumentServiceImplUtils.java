package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.Document;
import com.example.joinping.mapper.DocumentMapper;
import com.example.joinping.utils.service.DocumentServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class DocumentServiceImplUtils extends ServiceImpl<DocumentMapper, Document> implements DocumentServiceUtils {
}
