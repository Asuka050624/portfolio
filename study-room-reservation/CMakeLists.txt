cmake_minimum_required(VERSION 3.20)
project(reserveStudyRoom C)  # 你的项目名

set(CMAKE_C_STANDARD 11)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR})

# -------------------------- 1. 包含项目根目录（找到所有子文件夹） --------------------------
include_directories(${PROJECT_SOURCE_DIR})

# -------------------------- 2. MySQL依赖（你实际的MySQL路径） --------------------------
set(MYSQL_DIR "C:/Program Files/MySQL/MySQL Server 8.0")
include_directories("${MYSQL_DIR}/include")
link_directories("${MYSQL_DIR}/lib")

# -------------------------- 3. OpenSSL依赖（你实际的OpenSSL路径） --------------------------
set(OPENSSL_DIR "D:/OpenSSL/OpenSSL-Win64")
include_directories("${OPENSSL_DIR}/include")
link_directories("${OPENSSL_DIR}/lib")

# -------------------------- 4. 你的源文件（和你截图里的文件完全一致） --------------------------
add_executable(reserveStudyRoom
        main.c  # 根目录的main.c
        # auth模块（你截图里的auth文件夹）
        auth/user_auth.c
        auth/user_auth.h
        # db模块（你截图里的db文件夹）
        db/db_helper.c
        db/db_helper.h
        # reserve模块（你截图里的reserve文件夹）
        reserve/reserve.c
        reserve/reserve.h
)

# -------------------------- 5. 链接依赖库 --------------------------
target_link_libraries(reserveStudyRoom
        libmysql          # MySQL库（你lib文件夹里的libmysql.lib）
        libssl64MD        # OpenSSL的ssl库
        libcrypto64MD     # OpenSSL的加密库
        ws2_32            # Windows Socket库
)

# -------------------------- 6. 复制运行时DLL（避免缺失） --------------------------
configure_file(
        "${MYSQL_DIR}/lib/libmysql.dll"
        ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}
        COPYONLY
)
configure_file(
        "${OPENSSL_DIR}/bin/libssl-3-x64.dll"
        ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}
        COPYONLY
)
configure_file(
        "${OPENSSL_DIR}/bin/libcrypto-3-x64.dll"
        ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}
        COPYONLY
)