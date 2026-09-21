# ESP32 物联网天气站

软硬件结合的物联网天气站，手写传感器 I2C 协议读取，通过 MQTT 上报与远程控制。

## 技术栈

- C++ / Arduino + PlatformIO
- ESP32 开发板
- DHT20 温湿度传感器（I2C）
- SSD1306 OLED 显示屏（128×32）
- MQTT（EMQX 公共服务器）+ WiFi

## 功能特性

- 手动实现 DHT20 I2C 协议读取（发送 0xAC/0x33/0x00 触发测量，不依赖现成读库）
- OLED 实时显示温湿度，按钮切换温度 / 湿度、摄氏 / 华氏
- MQTT 发布温湿度到 `weather/station1/temperature`、`humidity` 主题，订阅 `control` 主题接收远程命令（含断线重连机制）

## 目录结构

```
├── src/main.cpp        # 主程序
├── platformio.ini      # PlatformIO 配置（依赖声明）
└── include/            # 头文件目录
```

## 构建

使用 PlatformIO 打开本项目，编辑 `src/main.cpp` 中的 WiFi 账号密码后编译烧录。

## 依赖（platformio.ini）

- Adafruit SSD1306
- DFRobot DHT20
- PubSubClient
