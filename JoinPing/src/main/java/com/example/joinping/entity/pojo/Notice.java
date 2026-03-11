package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notice")
public class Notice extends CommonPojo {
    
    /**
     * 消息对象的id（目前只存储二级评论回复的通知）
     */
    private Long objectId;
    /**
     * 这个通知由谁发出
     */
    private Long fromUserId;
    
    /**
     * 是否已读（0未读1已读）
     */
    private Integer hasRead;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 通知类型
     */
    private Integer type;
    
    /**
     * 这个通知的由谁发出
     */
    @TableField(exist = false)
    private String fromUserName;
    
    public Integer getHasRead() {
        if (hasRead == null) {
            return 0;
        }
        return hasRead;
    }
    
    
}
