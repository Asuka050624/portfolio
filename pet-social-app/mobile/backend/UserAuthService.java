/**
 * 用户认证服务类
 * 处理用户登录验证逻辑
 */
public class UserAuthService {
    
    /**
     * 验证用户登录信息
     * @param username 用户名
     * @param password 密码
     * @return 是否验证成功
     */
    public boolean authenticate(String username, String password) {
        // 简单的测试账号验证逻辑
        return "user".equals(username) && "123456".equals(password);
    }
    
    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息对象
     */
    public User getUserInfo(String username) {
        if ("user".equals(username)) {
            return new User("user", "user@example.com");
        }
        return null;
    }
}