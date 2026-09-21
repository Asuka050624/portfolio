#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_SSD1306.h>
#include <PubSubClient.h>
#include <WiFi.h>

// ========== 引脚定义 ==========
#define BUTTON1_PIN 18    // 开关1
#define BUTTON2_PIN 19    // 开关2（只接了一个开关的话，这行可以保留但不会影响）

// ========== I2C 地址定义 ==========
#define DHT20_ADDR 0x38   // DHT20的I2C地址
#define OLED_ADDR 0x3C    // OLED的I2C地址（通常是0x3C）

// ========== OLED 显示屏设置 ==========
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 32
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

// ========== WiFi 设置（请修改为你的WiFi信息）==========
const char* ssid = "your-ssid";
const char* password = "your-password";

// ========== MQTT 设置 ==========
const char* mqtt_server = "broker.emqx.io";  // 公共MQTT服务器
const char* mqtt_topic_temp = "weather/station1/temperature";
const char* mqtt_topic_hum = "weather/station1/humidity";
const char* mqtt_topic_control = "weather/station1/control";

WiFiClient espClient;
PubSubClient client(espClient);

// ========== 全局变量 ==========
float temperature = 0.0;
float humidity = 0.0;
bool showTemperature = true;   // true显示温度，false显示湿度
bool useCelsius = true;         // true显示摄氏度，false显示华氏度
unsigned long lastReadTime = 0;
unsigned long lastMQTTPublishTime = 0;
bool lastButton1State = HIGH;
unsigned long button1PressTime = 0;

// ========== 函数声明 ==========
void readDHT20();
void updateOLED();
void connectWiFi();
void mqttCallback(char* topic, byte* payload, unsigned int length);
void reconnectMQTT();
void publishData();

// ========== 1. 读取 DHT20 传感器（不使用库，手动实现）==========
void readDHT20() {
    // 第一步：发送触发测量命令
    Wire.beginTransmission(DHT20_ADDR);
    Wire.write(0xAC);   // 触发测量命令
    Wire.write(0x33);   // 第一个参数
    Wire.write(0x00);   // 第二个参数
    byte error = Wire.endTransmission();
    
    if (error != 0) {
        Serial.println("DHT20 通信错误");
        return;
    }
    
    // 第二步：等待测量完成（至少80ms）
    delay(80);
    
    // 第三步：读取数据（6字节）
    Wire.requestFrom(DHT20_ADDR, 6);
    
    if (Wire.available() >= 6) {
        byte status = Wire.read();     // 状态字节
        byte hum_H = Wire.read();      // 湿度高位
        byte hum_M = Wire.read();      // 湿度中位
        byte hum_L = Wire.read();      // 湿度低位（高4位是湿度的低4位，低4位是温度的高4位）
        byte temp_H = Wire.read();     // 温度中位
        byte temp_M = Wire.read();     // 温度低位
        
        // 检查状态位 Bit[7] 是否为0（表示测量完成）
        if ((status & 0x80) == 0) {
            // 正确拼接20位湿度数据
            uint32_t humidity_raw = ((uint32_t)hum_H << 12) | ((uint32_t)hum_M << 4) | ((hum_L & 0xF0) >> 4);
            humidity = humidity_raw * 100.0 / (1 << 20);
            
            // 正确拼接20位温度数据
            uint32_t temp_raw = ((uint32_t)(hum_L & 0x0F) << 16) | ((uint32_t)temp_H << 8) | temp_M;
            temperature = temp_raw * 200.0 / (1 << 20) - 50;
            
            // 打印到串口
            Serial.print("温度: ");
            Serial.print(temperature);
            Serial.print(" °C, 湿度: ");
            Serial.print(humidity);
            Serial.println(" %");
        } else {
            Serial.println("DHT20 测量未完成");
        }
    } else {
        Serial.println("DHT20 数据读取失败");
    }
}

// ========== 2. 更新 OLED 显示 ==========
void updateOLED() {
    display.clearDisplay();
    display.setTextSize(2);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(0, 0);
    
    if (showTemperature) {
        float displayTemp = useCelsius ? temperature : (temperature * 1.8 + 32);
        display.print("Temp: ");
        display.print(displayTemp, 1);
        display.print(useCelsius ? "C" : "F");
    } else {
        display.print("Hum: ");
        display.print(humidity, 1);
        display.print("%");
    }
    
    display.display();
}

// ========== 3. 连接 WiFi ==========
void connectWiFi() {
    Serial.print("正在连接 WiFi");
    WiFi.begin(ssid, password);
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 40) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\nWiFi 已连接");
        Serial.print("IP 地址: ");
        Serial.println(WiFi.localIP());
    } else {
        Serial.println("\nWiFi 连接失败，将只使用本地功能");
    }
}

