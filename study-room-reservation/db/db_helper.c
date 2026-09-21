/********************************************************************
 * 文件名称：db_helper.c
 * 文件用途：实现db_helper.h头文件中声明的MySQL数据库操作函数
 *          是项目与MySQL数据库交互的核心工具文件，所有数据库操作（登录、注册）都会间接调用此文件的函数
 * 依赖文件：db_helper.h（必须先包含，才能使用宏定义和函数声明）
 * 使用场景：配合db_helper.h使用，在main.c、user_auth.c中被调用
 ********************************************************************/
// 包含自定义的数据库工具头文件，获取宏定义和函数声明
#include "db_helper.h"

/********************************************************************
 * 函数名称：init_mysql_conn
 * 函数功能：初始化MySQL句柄并建立数据库连接
 * 实现逻辑：
 * 1. 调用mysql_init()初始化MySQL句柄，是连接数据库的前置操作
 * 2. 调用mysql_real_connect()传入配置参数，建立实际连接
 * 3. 设置字符集为utf8mb4，解决中文插入/查询乱码问题
 * 4. 失败时打印错误信息并释放资源，成功时返回连接句柄
 * 参数说明：无
 * 返回值：MYSQL* - 数据库连接句柄（成功）/ NULL（失败）
 * 调用场景：
 *   - 登录功能：登录前必须先调用此函数建立连接
 *   - 注册功能：注册用户前必须先调用此函数建立连接
 * 调用示例：MYSQL* conn = init_mysql_conn();
 ********************************************************************/
MYSQL* init_mysql_conn() {
    // 1. 初始化MySQL句柄：如果返回NULL，说明初始化失败
    MYSQL* conn = mysql_init(NULL);
    if (conn == NULL) {
        printf("【数据库错误】MySQL句柄初始化失败！无法继续连接数据库\n");
        return NULL;
    }

    // 2. 调用MySQL官方API建立数据库连接，参数来自db_helper.h的宏定义
    if (mysql_real_connect(
        conn,          // 初始化后的MySQL句柄
        DB_HOST,       // 数据库主机地址（本地为localhost）
        DB_USER,       // MySQL登录用户名（默认root）
        DB_PWD,        // MySQL登录密码（已在db_helper.h中配置）
        DB_NAME,       // 要连接的数据库名（reserveStudyRoom）
        DB_PORT,       // MySQL端口号（默认3306）
        NULL,          // 套接字文件（本地连接填NULL）
        0              // 连接标志（默认0，无特殊配置）
    ) == NULL) {
        // 连接失败：打印具体错误信息（mysql_error()返回错误描述），并关闭句柄释放资源
        printf("【数据库错误】连接失败！错误详情：%s\n", mysql_error(conn));
        mysql_close(conn); // 释放初始化的句柄，避免内存泄漏
        return NULL;
    }

    // 3. 设置数据库连接的字符集为utf8mb4，必须执行！否则插入/查询中文会乱码
    if (mysql_set_character_set(conn, "utf8mb4") != 0) {
        printf("【数据库错误】字符集设置失败！错误详情：%s\n", mysql_error(conn));
        mysql_close(conn);
        return NULL;
    }

    // 4. 连接成功：打印提示信息，返回连接句柄供后续操作使用
    printf("【数据库提示】MySQL数据库连接成功！已连接到reserveStudyRoom库\n");
    return conn;
}

/********************************************************************
 * 函数名称：close_mysql_conn
 * 函数功能：关闭MySQL数据库连接，释放资源
 * 实现逻辑：
 * 1. 先检查连接句柄是否为NULL，避免空指针操作导致程序崩溃
 * 2. 调用mysql_close()关闭连接，释放MySQL句柄占用的内存
 * 参数说明：
 *   - conn：MYSQL*类型，是init_mysql_conn()返回的数据库连接句柄
 * 返回值：无
 * 调用场景：
 *   - 登录/注册功能完成后，必须调用此函数关闭连接
 *   - 程序退出前，必须调用此函数释放资源
 * 调用示例：close_mysql_conn(conn);
 ********************************************************************/
void close_mysql_conn(MYSQL* conn) {
    // 空指针检查：如果conn是NULL，说明连接未建立，直接返回
    if (conn != NULL) {
        mysql_close(conn); // 关闭连接，释放资源
        printf("【数据库提示】MySQL数据库连接已关闭！\n");
    } else {
        printf("【数据库提示】无有效数据库连接，无需关闭\n");
    }
}

