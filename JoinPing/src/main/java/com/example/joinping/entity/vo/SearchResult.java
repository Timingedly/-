package com.example.joinping.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ES搜索结果封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    /**
     * ES中返回的id列表
     */
    private List<Long> ids;
    /**
     * es中查询的总记录数
     */
    private Long total;
}
