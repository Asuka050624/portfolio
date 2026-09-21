/********************************************************************
 * 文件名称：reserve.c
 * 文件用途：实现自习室预约、取消、查询的核心逻辑
 * 依赖文件：../db/db_helper.h、string.h、stdio.h
 * 使用场景：main.c中被调用，处理用户的预约请求
 ********************************************************************/
#include "../db/db_helper.h"
#include <string.h>
#include <stdio.h>

/********************************************************************
 * 函数名称：reserve_room
 * 函数功能：实现自习室预约，写入reservation表
 * 实现逻辑：1. 检查自习室是否可用 2. 检查是否重复预约 3. 插入预约记录
 ********************************************************************/
int reserve_room(MYSQL* conn, int user_id, int room_id, const char* date, const char* time) {
    if (conn == NULL || user_id <= 0 || room_id <= 0 || date == NULL || time == NULL) {
        printf("【预约错误】入参无效！\n");
        return -1;
    }

    // 1. 检查自习室是否可用
    char check_room_sql[256];
    sprintf(check_room_sql, "SELECT status FROM study_room WHERE id = %d", room_id);
    if (mysql_query(conn, check_room_sql) != 0) {
        printf("【预约错误】查询自习室失败：%s\n", mysql_error(conn));
        return -1;
    }
    MYSQL_RES* room_res = mysql_store_result(conn);
    MYSQL_ROW room_row = mysql_fetch_row(room_res);
    if (room_row == NULL || atoi(room_row[0]) != 1) {
        printf("【预约错误】自习室不可用！\n");
        mysql_free_result(room_res);
        return -1;
    }
    mysql_free_result(room_res);

    // 2. 检查用户是否已预约该时段的同一自习室
    char check_reserve_sql[512];
    sprintf(check_reserve_sql, "SELECT id FROM reservation WHERE user_id = %d AND room_id = %d AND reserve_date = '%s' AND reserve_time = '%s' AND status = 0",
            user_id, room_id, date, time);
    if (mysql_query(conn, check_reserve_sql) != 0) {
        printf("【预约错误】检查重复预约失败：%s\n", mysql_error(conn));
        return -1;
    }
    MYSQL_RES* res_res = mysql_store_result(conn);
    if (mysql_num_rows(res_res) > 0) {
        printf("【预约错误】已预约该时段的自习室！\n");
        mysql_free_result(res_res);
        return -1;
    }
    mysql_free_result(res_res);

    // 3. 插入预约记录
    char insert_sql[512];
    sprintf(insert_sql, "INSERT INTO reservation(user_id, room_id, reserve_date, reserve_time, status) VALUES (%d, %d, '%s', '%s', 0)",
            user_id, room_id, date, time);
    if (mysql_query(conn, insert_sql) != 0) {
        printf("【预约错误】插入预约记录失败：%s\n", mysql_error(conn));
        return -1;
    }

    printf("【预约成功】用户%d预约自习室%d（%s %s）\n", user_id, room_id, date, time);
    return 0;
}

/********************************************************************
 * 函数名称：cancel_reserve
 * 函数功能：取消用户的预约记录
 * 实现逻辑：1. 验证预约归属 2. 更新预约状态为2（已取消）
 ********************************************************************/
int cancel_reserve(MYSQL* conn, int reserve_id, int user_id) {
    if (conn == NULL || reserve_id <= 0 || user_id <= 0) {
        printf("【取消预约错误】入参无效！\n");
        return -1;
    }

    // 1. 验证预约是否属于该用户
    char check_sql[256];
    sprintf(check_sql, "SELECT id FROM reservation WHERE id = %d AND user_id = %d", reserve_id, user_id);
    if (mysql_query(conn, check_sql) != 0) {
        printf("【取消预约错误】查询预约失败：%s\n", mysql_error(conn));
        return -1;
    }
    MYSQL_RES* res = mysql_store_result(conn);
    if (mysql_num_rows(res) == 0) {
        printf("【取消预约错误】无此预约或无权限！\n");
        mysql_free_result(res);
        return -1;
    }
    mysql_free_result(res);

    // 2. 更新预约状态为已取消
    char update_sql[256];
    sprintf(update_sql, "UPDATE reservation SET status = 2 WHERE id = %d", reserve_id);
    if (mysql_query(conn, update_sql) != 0) {
        printf("【取消预约错误】更新状态失败：%s\n", mysql_error(conn));
        return -1;
    }

    printf("【取消预约成功】预约ID%d已取消\n", reserve_id);
    return 0;
}

/********************************************************************
 * 函数名称：get_user_reserves
 * 函数功能：查询用户的所有预约记录，拼接为JSON格式
 ********************************************************************/
int get_user_reserves(MYSQL* conn, int user_id, char* out_reserves) {
    if (conn == NULL || user_id <= 0 || out_reserves == NULL) {
        return -1;
    }

    char sql[256];
    sprintf(sql, "SELECT r.id, s.room_name, r.reserve_date, r.reserve_time, r.status FROM reservation r LEFT JOIN study_room s ON r.room_id = s.id WHERE r.user_id = %d", user_id);
    if (mysql_query(conn, sql) != 0) {
        printf("【查询预约错误】%s\n", mysql_error(conn));
        return -1;
    }

    MYSQL_RES* res = mysql_store_result(conn);
    int num = mysql_num_rows(res);
    if (num == 0) {
        strcpy(out_reserves, "[]");
        mysql_free_result(res);
        return 0;
    }

    // 拼接JSON数组
    strcpy(out_reserves, "[");
    MYSQL_ROW row;
    while ((row = mysql_fetch_row(res)) != NULL) {
        char reserve[256];
        sprintf(reserve, "{\"id\":%s,\"room_name\":\"%s\",\"date\":\"%s\",\"time\":\"%s\",\"status\":%s},",
                row[0], row[1], row[2], row[3], row[4]);
        strcat(out_reserves, reserve);
    }
    // 去掉最后一个逗号，添加结束符
    out_reserves[strlen(out_reserves)-1] = ']';
    mysql_free_result(res);

    return num;
}

/********************************************************************
 * 函数名称：get_all_rooms
 * 函数功能：查询所有可用的自习室，拼接为JSON格式
 ********************************************************************/
int get_all_rooms(MYSQL* conn, char* out_rooms) {
    if (conn == NULL || out_rooms == NULL) {
        return -1;
    }

    char sql[256];
    sprintf(sql, "SELECT id, room_name, capacity FROM study_room WHERE status = 1");
    if (mysql_query(conn, sql) != 0) {
        printf("【查询自习室错误】%s\n", mysql_error(conn));
        return -1;
    }

    MYSQL_RES* res = mysql_store_result(conn);
    int num = mysql_num_rows(res);
    if (num == 0) {
        strcpy(out_rooms, "[]");
        mysql_free_result(res);
        return 0;
    }

    // 拼接JSON数组
    strcpy(out_rooms, "[");
    MYSQL_ROW row;
    while ((row = mysql_fetch_row(res)) != NULL) {
        char room[256];
        sprintf(room, "{\"id\":%s,\"name\":\"%s\",\"capacity\":%s},", row[0], row[1], row[2]);
        strcat(out_rooms, room);
    }
    out_rooms[strlen(out_rooms)-1] = ']';
    mysql_free_result(res);

    return num;
}