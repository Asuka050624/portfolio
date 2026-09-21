/********************************************************************
 * 文件名称：user_auth.c
 * 文件用途：实现用户登录、注册的核心业务逻辑
 *          是项目的用户认证业务层文件，调用db文件夹中的数据库工具函数完成数据交互
 * 依赖文件：
 *   - ../db/db_helper.h：数据库工具头文件（因为当前在auth文件夹，需要回退到上级找db）
 *   - openssl/md5.h：MD5加密头文件（用于密码加密，避免明文存储）
 *   - string.h：字符串处理（strlen/strcpy）
 *   - time.h：生成用户创建时间戳
 * 使用场景：在main.c中被调用，处理用户的登录/注册请求
 * 注意事项：需要提前配置OpenSSL依赖，否则会报md5.h找不到的错误
 ********************************************************************/
// 包含数据库工具头文件：../db/ 表示回退到上级目录的db文件夹
#include "../db/db_helper.h"
// 字符串处理头文件：用于计算字符串长度、复制字符串等
#include <string.h>
// 时间头文件：用于获取系统时间戳（作为用户创建时间）
#include <time.h>
// 标准输入输出：用于打印日志/提示信息
#include <stdio.h>
// MD5加密头文件：用于将明文密码加密为密文（安全存储）
#include <openssl/md5.h>

/********************************************************************
 * 函数名称：md5_encrypt
 * 函数功能：将明文密码加密为32位MD5密文（不可逆加密，提升密码安全性）
 * 实现逻辑：
 * 1. 调用OpenSSL的MD5()函数计算明文密码的哈希值（16字节）
 * 2. 将16字节哈希值转换为32位16进制字符串（存入数据库）
 * 参数说明：
 *   - plain_pwd：const char*，传入的用户明文密码（如"123456"）
 *   - cipher_pwd：char*，输出的32位MD5密文（需提前分配33字节空间，含结束符）
 * 返回值：无
 * 调用场景：
 *   - 注册用户：将用户输入的明文密码加密后存入数据库
 *   - 登录验证：将用户输入的明文密码加密后，与数据库中的密文对比
 * 安全说明：MD5是不可逆加密，无法从密文反推明文，防止密码泄露
 ********************************************************************/
void md5_encrypt(const char* plain_pwd, char* cipher_pwd) {
    // 定义MD5哈希结果数组：MD5加密后生成16字节的二进制数据
    unsigned char md5_result[MD5_DIGEST_LENGTH];

    // 调用OpenSSL的MD5函数，计算明文密码的哈希值
    // 参数1：明文密码的字节数组；参数2：明文密码的长度；参数3：存储哈希结果的数组
    MD5((unsigned char*)plain_pwd, strlen(plain_pwd), md5_result);

    // 将16字节的二进制哈希值转换为32位的16进制字符串
    for (int i = 0; i < MD5_DIGEST_LENGTH; i++) {
        // %02x：将单字节转为两位16进制数（不足两位补0）
        sprintf(&cipher_pwd[i*2], "%02x", md5_result[i]);
    }
    // 给字符串添加结束符，避免内存越界或乱码
    cipher_pwd[32] = '\0';
}

/********************************************************************
 * 函数名称：user_register
 * 函数功能：实现用户注册功能，将用户信息插入数据库的t_user表
 * 实现逻辑：
 * 1. 校验入参是否有效（避免空指针导致程序崩溃）
 * 2. 对明文密码进行MD5加密（安全存储）
 * 3. 获取当前系统时间戳作为用户创建时间
 * 4. 拼接SQL插入语句，调用MySQL API执行插入操作
 * 参数说明：
 *   - conn：MYSQL*，已建立的数据库连接句柄（来自init_mysql_conn()）
 *   - account：const char*，用户输入的注册账号（如"user001"）
 *   - pwd：const char*，用户输入的明文密码（如"123456"）
 *   - name：const char*，用户输入的姓名（如"张三"）
 * 返回值：int，0表示注册成功，-1表示注册失败
 * 调用场景：在main.c中被调用，处理用户的注册请求
 * 示例：int ret = user_register(conn, "user001", "123456", "张三");
 ********************************************************************/
