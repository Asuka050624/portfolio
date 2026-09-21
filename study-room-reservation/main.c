/********************************************************************
 * 文件名称：main.c
 * 文件用途：项目的入口文件，实现HTTP服务端，处理前端的登录/注册/预约请求
 *          是前后端交互的核心枢纽，调用auth/预约/数据库工具函数
 * 依赖文件：
 *   - auth/user_auth.h：用户认证函数（登录/注册）
 *   - reserve/reserve.h：自习室预约函数（预约/取消/查询）
 *   - db/db_helper.h：数据库连接工具（初始化/关闭连接）
 *   - winsock2.h：Windows网络编程（替换Linux的sys/socket.h）
 *   - string.h：字符串处理（解析参数）
 *   - stdlib.h：内存分配/程序退出
 * 使用场景：直接运行此文件启动后端，前端访问http://localhost:8080
 * 注意事项：运行前确保MySQL服务启动，OpenSSL/MySQL依赖配置正确
 ********************************************************************/

/************************** 引入依赖头文件 **************************/
// 数据库连接工具
#include "db/db_helper.h"
// 用户认证函数声明
#include "auth/user_auth.h"
// 自习室预约函数声明
#include "reserve/reserve.h"
// Windows网络编程头文件（替换Linux的socket头文件）
#include <winsock2.h>
#include <ws2tcpip.h>
// 标准输入输出
#include <stdio.h>
// 字符串处理
#include <string.h>
// 标准库
#include <stdlib.h>

/************************** 定义常量宏 **************************/
#define PORT 8080                // HTTP服务监听端口
#define BUFFER_SIZE 1024         // 接收前端数据的缓冲区大小
#define NAME_BUF_SIZE 50         // 用户名/姓名存储大小
#define JSON_BUF_SIZE 2048       // JSON响应缓冲区大小

/********************************************************************
 * 函数名称：get_user_id
 * 函数功能：根据用户账号（手机号）查询数据库中的用户ID
 * 实现逻辑：
 * 1. 拼接SQL查询t_user表的id字段
 * 2. 执行查询并返回用户ID（无数据返回-1）
 * 参数说明：
 *   - conn：MYSQL*，已建立的数据库连接句柄
 *   - account：const char*，用户账号（手机号）
 * 返回值：int，成功返回用户ID，失败返回-1
 * 调用场景：预约/取消预约时，需要将账号转为用户ID
 ********************************************************************/
int get_user_id(MYSQL* conn, const char* account) {
    if (conn == NULL || account == NULL) {
        return -1;
    }

    char sql[256] = {0};
    sprintf(sql, "SELECT id FROM t_user WHERE account = '%s' AND status = 0", account);

    // 执行查询
    if (mysql_query(conn, sql) != 0) {
        printf("【查询用户ID错误】%s\n", mysql_error(conn));
        return -1;
    }

    // 获取查询结果
    MYSQL_RES* res = mysql_store_result(conn);
    if (res == NULL) {
        return -1;
    }

    MYSQL_ROW row = mysql_fetch_row(res);
    int user_id = (row != NULL) ? atoi(row[0]) : -1;

    // 释放结果集
    mysql_free_result(res);
    return user_id;
}

/********************************************************************
 * 函数名称：parse_params
 * 函数功能：解析前端URL参数（account/pwd/name/room_id/date/time/reserve_id/student_id等）
 * 实现逻辑：
 * 1. 按&分割参数，按=分割参数名和值
 * 2. 将参数值存入对应数组
 * 参数说明：
 *   - query：const char*，前端传递的URL参数（如?account=13800138000&student_id=20250001）
 *   - account：char*，输出账号
 *   - pwd：char*，输出密码
 *   - name：char*，输出姓名
 *   - room_id：int*，输出自习室ID
 *   - date：char*，输出预约日期
 *   - time：char*，输出预约时间
 *   - reserve_id：int*，输出预约ID
 *   - student_id：char*，输出学号（新增）
 * 返回值：无
 * 调用场景：所有接口解析参数时调用
 ********************************************************************/
