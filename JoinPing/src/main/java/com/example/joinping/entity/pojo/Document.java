package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("document")
public class Document extends CommonPojo {
    /**
     * 文件访问地址
     */
    private String url;
    /**
     * 实际存储地址
     */
    private String path;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 文件拓展名
     */
    private String extra;
    /**
     * 上传文件的用户id
     */
    private Long userId;
    
    /**
     * 初始化属性
     *
     * @param url
     * @param path
     * @param name
     * @param extra
     * @param userId
     */
    public void initProperties(String url, String path, String name, String extra, Long userId) {
        this.url = url;
        this.path = path;
        this.name = name;
        this.extra = extra;
        this.userId = userId;
    }
}
