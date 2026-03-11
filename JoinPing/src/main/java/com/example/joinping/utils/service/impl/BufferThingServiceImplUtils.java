package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.BufferThing;
import com.example.joinping.mapper.BufferThingMapper;
import com.example.joinping.utils.service.BufferThingServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class BufferThingServiceImplUtils extends ServiceImpl<BufferThingMapper, BufferThing> implements BufferThingServiceUtils {
}