void parse_params(const char* query, char* account, char* pwd, char* name,
                  int* room_id, char* date, char* time, int* reserve_id, char* student_id) {
    if (query == NULL) {
        return;
    }

    // 复制到临时数组（避免修改原字符串）
    char temp[BUFFER_SIZE] = {0};
    strncpy(temp, query, BUFFER_SIZE - 1);

    // 按&分割参数
    char* param = strtok(temp, "&");
    while (param != NULL) {
        char* key = strtok(param, "=");
        char* value = strtok(NULL, "=");

        if (key == NULL || value == NULL) {
            param = strtok(NULL, "&");
            continue;
        }

        // 匹配参数名并赋值
        if (strcmp(key, "account") == 0) {
            strncpy(account, value, NAME_BUF_SIZE - 1);
        } else if (strcmp(key, "pwd") == 0) {
            strncpy(pwd, value, NAME_BUF_SIZE - 1);
        } else if (strcmp(key, "name") == 0) {
            strncpy(name, value, NAME_BUF_SIZE - 1);
        } else if (strcmp(key, "room_id") == 0) {
            *room_id = atoi(value);
        } else if (strcmp(key, "date") == 0) {
            strncpy(date, value, 20 - 1);
        } else if (strcmp(key, "time") == 0) {
            strncpy(time, value, 20 - 1);
        } else if (strcmp(key, "reserve_id") == 0) {
            *reserve_id = atoi(value);
        } else if (strcmp(key, "student_id") == 0) { // 新增：解析学号参数
            strncpy(student_id, value, 20 - 1);
        }

        param = strtok(NULL, "&");
    }
}

/********************************************************************
 * 函数名称：send_json_response
 * 函数功能：向前端返回JSON格式的响应
 * 实现逻辑：
 * 1. 拼接HTTP响应头（指定JSON格式+编码）
 * 2. 拼接JSON数据体并发送
 * 参数说明：
 *   - client_fd：SOCKET，客户端连接描述符（Windows专用类型）
 *   - code：int，状态码（0=成功，1=失败）
 *   - msg：const char*，提示信息
 *   - data：const char*，附加数据（JSON字符串，可选）
 * 返回值：无
 * 调用场景：所有接口响应前端时调用
 ********************************************************************/
void send_json_response(SOCKET client_fd, int code, const char* msg, const char* data) {
    char response[BUFFER_SIZE + JSON_BUF_SIZE] = {0};
    char json[JSON_BUF_SIZE] = {0};

    // 拼接JSON数据体
    if (data != NULL && strlen(data) > 0) {
        snprintf(json, JSON_BUF_SIZE - 1,
                 "{\"code\":%d,\"msg\":\"%s\",\"data\":%s}",
                 code, msg, data);
    } else {
        snprintf(json, JSON_BUF_SIZE - 1,
                 "{\"code\":%d,\"msg\":\"%s\"}",
                 code, msg);
    }

    // 拼接HTTP响应头
    snprintf(response, sizeof(response) - 1,
             "HTTP/1.1 200 OK\r\n"
             "Content-Type: application/json; charset=utf-8\r\n"
             "Access-Control-Allow-Origin: *\r\n"  // 解决跨域问题
             "Content-Length: %ld\r\n"
             "\r\n%s",
             strlen(json), json);

    // 发送响应
    send(client_fd, response, strlen(response), 0);
}

/********************************************************************
 * 函数名称：build_rooms_json
 * 函数功能：将自习室列表拼接为JSON格式字符串
 * 实现逻辑：
 * 1. 遍历自习室查询结果，拼接JSON数组
 * 参数说明：
 *   - conn：MYSQL*，数据库连接句柄
 *   - json_out：char*，输出JSON字符串（需提前分配空间）
 * 返回值：int，成功返回自习室数量，失败返回-1
 * 调用场景：/api/rooms接口返回自习室列表
 ********************************************************************/
int build_rooms_json(MYSQL* conn, char* json_out) {
    if (conn == NULL || json_out == NULL) {
        return -1;
    }

    char sql[256] = "SELECT id, room_name, capacity FROM study_room WHERE status = 1";
    if (mysql_query(conn, sql) != 0) {
        printf("【查询自习室错误】%s\n", mysql_error(conn));
        return -1;
    }

    MYSQL_RES* res = mysql_store_result(conn);
    if (res == NULL) {
        return -1;
    }

    int num_rows = mysql_num_rows(res);
    if (num_rows == 0) {
        strcpy(json_out, "[]");
        mysql_free_result(res);
        return 0;
    }

    // 拼接JSON数组
    strcpy(json_out, "[");
    MYSQL_ROW row;
    while ((row = mysql_fetch_row(res)) != NULL) {
        char room_json[256] = {0};
        sprintf(room_json, "{\"id\":%s,\"name\":\"%s\",\"capacity\":%s},",
                row[0], row[1], row[2]);
        strcat(json_out, room_json);
    }

    // 去掉最后一个逗号，添加结束符
    json_out[strlen(json_out) - 1] = ']';
    mysql_free_result(res);

    return num_rows;
}

