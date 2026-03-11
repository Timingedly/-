create table buffer_t1comment
(
    id         bigint   not null
        primary key,
    status     int      null,
    createTime datetime null,
    updateTime datetime null,
    thingId    bigint   null comment '所属主体id',
    userId     bigint   null comment '用户id',
    likeNum    bigint   null comment '被推荐数',
    replyNum   int      null comment '被回复数',
    content    text     null comment '评论内容',
    constraint id
        unique (id)
)
    comment '一级评论缓存表';

create table buffer_t2comment
(
    id          bigint   not null
        primary key,
    status      int      null,
    createTime  datetime null,
    updateTime  datetime null,
    t1CommentId bigint   null comment '所属一级评论id',
    replyUserId bigint   null comment '所回复的评论的发布者id',
    userId      bigint   null comment '用户id',
    content     text     null comment '评论内容',
    thingId     bigint   null,
    replyType   int      null,
    constraint id
        unique (id)
)
    comment '二级评论缓存表';

create table buffer_thing
(
    id          bigint       not null
        primary key,
    status      int          null,
    createTime  datetime     null,
    updateTime  datetime     null,
    topicId     bigint       null comment '所属话题id',
    name        varchar(200) null comment '主体名',
    score       bigint       null comment '总评分',
    customerNum bigint       null comment '参评人数',
    photoId     bigint       null comment '图片id',
    commentSum  int          null comment '评论总数',
    photoUrl    varchar(200) null comment '图片路径',
    userId      bigint       null,
    constraint id
        unique (id)
)
    comment '主体缓存表';

create table buffer_topic
(
    id          bigint       not null
        primary key,
    status      int          null,
    createTime  datetime     null,
    updateTime  datetime     null,
    name        varchar(200) null comment '名字',
    likeNum     bigint       null comment '推荐数',
    favoriteNum bigint       null comment '收藏数',
    content     text         null comment '简介',
    photoId     bigint       null comment '图片id',
    areaId      int          null comment '所属分区id',
    photoUrl    varchar(200) null comment '图片路径',
    userId      bigint       null,
    constraint id
        unique (id)
)
    comment '主题缓存表';

create table commontable
(
    id         bigint   not null comment '唯一递增主键'
        primary key,
    status     int      null comment '逻辑删除(0未删除1已删除)',
    createTime datetime null comment '创建时间',
    updateTime datetime null comment '修改时间'
);

create table document
(
    id         bigint       not null
        primary key,
    status     int          null,
    createTime datetime     null,
    updateTime datetime     null,
    url        varchar(200) null comment '文件上传路径',
    name       varchar(50)  null comment '文件名字',
    extra      varchar(50)  null comment '扩展名',
    userId     bigint       null comment '上传文件的用户id',
    path       varchar(200) null comment '实际存储路径',
    constraint id
        unique (id)
)
    comment '上传文件表';

create table notice
(
    id         bigint       not null
        primary key,
    userId     bigint       null,
    status     int          null,
    createTime datetime     null,
    objectId   bigint       null,
    fromUserId bigint       null comment '消息发出者的用户id',
    hasRead    int          null comment '是否已读(0未读1已读)',
    content    varchar(100) null comment '通知内容',
    type       int          null,
    updateTime datetime     null,
    constraint id
        unique (id)
)
    comment '通知表';

create index message_userId_createTime_index
    on notice (userId asc, id desc);

create table t1comment
(
    id         bigint   not null
        primary key,
    status     int      null,
    createTime datetime null,
    updateTime datetime null,
    thingId    bigint   null comment '所属主体id',
    userId     bigint   null comment '用户id',
    likeNum    bigint   null comment '被推荐数',
    replyNum   int      null comment '被回复数',
    content    text     null comment '评论内容',
    constraint id
        unique (id)
)
    comment '一级评论表';

create index t1comment_likeNum_id_index
    on t1comment (thingId asc, likeNum desc, replyNum desc, id desc);

create table t1comment_user_like
(
    t1CommentId bigint   null,
    userId      bigint   null,
    status      int      null,
    createTime  datetime null,
    constraint t1comment_user_like_userId_t1CommentId_uindex
        unique (userId, t1CommentId)
)
    comment '一级评论与用户点赞关系表';

create index t1comment_user_like_t1CommentId_userId_index
    on t1comment_user_like (t1CommentId, userId);

create index t1comment_user_like_userId_index
    on t1comment_user_like (userId);

create table t2comment
(
    id          bigint   not null
        primary key,
    status      int      null,
    createTime  datetime null,
    updateTime  datetime null,
    t1CommentId bigint   null comment '所属一级评论id',
    replyUserId bigint   null comment '所回复的评论的发布者id',
    userId      bigint   null comment '用户id',
    content     text     null comment '评论内容',
    thingId     bigint   null,
    replyType   int      null comment '区分回复的是一级评论还是二级评论（0为一级，1为二级）',
    constraint id
        unique (id),
    constraint t2comment_pk
        unique (t1CommentId, id)
)
    comment '二级评论表';

