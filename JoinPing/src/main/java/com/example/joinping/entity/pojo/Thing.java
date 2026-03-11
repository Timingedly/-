package com.example.joinping.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.CommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;

/**
 * 主体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("thing")
public class Thing extends CommonPojo {
    
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
    
    /**
     * 平均评分(0未评分)
     */
    @TableField(exist = false)
    private Double averageScore;
    
    /**
     * 评分人数（文本）
     */
    @TableField(exist = false)
    private String customerNumText;
    
    /**
     * 评论总数（文本）
     */
    @TableField(exist = false)
    private String commentSumText;
    
    
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
    
    public Double getAverageScore() {
        DecimalFormat df = new DecimalFormat("#.0");
        if (customerNum != null) {
            double result = (double) score / customerNum;
            return Double.parseDouble(df.format(result));
        }
        return Double.parseDouble(df.format(0));
    }
    
    public String getCustomerNumText() {
        if (customerNum != null && customerNum >= 10000L) {
            double tenThousand = customerNum / 10000.0;
            // 保留一位小数 12345 -> 1.2w
            return String.format("%.1fw", tenThousand);
        } else if (customerNum != null) {
            return customerNum.toString();
        }
        return "0";
    }
    
    public String getCommentSumText() {
        if (commentSum != null && commentSum >= 10000L) {
            double tenThousand = commentSum / 10000.0;
            // 保留一位小数 12345 -> 1.2w
            return String.format("%.1fw", tenThousand);
        } else if (commentSum != null) {
            return commentSum.toString();
        }
        return "0";
    }
    
    
}
