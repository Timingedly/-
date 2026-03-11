package com.example.joinping.service;

import com.example.joinping.entity.relaPojo.UserAgainst;

public interface UserAgainstService {
    /**
     * 用户对内容进行举报
     *
     * @param userAgainst
     * @param type
     */
    void against(UserAgainst userAgainst);
    
    /**
     * 查询举报记录
     *
     * @param id
     * @return
     */
    Integer get(Long id);
}
