# 宠物健康社交 App

宠物健康系统的早期版本，覆盖桌面 GUI 与移动 Web 两端，是最终全栈版的前身。

## 目录结构

```
├── desktop/   # Java Swing 桌面端
└── mobile/    # 移动 Web 端 + Java 后端 + 自动化测试
```

## 桌面端（Java Swing）

- 5 大功能面板：宠物档案、健康提醒、社交、服务、推送提醒
- 宠物档案 CRUD（添加 / 编辑 / 删除，含确认弹窗 + 表单校验）
- 自定义绘制 UI：欢迎页、侧边导航、卡片式布局、渐变背景、按钮 hover/press 效果

## 移动 Web 端

- 原生 HTML/CSS/JS + jQuery
- 页面：登录 / 注册 / 预约 / 商城 / 资讯（5 篇详情页）/ 反馈 / 个人中心
- Java 后端：`UserAuthService` 用户认证
- 自动化测试：`AutomatedTestRunner.java` + `run_all_tests.html` 测试聚合页

## 运行

- **桌面端**：编译 `desktop/` 下的 Java 源码后运行（入口 `Main.java`）
- **移动端**：浏览器打开 `mobile/index.html`；后端需编译 `mobile/backend/` 下的 Java 源码
