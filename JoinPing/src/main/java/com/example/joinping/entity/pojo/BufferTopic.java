package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 话题（缓冲）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("buffer_topic")
public class BufferTopic extends CommonPojo {
    
    /**
     * 话题名称
     */
    private String name;
    
    /**
     * 被点赞数
     */
    private Long likeNum;
    
    /**
     * 被收藏数
     */
    private Long favoriteNum;
    
    /**
     * 话题简介
     */
    private String content;
    
    /**
     * 图片id
     */
    private Long photoId;
    
    
    /**
     * 所属分区id
     */
    private Integer areaId;
    
    /**
     * 话题图片路径
     */
    private String photoUrl;
    
    /**
     * 用户id
     */
    private Long userId;
    
    public Long getLikeNum() {
        if (likeNum == null) {
            return 0L;
        }
        return likeNum;
    }
    
    public Long getFavoriteNum() {
        if (likeNum == null) {
            return 0L;
        }
        return favoriteNum;
    }
}
