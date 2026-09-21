/********************************************************************
* 文件名称：reserve.h
 * 文件用途：自习室预约功能的头文件，声明预约、取消、查询相关函数
 * 依赖文件：../db/db_helper.h（数据库工具）、../auth/user_auth.h（用户信息）
 * 使用位置：main.c（调用预约接口）、reserve/reserve.c（实现函数）
 ********************************************************************/
#ifndef RESERVE_H
#define RESERVE_H

#include "../db/db_helper.h"
#include "../auth/user_auth.h"

// 预约自习室函数
// 参数：conn-数据库连接，user_id-用户ID，room_id-自习室ID，date-预约日期，time-预约时段
// 返回值：0-预约成功，-1-预约失败
int reserve_room(MYSQL* conn, int user_id, int room_id, const char* date, const char* time);

// 取消预约函数
// 参数：conn-数据库连接，reserve_id-预约ID，user_id-用户ID（验证权限）
// 返回值：0-取消成功，-1-取消失败
int cancel_reserve(MYSQL* conn, int reserve_id, int user_id);

// 查询用户的所有预约记录
// 参数：conn-数据库连接，user_id-用户ID，out_reserves-输出预约记录（需提前分配空间）
// 返回值：>=0-预约记录数，-1-查询失败
int get_user_reserves(MYSQL* conn, int user_id, char* out_reserves);

// 查询所有可用的自习室
// 参数：conn-数据库连接，out_rooms-输出自习室列表（需提前分配空间）
// 返回值：>=0-自习室数量，-1-查询失败
int get_all_rooms(MYSQL* conn, char* out_rooms);

#endif // RESERVE_H