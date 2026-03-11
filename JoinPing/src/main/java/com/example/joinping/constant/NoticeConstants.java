package com.example.joinping.constant;

/**
 * 通知相关常量类
 */
public class NoticeConstants {
    public static final String TOPIC_NOT_PASS = "新建话题未通过审核";
    
    public static final String THING_NOT_PASS = "新建主体未通过审核";
    
    public static final String COMMENT_NOT_PASS = "评论未通过审核";
    
    /**
     * 获取被用户评论的通知文本
     *
     * @param userId
     * @return
     */
    public static String getReplyContent(Long userId) {
        return "用户 " + userId + "回复了您的评论";
    }
    
    /**
     * 获得对话题的举报结果
     *
     * @param hasSuccessed
     * @return
     */
    public static String getTopicAgainstResult(Boolean hasSuccessed) {
        if (hasSuccessed) {
            return "成功举报话题，感谢你为维护社区做出的贡献";
        } else {
            return "您举报的话题未发现异常";
        }
    }
    
    /**
     * 获得对主体的举报结果
     *
     * @param hasSuccessed
     * @return
     */
    public static String getThingAgainstResult(Boolean hasSuccessed) {
        if (hasSuccessed) {
            return "成功举报主体，感谢你为维护社区做出的贡献";
        } else {
            return "您举报的主体未发现异常";
        }
    }
    
    /**
     * 获得对评论的举报结果
     *
     * @param hasSuccessed
     * @return
     */
    public static String getCommentAgainstResult(Boolean hasSuccessed) {
        if (hasSuccessed) {
            return "成功举报评论，感谢你为维护社区做出的贡献";
        } else {
            return "您举报的评论未发现异常";
        }
    }
}
