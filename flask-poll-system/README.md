# Flask 投票系统

RESTful 投票系统，Redis 存储 + Nginx 负载均衡，接口经历从 XML 到 RESTful 再到 Redis 的五次版本演进。

## 技术栈

- Flask + Flask-RESTful
- Redis
- Docker Compose + Nginx（3 个服务实例负载均衡）

## 功能特性

- REST 资源设计：Poll / PollOption / Vote（完整 CRUD）
- 统一异常处理 + 投票结果统计接口
- 前端投票页 `index.html` + 内置 API 测试工具（`/api-test`）

## 接口版本演进

| 版本 | 协议 / 架构 | 存储 |
|------|-------------|------|
| 1.0.0 | HTTP GET + XML 响应 | 进程内存 |
| 2.0.0 | XML-RPC | 进程内存 |
| 3.0.0 | RESTful API | 进程内存 |
| 3.1.0 | RESTful + flask_restful | 进程内存 |
| 3.2.0 | RESTful + Redis | Redis（无状态） |

## 目录结构

```
├── app.py              # 主程序（Flask + Redis）
├── requirements.txt    # Python 依赖
├── docker-compose.yml  # Docker Compose 编排（Redis + 3×poll + Nginx）
├── nginx.conf          # Nginx 负载均衡配置
├── containers/
│   └── Containerfile   # 镜像构建文件
└── templates/
    ├── index.html      # 投票主界面
    └── api_test.html   # API 测试工具
```

## 运行

```bash
docker build -t poll-service -f containers/Containerfile .
docker-compose up -d
# 访问 http://localhost/
```