int user_register(MYSQL* conn, const char* account, const char* pwd, const char* name) {
    // 1. 入参校验：任何一个参数为NULL，直接返回失败
    if (conn == NULL || account == NULL || pwd == NULL || name == NULL) {
        printf("【注册错误】入参为空！账号/密码/姓名都不能为空\n");
        return -1;
    }

    // 2. 对明文密码进行MD5加密：定义33字节数组存储密文（32位+结束符）
    char pwd_md5[33];
    md5_encrypt(pwd, pwd_md5);
    printf("【注册提示】明文密码加密后的MD5密文：%s\n", pwd_md5);

    // 3. 获取当前系统时间戳（秒级），作为用户的创建时间
    long create_time = time(NULL);

    // 4. 拼接SQL插入语句：将用户信息插入t_user表
    // 注意：SQL语句的长度要足够，避免数组越界
    char sql[512];
    sprintf(sql, "INSERT INTO t_user(account, password, name, role, status, create_time) "
                 "VALUES ('%s', '%s', '%s', 0, 0, %ld)",
            account, pwd_md5, name, create_time);
    printf("【注册提示】即将执行的SQL语句：%s\n", sql);

    // 5. 调用MySQL API执行SQL语句：成功返回0，失败返回非0
    if (mysql_query(conn, sql) != 0) {
        printf("【注册错误】插入用户失败！错误详情：%s\n", mysql_error(conn));
        return -1;
    }

    // 6. 注册成功：打印提示信息，返回0
    printf("【注册成功】用户%s注册成功！姓名：%s\n", account, name);
    return 0;
}

/********************************************************************
 * 函数名称：user_login
 * 函数功能：实现用户登录验证功能，对比数据库中的账号密码
 * 实现逻辑：
 * 1. 校验入参是否有效
 * 2. 对用户输入的明文密码进行MD5加密
 * 3. 拼接SQL查询语句，从t_user表查询对应账号的密文密码和信息
 * 4. 获取查询结果，对比密文密码是否一致
 * 参数说明：
 *   - conn：MYSQL*，已建立的数据库连接句柄
 *   - account：const char*，用户输入的登录账号
 *   - pwd：const char*，用户输入的明文密码
 *   - out_name：char*，输出参数，存储查询到的用户名（需提前分配空间）
 *   - out_role：int*，输出参数，存储用户角色（0=普通用户，1=管理员）
 * 返回值：int，0表示登录成功，-1表示失败
 * 调用场景：在main.c中被调用，处理用户的登录请求
 * 示例：char name[20]; int role; int ret = user_login(conn, "user001", "123456", name, &role);
 ********************************************************************/
int user_login(MYSQL* conn, const char* account, const char* pwd, char* out_name, int* out_role, char* out_student_id) {
    // 1. 入参校验：避免空指针
    if (conn == NULL || account == NULL || pwd == NULL || out_name == NULL || out_role == NULL) {
        printf("【登录错误】入参为空！账号/密码不能为空\n");
        return -1;
    }

    // 2. 对用户输入的明文密码进行MD5加密
    char pwd_md5[33];
    md5_encrypt(pwd, pwd_md5);
    printf("【登录提示】输入密码的MD5密文：%s\n", pwd_md5);

    // 3. 拼接SQL查询语句：查询账号对应的用户信息（密码、姓名、角色）
    char sql[256];
    sprintf(sql, sprintf(sql, "SELECT password, name, role, student_id FROM t_user WHERE (account = '%s' OR student_id = '%s') AND status = 0", account, account);"SELECT password, name, role FROM t_user WHERE account = '%s' AND status = 0", account);
    printf("【登录提示】即将执行的SQL语句：%s\n", sql);

    // 4. 执行SQL查询：失败则返回-1
    if (mysql_query(conn, sql) != 0) {
        printf("【登录错误】查询用户失败！错误详情：%s\n", mysql_error(conn));
        return -1;
    }

    // 5. 获取查询结果集：mysql_store_result()将结果读取到内存
    MYSQL_RES* res = mysql_store_result(conn);
    if (res == NULL) {
        printf("【登录错误】未查询到用户%s的信息\n", account);
        return -1;
    }

    // 6. 获取结果集中的行：如果没有行，说明账号不存在
    MYSQL_ROW row = mysql_fetch_row(res);
    if (row == NULL) {
        printf("【登录错误】账号%s不存在！\n", account);
        mysql_free_result(res); // 释放结果集资源
        return -1;
    }

    // 7. 对比密码：数据库中的密文（row[0]）和用户输入的密文（pwd_md5）
    if (strcmp(row[0], pwd_md5) != 0) {
        printf("【登录错误】密码错误！\n");
        mysql_free_result(res); // 释放结果集资源
        return -1;
    }

    // 8. 登录成功：给输出参数赋值，释放结果集
    strcpy(out_name, row[1]);          // 复制用户名到out_name
    *out_role = atoi(row[2]);          // 转换角色为整数（row[2]是字符串）
    strcpy(out_student_id, row[3] ? row[3] : ""); // 给学号赋值，学号为空时填空字符串
    mysql_free_result(res);            // 必须释放结果集，避免内存泄漏

    // 9. 打印登录成功信息，返回0
    printf("【登录成功】账号%s登录成功！欢迎你：%s，角色：%s\n",
           account, out_name, *out_role == 1 ? "管理员" : "普通用户");
    return 0;
}