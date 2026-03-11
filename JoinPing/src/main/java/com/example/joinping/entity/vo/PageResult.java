package com.example.joinping.entity.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果封装类
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    /**
     * 核心数据列表
     */
    private List<T> list;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 当前页码
     */
    private Long current;
    /**
     * 每页大小
     */
    private Long size;
    /**
     * 总页数
     */
    private Long pages;
    /**
     * 是否有上一页(0无,1有)
     */
    private Boolean hasPrevious;
    /**
     * 是否有下一页(0无,1有)
     */
    private Boolean hasNext;
    
    /**
     * 将mybatis-plus的分页结果集封装
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> PageResult<T> convert(Page<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
    
    /**
     * 将pageHelper分页结果集封装
     *
     * @param pageInfo
     * @param <T>
     * @return
     */
    public static <T> PageResult<T> convert(PageInfo<T> pageInfo) {
        return new PageResult<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                (long) pageInfo.getPageNum(),
                (long) pageInfo.getPageSize(),
                (long) pageInfo.getPages(),
                pageInfo.isHasPreviousPage(),
                pageInfo.isHasNextPage()
        );
    }
}
