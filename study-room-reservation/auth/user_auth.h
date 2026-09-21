/********************************************************************
 * 文件名称：user_auth.h
 * 文件用途：用户认证功能的头文件，专门声明登录、注册的函数接口
 *          是auth文件夹的对外接口文件，main.c等外部文件通过此头文件调用认证功能
 *          配合auth/user_auth.c使用（你已有的实现文件）
 * 使用位置：main.c（调用登录/注册函数）、auth/user_auth.c（实现函数）
 * 依赖文件：../db/db_helper.h（数据库连接工具，因为auth在下级目录，需回退上级找db）
 * 注意事项：此文件仅声明函数，具体实现写在user_auth.c中，避免重复定义
 ********************************************************************/
#ifndef USER_AUTH_H  // 头文件保护宏：防止头文件被重复包含（C语言必加）
#define USER_AUTH_H

/************************** 引入依赖头文件 **************************
 * 作用：引入数据库连接工具的声明，因为登录/注册需要操作数据库
 * ../db/ 表示回退到上级目录的db文件夹，找到db_helper.h
 ******************************************************************/
#include "../db/db_helper.h"

/************************** 函数声明 **************************
 * 作用：声明用户登录、注册的核心函数，具体实现写在user_auth.c中
 * 函数调用场景：main.c中接收前端请求后，调用这些函数处理认证逻辑
 ******************************************************************/

/**
 * 函数名称：user_register
 * 函数功能：实现用户注册功能，将用户信息插入数据库的t_user表
 * 实现逻辑（对应user_auth.c的实现）：
 *  1. 对明文密码做MD5加密（避免明文存储）
 *  2. 拼接SQL语句插入用户账号、加密密码、姓名等信息
 *  3. 调用MySQL API执行插入操作
 * 参数说明：
 *   - conn：MYSQL*类型，已建立的数据库连接句柄（来自db_helper的init_mysql_conn()）
 *   - account：const char*类型，用户输入的注册账号（如手机号/用户名）
 *   - pwd：const char*类型，用户输入的明文密码（会被MD5加密后存入数据库）
 *   - name：const char*类型，用户输入的真实姓名
 * 返回值：int类型，0表示注册成功，-1表示注册失败（如参数为空、SQL执行失败）
 * 调用示例：int ret = user_register(conn, "user001", "123456", "张三");
 */
int user_register(MYSQL* conn, const char* account, const char* pwd, const char* name);

/**
 * 函数名称：user_login
 * 函数功能：实现用户登录验证，对比数据库中的账号和加密密码
 * 实现逻辑（对应user_auth.c的实现）：
 *  1. 对用户输入的明文密码做MD5加密
 *  2. 从t_user表查询对应账号的加密密码、姓名、角色
 *  3. 对比加密密码是否一致，一致则登录成功
 * 参数说明：
 *   - conn：MYSQL*类型，已建立的数据库连接句柄
 *   - account：const char*类型，用户输入的登录账号
 *   - pwd：const char*类型，用户输入的明文密码
 *   - out_name：char*类型，输出参数，用于存储查询到的用户名（需提前分配空间）
 *   - out_role：int*类型，输出参数，用于存储用户角色（0=普通用户，1=管理员）
 * 返回值：int类型，0表示登录成功，-1表示登录失败（如账号不存在、密码错误）
 * 调用示例：char name[50]; int role; int ret = user_login(conn, "user001", "123456", name, &role);
 */
int user_login(MYSQL* conn, const char* account, const char* pwd, char* out_name, int* out_role, char* out_student_id);

#endif // USER_AUTH_H  // 头文件保护宏的结束标记
