package com.example.joinping.entity.relaPojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.joinping.entity.common.RelaCommonPojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户与举报记录的关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_against")
public class UserAgainst extends RelaCommonPojo {
    /**
     * 举报的对象id
     */
    private Long objectId;
    /**
     * 举报的对象类型
     *
     * @see com.example.joinping.enums.MessageRoutingKeyEnum
     */
    private Integer type;
}