// ========== 4. MQTT 回调函数（接收远程控制命令）==========
void mqttCallback(char* topic, byte* payload, unsigned int length) {
    String message = "";
    for (unsigned int i = 0; i < length; i++) {
        message += (char)payload[i];
    }
    
    Serial.print("收到 MQTT 命令: ");
    Serial.println(message);
    
    if (message == "celsius") {
        useCelsius = true;
        updateOLED();
    } else if (message == "fahrenheit") {
        useCelsius = false;
        updateOLED();
    } else if (message == "show_temp") {
        showTemperature = true;
        updateOLED();
    } else if (message == "show_hum") {
        showTemperature = false;
        updateOLED();
    }
}

// ========== 5. 连接 MQTT 服务器 ==========
void reconnectMQTT() {
    // 如果已经连接，直接返回
    if (client.connected()) {
        return;
    }
    
    // 如果 WiFi 没连上，也不尝试连接 MQTT
    if (WiFi.status() != WL_CONNECTED) {
        return;
    }
    
    // 尝试连接（不重试，只尝试一次）
    Serial.print("正在连接 MQTT...");
    if (client.connect("ESP32_WeatherStation")) {
        Serial.println("已连接");
        client.subscribe(mqtt_topic_control);
    } else {
        Serial.print("失败，rc=");
        Serial.println(client.state());
        // 不 delay，直接返回，下次 loop 会再尝试
    }
}

// ========== 6. 发布传感器数据到 MQTT ==========
void publishData() {
    if (WiFi.status() != WL_CONNECTED || !client.connected()) {
        return;
    }
    
    char tempStr[10];
    char humStr[10];
    dtostrf(temperature, 4, 1, tempStr);
    dtostrf(humidity, 4, 1, humStr);
    
    client.publish(mqtt_topic_temp, tempStr);
    client.publish(mqtt_topic_hum, humStr);
    
    Serial.print("MQTT 已发布 - 温度: ");
    Serial.print(tempStr);
    Serial.print(" °C, 湿度: ");
    Serial.print(humStr);
    Serial.println(" %");
}

// ========== 7. 初始化设置 ==========
void setup() {
    Serial.begin(115200);
    delay(100);
    Serial.println("\n\n气象站启动...");
    
    // 初始化 I2C（SDA=GPIO21, SCL=GPIO22）
    Wire.begin(21, 22);
    Wire.setClock(100000);  // 100kHz I2C时钟
    
    // 初始化 OLED
    if (!display.begin(SSD1306_SWITCHCAPVCC, OLED_ADDR)) {
        Serial.println("OLED 初始化失败");
    } else {
        display.clearDisplay();
        display.setTextSize(1);
        display.setTextColor(SSD1306_WHITE);
        display.setCursor(0, 0);
        display.println("Weather Station");
        display.println("Starting...");
        display.display();
        delay(2000);
    }
    
    // 初始化按钮引脚（使用内部上拉）
    pinMode(BUTTON1_PIN, INPUT_PULLUP);
    pinMode(BUTTON2_PIN, INPUT_PULLUP);
    
    // 连接 WiFi
    connectWiFi();
    
    // 设置 MQTT
    client.setServer(mqtt_server, 1883);
    client.setCallback(mqttCallback);
    
    Serial.println("初始化完成");
}

// ========== 8. 主循环 ==========
void loop() {
    unsigned long currentMillis = millis();
    
    // 维护 MQTT 连接（非阻塞）
    if (WiFi.status() == WL_CONNECTED) {
        if (!client.connected()) {
            reconnectMQTT();
        }
        client.loop();
    }
    
    // 每隔2秒读取一次传感器
    if (currentMillis - lastReadTime >= 2000) {
        lastReadTime = currentMillis;
        readDHT20();
        updateOLED();
        
        // 每隔10秒发布一次 MQTT
        if (WiFi.status() == WL_CONNECTED && client.connected()) {
            if (currentMillis - lastMQTTPublishTime >= 10000) {
                lastMQTTPublishTime = currentMillis;
                publishData();
            }
        }
    }
    
    // 按钮检测（不会被 MQTT 阻塞）
    bool currentButton1State = digitalRead(BUTTON1_PIN);
    
    if (lastButton1State == HIGH && currentButton1State == LOW) {
        button1PressTime = millis();
    }
    else if (lastButton1State == LOW && currentButton1State == HIGH) {
        unsigned long pressDuration = millis() - button1PressTime;
        if (pressDuration < 1000) {
            showTemperature = !showTemperature;
            updateOLED();
            Serial.print("显示切换为: ");
            Serial.println(showTemperature ? "温度" : "湿度");
        } else {
            useCelsius = !useCelsius;
            updateOLED();
            Serial.print("单位切换为: ");
            Serial.println(useCelsius ? "摄氏度" : "华氏度");
        }
    }
    lastButton1State = currentButton1State;
    
    // 开关2（如果有）
    bool currentButton2State = digitalRead(BUTTON2_PIN);
    static bool lastButton2State = HIGH;
    if (lastButton2State == HIGH && currentButton2State == LOW) {
        Serial.println("开关2被按下，立即发布 MQTT 数据");
        if (WiFi.status() == WL_CONNECTED && client.connected()) {
            publishData();
        }
    }
    lastButton2State = currentButton2State;
    
    delay(50);
}