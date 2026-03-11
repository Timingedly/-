package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级评论
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t1comment")
public class T1Comment extends CommonPojo {
    
    /**
     * 所属的主体id
     */
    private Long thingId;
    
    /**
     * 被点赞数
     */
    private Long likeNum;
    
    /**
     * 被回复数
     */
    private Integer replyNum;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 主体名
     */
    @TableField(exist = false)
    private String thingName;
    
    /**
     * 图片路径
     */
    @TableField(exist = false)
    private String photoUrl;
    
    /**
     * 用户名
     */
    @TableField(exist = false)
    private String userName;
    
    
    /**
     * 已点赞
     */
    @TableField(exist = false)
    private Integer liked;
    
    /**
     * 点赞数的文本展示
     */
    @TableField(exist = false)
    private String likeNumText;
    
    /**
     * 回复数的文本展示
     */
    @TableField(exist = false)
    private String replyNumText;
    
    public Long getLikeNum() {
        if (likeNum == null) {
            return 0L;
        }
        return likeNum;
    }
    
    public Integer getReplyNum() {
        if (replyNum == null) {
            return 0;
        }
        return replyNum;
    }
    
    public String getLikeNumText() {
        if (likeNum != null && likeNum >= 10000L) {
            double tenThousand = likeNum / 10000.0;
            // 保留一位小数 12345 -> 1.2w
            return String.format("%.1fw", tenThousand);
        } else if (likeNum != null) {
            return likeNum.toString();
        }
        return "0";
    }
    
    public String getReplyNumText() {
        if (replyNum != null && replyNum >= 10000L) {
            double tenThousand = replyNum / 10000.0;
            // 保留一位小数 12345 -> 1.2w
            return String.format("%.1fw", tenThousand);
        } else if (replyNum != null) {
            return replyNum.toString();
        }
        return "0";
    }
}
