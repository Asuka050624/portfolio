# 宠物健康管理系统

> 全栈宠物健康管理平台（旗舰项目）｜代码仓库：[Asuka050624/pet-health-management](https://github.com/Asuka050624/pet-health-management)

本项目代码托管在独立仓库，本目录仅作展示与导航。

## 项目简介

宠物健康管理全栈平台，包含移动用户端与管理员后台，前后端分离，Docker 部署，接入大模型 AI 健康分析。

## 技术栈

- **后端**：Python 3 + Flask + SQLAlchemy + Flask-JWT-Extended + Flask-Migrate
- **前端**：React 18 + Vite + React Router 6 + Axios
- **数据库**：SQLite（开发环境）/ MySQL 8.0（生产环境）
- **部署与测试**：Docker Compose + Nginx、GitHub Actions CI、pytest

## 核心功能

- 用户认证（JWT + 三级路由守卫）、宠物档案管理
- 电商闭环（商品 / 购物车 / 订单 / 扣库存）
- 挂号预约、资讯与评论、用户反馈、系统消息
- 管理后台（仪表盘数据统计 + 全量 CRUD）
- AI 健康分析（DeepSeek 大模型 + 规则引擎降级熔断）

## 技术亮点

- 策略模式集成 AI：大模型 + 规则引擎双引擎，带降级熔断
- 前端工程化：Axios 拦截器自动刷新 token、Context 状态管理、路由守卫
- 分层架构：models / api / services / middleware 清晰分离

## 访问代码

👉 [https://github.com/Asuka050624/pet-health-management](https://github.com/Asuka050624/pet-health-management)
