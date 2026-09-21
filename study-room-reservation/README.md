# 自习室预约系统

用 C 语言从零实现 HTTP 服务端 + MySQL 的预约系统，不依赖任何 Web 框架。

## 技术栈

- C + Winsock2（Windows 网络编程）
- MySQL（C API）
- OpenSSL（MD5 密码加密）
- 原生 HTML/CSS/JS 前端
- CMake / CLion

## 功能特性

- 手写 HTTP 服务器（监听 8080 端口），手动解析 HTTP 请求、拼接 JSON 响应
- 用户注册 / 登录（OpenSSL MD5 加密存储密码，避免明文）
- 自习室预约 / 取消 / 查询，含房间可用性检查 + 防重复预约校验
- 前端 7 个页面：首页 / 登录 / 注册 / 预约 / 预约页 / 我的预约 / 个人中心

## 目录结构

```
├── main.c      # HTTP 服务入口（核心枢纽）
├── auth/       # 用户认证（注册 / 登录）
├── reserve/    # 自习室预约核心逻辑
├── db/         # MySQL 连接工具
├── web/        # 前端页面
└── static/     # 静态资源（CSS / JS / 图片）
```

## 运行

1. 启动 MySQL，在 `db/db_helper.h` 中配置连接参数
2. 编译运行 `main.c`（CMake 项目，可用 CLion 打开）
3. 浏览器访问 `http://localhost:8080`