/********************************************************************
 * 函数名称：build_reserves_json
 * 函数功能：将用户预约记录拼接为JSON格式字符串
 * 实现逻辑：
 * 1. 查询用户预约记录并遍历
 * 2. 拼接包含预约ID/自习室名称/日期/时间/状态的JSON数组
 * 参数说明：
 *   - conn：MYSQL*，数据库连接句柄
 *   - user_id：int，用户ID
 *   - json_out：char*，输出JSON字符串
 * 返回值：int，成功返回预约数量，失败返回-1
 * 调用场景：/api/my-reserves接口返回用户预约列表
 ********************************************************************/
int build_reserves_json(MYSQL* conn, int user_id, char* json_out) {
    if (conn == NULL || user_id <= 0 || json_out == NULL) {
        return -1;
    }

    char sql[256] = {0};
    sprintf(sql, "SELECT r.id, s.room_name, r.reserve_date, r.reserve_time, r.status "
                 "FROM reservation r LEFT JOIN study_room s ON r.room_id = s.id "
                 "WHERE r.user_id = %d", user_id);

    if (mysql_query(conn, sql) != 0) {
        printf("【查询预约记录错误】%s\n", mysql_error(conn));
        return -1;
    }

    MYSQL_RES* res = mysql_store_result(conn);
    if (res == NULL) {
        return -1;
    }

    int num_rows = mysql_num_rows(res);
    if (num_rows == 0) {
        strcpy(json_out, "[]");
        mysql_free_result(res);
        return 0;
    }

    // 拼接JSON数组
    strcpy(json_out, "[");
    MYSQL_ROW row;
    while ((row = mysql_fetch_row(res)) != NULL) {
        char reserve_json[256] = {0};
        sprintf(reserve_json, "{\"id\":%s,\"room_name\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"status\":%s},",
                row[0], row[1], row[2], row[3], row[4]);
        strcat(json_out, reserve_json);
    }

    // 去掉最后一个逗号，添加结束符
    json_out[strlen(json_out) - 1] = ']';
    mysql_free_result(res);

    return num_rows;
}

/********************************************************************
 * 函数名称：main
 * 函数功能：项目入口，启动HTTP服务端，处理所有前端请求
 * 实现逻辑：
 * 1. 初始化Windows Socket并绑定端口8080
 * 2. 监听端口，循环接收客户端连接
 * 3. 解析请求类型，调用对应业务函数（新增学号绑定/学号登录）
 * 4. 返回JSON响应给前端
 * 返回值：int，0表示正常退出，-1表示异常
 ********************************************************************/
