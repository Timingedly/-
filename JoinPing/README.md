# JoinPing 后端项目

## 项目介绍

JoinPing 是一个仿虎扑评分的社区平台后端服务，基于 Spring Boot 3.4.7 构建，提供完整的社区互动、评分、评论等功能。项目采用微服务架构思想，集成了多种中间件技术栈。

### 核心特性

- **多级评论系统**：支持话题→主体→一级评论→二级评论的完整评论层级
- **实时搜索**：基于 Elasticsearch 的全文搜索功能
- **消息队列**：使用 RabbitMQ 处理异步任务
- **缓存优化**：Redis 缓存 + 布隆过滤器优化查询性能
- **WebSocket**：实时消息推送和在线状态管理
- **AI 集成**：集成 Ollama 本地大模型提供智能回复功能

## 技术栈

### 核心框架
- **Spring Boot 3.4.7** - 主框架
- **Java 17** - 开发语言
- **Sa-Token 1.44.0** - 权限认证框架

### 数据持久化
- **MySQL 8.0** - 主数据库
- **MyBatis-Plus 3.5.7** - ORM 框架
- **PageHelper 2.1.0** - 分页插件

### 中间件
- **Redis** - 缓存和会话管理
- **Elasticsearch 7.12.1** - 搜索引擎
- **RabbitMQ 3.8** - 消息队列
- **Redisson 3.16.0** - 分布式锁

### 工具库
- **Hutool 5.8.26** - 工具集
- **Lombok** - 代码简化

## 项目结构

```
com.example.joinping/
├── Initializer/              # 初始化组件
│   ├── BloomFilterInitializer.java      # 布隆过滤器初始化
│   └── ElasticsearchIndexInitializer.java # ES索引初始化
├── aop/                      # 切面编程
│   ├── anotation/            # 自定义注解
│   └── aspect/               # 切面实现
├── background/               # 后台任务
│   ├── GlobalExceptionHandler.java      # 全局异常处理
│   ├── RabbitMQListener.java            # MQ消息监听
│   └── schedule/             # 定时任务
├── config/                   # 配置类
│   ├── ElasticsearchConfig.java         # ES配置
│   ├── JacksonConfig.java               # JSON配置
│   ├── MybatisPlusConfig.java           # MyBatis配置
│   ├── RabbitMQConfig.java              # MQ配置
│   ├── RedisConfig.java                 # Redis配置
│   ├── SaTokenConfig.java               # 权限配置
│   ├── WebConfig.java                   # Web配置
│   └── WebSocketConfig.java             # WebSocket配置
├── constant/                 # 常量定义
├── controller/               # 控制器层
├── entity/                   # 实体类
│   ├── common/               # 通用实体
│   ├── dto/                  # 数据传输对象
│   ├── extra/                # 扩展实体
│   ├── pojo/                 # 持久化对象
│   ├── relaPojo/             # 关系对象
│   └── vo/                   # 视图对象
├── enums/                    # 枚举类
├── mapper/                   # 数据访问层
├── service/                  # 业务逻辑层
└── utils/                    # 工具类
```

## 核心业务模块

### 1. 用户模块 (User)
- 用户注册、登录、认证
- 用户信息管理
- 用户关系（关注、拉黑）

### 2. 话题模块 (Topic)
- 话题创建、编辑、删除
- 话题点赞、收藏
- 话题浏览历史

### 3. 主体模块 (Thing)
- 主体创建、评分
- 评分统计和排名
- 主体详情展示

### 4. 评论模块 (Comment)
- 一级评论 (T1Comment)
- 二级评论 (T2Comment)
- 评论点赞、回复

### 5. 搜索模块 (Search)
- 全文搜索 (Elasticsearch)
- 搜索结果排序
- 搜索建议

### 6. 通知模块 (Notice)
- 系统通知
- 消息推送 (WebSocket)
- 通知管理

### 7. 文件模块 (Document)
- 文件上传下载
- 图片处理
- 文件管理

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Elasticsearch 7.12.1
- RabbitMQ 3.8+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE joinping CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：`src/main/resources/mysqlDDL/script.sql`

### 中间件配置

#### Docker 快速部署

