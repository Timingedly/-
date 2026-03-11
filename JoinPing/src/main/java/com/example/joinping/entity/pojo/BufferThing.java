package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 主体（缓冲）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("buffer_thing")
public class BufferThing extends CommonPojo {
    
    /**
     * 所属的话题ID
     */
    private Long topicId;
    
    /**
     * 主体名称
     */
    private String name;
    
    /**
     * 总评分
     */
    private Long score;
    
    /**
     * 参与评分人数
     */
    private Long customerNum;
    
    /**
     * 评论总数
     */
    private Integer commentSum;
    
    /**
     * 图片id
     */
    private Long photoId;
    /**
     * 话题图片路径
     */
    private String photoUrl;
    
    
    public Long getCustomerNum() {
        if (customerNum == null) {
            return 0L;
        }
        return customerNum;
    }
    
    public Integer getCommentSum() {
        if (commentSum == null) {
            return 0;
        }
        return commentSum;
    }
    
    public Long getScore() {
        if (score == null) {
            return 0L;
        }
        return score;
    }
}