create table thing
(
    id          bigint       not null
        primary key,
    status      int          null,
    createTime  datetime     null,
    updateTime  datetime     null,
    topicId     bigint       null comment '所属话题id',
    name        varchar(200) null comment '主体名',
    score       bigint       null comment '总评分',
    customerNum bigint       null comment '参评人数',
    photoId     bigint       null comment '图片id',
    commentSum  int          null comment '评论总数',
    photoUrl    varchar(200) null comment '图片路径',
    userId      bigint       null,
    constraint id
        unique (id),
    constraint thing_pk
        unique (topicId, id)
)
    comment '主体';

create table thing_user_score
(
    thingId    bigint   null,
    status     int      null,
    userId     bigint   null,
    score      int      null comment '评分值',
    createTime datetime null,
    constraint thing_user_score_userId_thingId_uindex
        unique (userId, thingId)
)
    comment '用户主体评分关系';

create index score_relation_thing_id_user_id_index
    on thing_user_score (thingId, userId);

create table topic
(
    id          bigint       not null
        primary key,
    status      int          null,
    createTime  datetime     null,
    updateTime  datetime     null,
    name        varchar(200) null comment '名字',
    likeNum     bigint       null comment '推荐数',
    favoriteNum bigint       null comment '收藏数',
    content     text         null comment '简介',
    photoId     bigint       null comment '图片id',
    areaId      int          null comment '所属分区id',
    photoUrl    varchar(200) null comment '图片路径',
    userId      bigint       null,
    constraint id
        unique (id)
)
    comment '话题';

create index likednum__index
    on topic (likeNum desc, id desc);

create index topic_area_id_like_index
    on topic (areaId asc, likeNum desc, id desc);

create index topic_favoriteNum_id_index
    on topic (favoriteNum desc, id desc);

create table topic_user_favorite
(
    topicId    bigint   not null,
    userId     bigint   null,
    status     int      null,
    createTime datetime null,
    constraint topic_user_favorite_userId_topicId_uindex
        unique (userId, topicId)
)
    comment '用户收藏话题表';

create index topic_user_favorite_topicId_userId_index
    on topic_user_favorite (topicId, userId);

create index topic_user_favorite_userId_createTime_index
    on topic_user_favorite (userId asc, createTime desc);

create table topic_user_like
(
    topicId    bigint   not null comment '话题id',
    userId     bigint   null comment '用户id',
    status     int      null comment '是否点赞',
    createTime datetime null,
    constraint topic_user_like_userId_topicId_uindex
        unique (userId, topicId)
);

create index topic_user_like_topicId_userId_index
    on topic_user_like (topicId, userId);

create table user
(
    id            bigint       not null
        primary key,
    status        int          null,
    createTime    datetime     null,
    updateTime    datetime     null,
    name          varchar(50)  null comment '用户名',
    photoId       bigint       null comment '头像id',
    openId        varchar(100) null comment '微信用户唯一标识',
    unionId       varchar(100) null comment '微信开放平台统一id',
    lastLoginTime datetime     null comment '最后登录时间',
    phone         bigint       null comment '手机号',
    password      varchar(50)  null comment '密码',
    photoUrl      varchar(200) null comment '头像地址',
    userId        bigint       null,
    constraint id
        unique (id),
    constraint openId
        unique (openId),
    constraint phone
        unique (phone),
    constraint user_pk
        unique (unionId)
)
    comment '用户表';

create index user_phone_index
    on user (phone);

create table user_against
(
    userId     bigint   null,
    objectId   bigint   null comment '举报的对象id(话题、一级评论、二级评论)',
    type       int      null comment '举报对象的类型（话题、一级评论、二级评论）',
    status     int      null,
    createTime datetime null,
    constraint user_against_pk
        unique (userId, objectId)
)
    comment '举报记录表';

create index user_against_userId_objectId_index
    on user_against (userId, objectId);

create table user_own_topic
(
    userId     bigint   null,
    status     int      null,
    createTime datetime null,
    updateTime datetime null,
    topicId    bigint   null,
    constraint user_own_topic_pk
        unique (userId, topicId)
)
    comment '我的话题';

create index user_own_topic_user_id_topic_id_index
    on user_own_topic (userId asc, topicId desc);

create table user_topic_view_history
(
    userId     bigint   null,
    status     int      null,
    createTime datetime null,
    topicId    bigint   null,
    constraint user_topic_view_history_pk
        unique (userId, topicId)
)
    comment '话题浏览历史';

create index topic_view_history_user_id_topic_id_index
    on user_topic_view_history (userId asc, topicId desc);