```bash
# 创建网络
docker network create jp-net

# 部署 RabbitMQ
docker run -e RABBITMQ_DEFAULT_USER=joinping -e RABBITMQ_DEFAULT_PASS=123456 \
  -v mq-plugins:/plugins --name mq --hostname mq \
  -p 15672:15672 -p 5672:5672 --network jp-net -d rabbitmq:3.8-management

# 部署 Elasticsearch
docker run -d --name es -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "discovery.type=single-node" -v es-data:/usr/share/elasticsearch/data \
  -v es-plugins:/usr/share/elasticsearch/plugins --privileged \
  --network jp-net -p 9200:9200 -p 9300:9300 elasticsearch:7.12.1

# 部署 Kibana（可选）
docker run -d --name kb -e ELASTICSEARCH_HOSTS=http://es:9200 \
  --network=jp-net -p 5601:5601 kibana:7.12.1

# 部署 Redis
docker run -d --name redis -p 6379:6379 redis:6.0 --requirepass 123456
```

### 配置修改

修改 `src/main/resources/application-test.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/joinping?serverTimezone=GMT%2B8
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      password: 123456
  rabbitmq:
    host: localhost
    username: joinping
    password: 123456
  elasticsearch:
    uris: http://localhost:9200
```

### 启动项目

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

项目启动后访问：http://localhost:8080

## API 接口规范

### 统一响应格式

**成功响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "hasSuccessed": true
}
```

**失败响应：**
```json
{
  "code": 500,
  "message": "error",
  "data": {},
  "hasSuccessed": false
}
```

**分页响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 0,
    "current": 1,
    "size": 10,
    "pages": 0,
    "hasPrevious": false,
    "hasNext": false
  },
  "hasSuccessed": true
}
```

### 主要 API 接口

#### 用户相关
- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/update` - 更新用户信息

#### 话题相关
- `GET /api/topic/list` - 获取话题列表
- `POST /api/topic/create` - 创建话题
- `GET /api/topic/{id}` - 获取话题详情
- `PUT /api/topic/update` - 更新话题
- `DELETE /api/topic/{id}` - 删除话题

#### 主体相关
- `GET /api/thing/list` - 获取主体列表
- `POST /api/thing/create` - 创建主体
- `POST /api/thing/score` - 为主体评分
- `GET /api/thing/{id}` - 获取主体详情

#### 评论相关
- `GET /api/t1comment/list` - 获取一级评论列表
- `POST /api/t1comment/create` - 创建一级评论
- `GET /api/t2comment/list` - 获取二级评论列表
- `POST /api/t2comment/create` - 创建二级评论

#### 搜索相关
- `GET /api/search` - 全文搜索
- `GET /api/search/suggest` - 搜索建议

## 核心功能实现

### 1. 分布式锁机制
使用 Redisson 实现分布式锁，防止并发操作导致的数据不一致问题。

### 2. 布隆过滤器
用于用户 ID 等数据的快速查询，减少数据库压力。

### 3. 消息队列
- 异步处理评论创建
- 延迟消息处理
- 失败重试机制

### 4. 搜索引擎
- Elasticsearch 索引自动创建
- 近实时搜索
- 搜索结果高亮

### 5. WebSocket 实时通信
- 在线状态管理
- 实时消息推送
- 连接状态监控

## 性能优化

### 缓存策略
- Redis 缓存热点数据
- 多级缓存设计
- 缓存穿透防护

### 数据库优化
- 索引优化
- 分页查询
- 读写分离（预留）

### 搜索优化
- 分词器优化
- 搜索建议缓存
- 搜索结果排序

## 部署说明

### 生产环境配置

创建 `application-prod.yml`：

```yaml
spring:
  profiles: prod
  datasource:
    url: jdbc:mysql://生产数据库地址:3306/joinping
    username: 生产用户名
    password: 生产密码
  # 其他生产环境配置...
```

### Docker 部署

```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/JoinPing-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 测试账号

- **手机号**: 12312312312
- **密码**: 123123

## 开发规范

### 代码规范
- 使用 Lombok 简化代码
- 统一异常处理
- 详细的日志记录

### API 规范
- 统一的响应格式
- 合理的错误码设计
- 完整的接口文档

### 安全规范
- SQL 注入防护
- XSS 攻击防护
- 数据脱敏处理

## 常见问题

### Q: 项目启动失败
A: 检查中间件连接配置，确保 Redis、MySQL、Elasticsearch、RabbitMQ 服务正常运行。

### Q: 搜索功能不可用
A: 检查 Elasticsearch 服务状态，确认索引是否正常创建。

### Q: 消息队列不工作
A: 检查 RabbitMQ 连接配置，确认队列和交换机是否正确创建。

## 贡献指南

1. Fork 本仓库
2. 新建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题请联系项目维护团队。

---

**注意**: 本项目为后端服务，前端项目请参考 [JoinPingWeb](../JoinPingWeb) 仓库。