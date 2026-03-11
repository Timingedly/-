package com.example.joinping.utils.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.joinping.entity.pojo.Thing;
import com.example.joinping.mapper.ThingMapper;
import com.example.joinping.utils.service.ThingServiceUtils;
import org.springframework.stereotype.Service;

@Service
public class ThingServiceImplUtils extends ServiceImpl<ThingMapper, Thing> implements ThingServiceUtils {
}