int main() {
    // 新增：Windows Socket初始化（必须）
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        printf("【服务错误】WSA初始化失败！\n");
        return -1;
    }

    SOCKET server_fd, client_fd; // 修改：Windows使用SOCKET类型，而非int
    struct sockaddr_in server_addr, client_addr;
    int client_addr_len = sizeof(client_addr); // 修改：Windows使用int，而非socklen_t
    char buffer[BUFFER_SIZE] = {0};

    /************************** 1. 创建Socket **************************/
    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd == INVALID_SOCKET) { // 修改：Windows用INVALID_SOCKET判断失败
        printf("【服务错误】创建Socket失败！\n");
        WSACleanup();
        return -1;
    }
    printf("【服务提示】Socket创建成功\n");

    /************************** 2. 设置Socket选项（避免端口占用） **************************/
    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, (const char*)&opt, sizeof(opt));

    /************************** 3. 绑定Socket到端口8080 **************************/
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(PORT);

    if (bind(server_fd, (struct sockaddr*)&server_addr, sizeof(server_addr)) == SOCKET_ERROR) {
        printf("【服务错误】绑定端口失败！\n");
        closesocket(server_fd); // 修改：Windows用closesocket关闭Socket
        WSACleanup();
        return -1;
    }
    printf("【服务提示】端口%d绑定成功\n", PORT);

    /************************** 4. 监听端口 **************************/
    if (listen(server_fd, 3) == SOCKET_ERROR) {
        printf("【服务错误】监听端口失败！\n");
        closesocket(server_fd);
        WSACleanup();
        return -1;
    }
    printf("【服务提示】后端服务启动成功，监听http://localhost:%d\n", PORT);

    /************************** 5. 循环处理客户端请求 **************************/
    while (1) {
        // 接收客户端连接
        client_fd = accept(server_fd, (struct sockaddr*)&client_addr, &client_addr_len);
        if (client_fd == INVALID_SOCKET) {
            printf("【服务错误】接收客户端连接失败！\n");
            continue;
        }
        printf("【服务提示】客户端已连接：%s:%d\n",
               inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));

        // 读取客户端请求数据（修改：Windows用recv，而非read）
        memset(buffer, 0, BUFFER_SIZE);
        ssize_t read_len = recv(client_fd, buffer, BUFFER_SIZE - 1, 0);
        if (read_len <= 0) {
            printf("【服务错误】读取客户端数据失败！\n");
            closesocket(client_fd);
            continue;
        }

        // 初始化参数变量（新增：学号相关变量）
        char account[NAME_BUF_SIZE] = {0};
        char pwd[NAME_BUF_SIZE] = {0};
        char name[NAME_BUF_SIZE] = {0};
        int room_id = -1;
        char date[20] = {0};
        char time[20] = {0};
        int reserve_id = -1;
        char student_id[20] = {0}; // 新增：存储前端传递的学号
        char out_student_id[20] = {0}; // 新增：存储登录返回的学号
        char out_name[NAME_BUF_SIZE] = {0};
        int out_role = 0;
        int code = 1;
        char msg[100] = "未知请求";
        char json_data[JSON_BUF_SIZE] = {0};

        // 初始化数据库连接
        MYSQL* conn = init_mysql_conn();
        if (conn == NULL) {
            strcpy(msg, "数据库连接失败");
            send_json_response(client_fd, code, msg, NULL);
            closesocket(client_fd);
            continue;
        }

        /************************** 解析请求类型并处理 **************************/
        // 1. 注册接口：/api/register
        if (strstr(buffer, "GET /api/register") != NULL) {
            // 提取URL参数（跳过?）
            char* query = strstr(buffer, "?");
            if (query != NULL) {
                parse_params(query + 1, account, pwd, name, &room_id, date, time, &reserve_id, student_id);
            }

            // 调用注册函数
            int ret = user_register(conn, account, pwd, name);
            if (ret == 0) {
                code = 0;
                strcpy(msg, "注册成功，请登录");
            } else {
                strcpy(msg, "注册失败（账号已存在/参数错误）");
            }

            // 返回响应
            send_json_response(client_fd, code, msg, NULL);
        }

        // 2. 登录接口：/api/login（修改：支持学号登录，返回学号）
        else if (strstr(buffer, "GET /api/login") != NULL) {
            // 提取URL参数
            char* query = strstr(buffer, "?");
            if (query != NULL) {
                parse_params(query + 1, account, pwd, name, &room_id, date, time, &reserve_id, student_id);
            }

            // 调用登录函数（新增：传递out_student_id参数）
            int ret = user_login(conn, account, pwd, out_name, &out_role, out_student_id);
            if (ret == 0) {
                code = 0;
                strcpy(msg, "登录成功");
                // 拼接用户信息JSON（新增：返回学号）
                snprintf(json_data, JSON_BUF_SIZE - 1,
                         "{\"name\":\"%s\",\"role\":%d,\"student_id\":\"%s\"}",
                         out_name, out_role, out_student_id);
            } else {
                strcpy(msg, "登录失败（账号/学号不存在/密码错误）");
            }

            // 返回响应
            send_json_response(client_fd, code, msg, json_data);
        }

        // 3. 新增：绑定学号接口 /api/bind-student-id
        else if (strstr(buffer, "GET /api/bind-student-id") != NULL) {
            // 提取URL参数
            char* query = strstr(buffer, "?");
            if (query != NULL) {
                parse_params(query + 1, account, pwd, name, &room_id, date, time, &reserve_id, student_id);
            }

            // 参数校验
            if (strlen(account) == 0 || strlen(student_id) == 0) {
                strcpy(msg, "参数错误：账号/学号不能为空");
            } else {
                // 更新t_user表的student_id字段
                char sql[256] = {0};
                sprintf(sql, "UPDATE t_user SET student_id = '%s' WHERE account = '%s' AND status = 0",
                        student_id, account);
                if (mysql_query(conn, sql) != 0) {
                    printf("【绑定学号错误】%s\n", mysql_error(conn));
                    strcpy(msg, "绑定学号失败");
                } else {
                    code = 0;
                    strcpy(msg, "学号绑定成功");
                }
            }

            // 返回响应
            send_json_response(client_fd, code, msg, NULL);
        }

        // 4. 查询自习室列表接口：/api/rooms（不变）
        else if (strstr(buffer, "GET /api/rooms") != NULL) {
            // 构建自习室列表JSON
            int num = build_rooms_json(conn, json_data);
            if (num >= 0) {
                code = 0;
                strcpy(msg, "查询自习室成功");
            } else {
                strcpy(msg, "查询自习室失败");
                strcpy(json_data, "[]");
            }

            // 返回响应
            send_json_response(client_fd, code, msg, json_data);
        }

        // 5. 预约自习室接口：/api/reserve（不变）
        else if (strstr(buffer, "GET /api/reserve") != NULL) {
            // 提取URL参数
            char* query = strstr(buffer, "?");
            if (query != NULL) {
                parse_params(query + 1, account, pwd, name, &room_id, date, time, &reserve_id, student_id);
            }

            // 校验参数
            if (strlen(account) == 0 || room_id <= 0 || strlen(date) == 0 || strlen(time) == 0) {
                strcpy(msg, "参数错误（账号/自习室ID/日期/时间不能为空）");
                send_json_response(client_fd, code, msg, NULL);
                close_mysql_conn(conn);
                closesocket(client_fd);
                continue;
            }

            // 查询用户ID
            int user_id = get_user_id(conn, account);
            if (user_id == -1) {
                strcpy(msg, "用户不存在");
                send_json_response(client_fd, code, msg, NULL);
                close_mysql_conn(conn);
                closesocket(client_fd);
                continue;
            }

            // 调用预约函数
            int ret = reserve_room(conn, user_id, room_id, date, time);
            if (ret == 0) {
                code = 0;
                strcpy(msg, "预约成功");
            } else {
                strcpy(msg, "预约失败（自习室不可用/重复预约）");
            }

            // 返回响应
            send_json_response(client_fd, code, msg, NULL);
        }

        // 6. 查询我的预约接口：/api/my-reserves（不变）
        else if (strstr(buffer, "GET /api/my-reserves") != NULL) {
            // 提取URL参数
            char* query = strstr(buffer, "?");
            if (query != NULL) {
                parse_params(query + 1, account, pwd, name, &room_id, date, time, &reserve_id, student_id);
            }

            // 校验参数
            if (strlen(account) == 0) {
                strcpy(msg, "账号不能为空");
                send_json_response(client_fd, code, msg, NULL);
                close_mysql_conn(conn);
                closesocket(client_fd);
                continue;
            }

            // 查询用户ID
            int user_id = get_user_id(conn, account);
            if (user_id == -1) {
                strcpy(msg, "用户不存在");
                send_json_response(client_fd, code, msg, NULL);
                close_mysql_conn(conn);
                closesocket(client_fd);
                continue;
            }

            // 构建预约列表JSON
            int num = build_reserves_json(conn, user_id, json_data);
            if (num >= 0) {
                code = 0;
                strcpy(msg, "查询预约记录成功");
            } else {
                strcpy(msg, "查询预约记录失败");
                strcpy(json_data, "[]");
            }

            // 返回响应
            send_json_response(client_fd, code, msg, json_data);
        }

        // 7. 取消预约接口：/api/cancel-reserve（不变）
        else if (strstr(buffer, "GET /api/cancel-reserve") != NULL) {
            // 提取URL参数
            char* query = strstr(buffer, "?");
            if (query != NULL) {
                parse_params(query + 1, account, pwd, name, &room_id, date, time, &reserve_id, student_id);
            }

            // 校验参数
            if (strlen(account) == 0 || reserve_id <= 0) {
                strcpy(msg, "参数错误（账号/预约ID不能为空）");
                send_json_response(client_fd, code, msg, NULL);
                close_mysql_conn(conn);
                closesocket(client_fd);
                continue;
            }

            // 查询用户ID
            int user_id = get_user_id(conn, account);
            if (user_id == -1) {
                strcpy(msg, "用户不存在");
                send_json_response(client_fd, code, msg, NULL);
                close_mysql_conn(conn);
                closesocket(client_fd);
                continue;
            }

            // 调用取消预约函数
            int ret = cancel_reserve(conn, reserve_id, user_id);
            if (ret == 0) {
                code = 0;
                strcpy(msg, "取消预约成功");
            } else {
                strcpy(msg, "取消预约失败（无此预约/无权限）");
            }

            // 返回响应
            send_json_response(client_fd, code, msg, NULL);
        }

        // 未知请求
        else {
            send_json_response(client_fd, code, msg, NULL);
        }

        /************************** 清理资源 **************************/
        close_mysql_conn(conn);  // 关闭数据库连接
        closesocket(client_fd);  // 修改：Windows用closesocket关闭客户端连接
        printf("【服务提示】客户端连接已关闭\n");
    }

    // 关闭服务端Socket（循环不会执行到这里，仅作兜底）
    closesocket(server_fd);
    WSACleanup(); // 新增：释放Windows Socket资源
    return 0;
}