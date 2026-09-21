/**
 * 后端应用主类
 * 用于演示用户认证功能
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("宠物健康管理系统后端服务启动");
        
        // 创建认证服务实例
        UserAuthService authService = new UserAuthService();
        
        // 演示登录验证
        System.out.println("\n===== 演示登录验证 =====");
        
        // 测试成功登录
        boolean successLogin = authService.authenticate("user", "123456");
        System.out.println("测试账号登录结果: " + (successLogin ? "成功" : "失败"));
        
        if (successLogin) {
            // 获取用户信息
            User user = authService.getUserInfo("user");
            if (user != null) {
                System.out.println("用户信息:");
                System.out.println("  用户名: " + user.getUsername());
                System.out.println("  邮箱: " + user.getEmail());
            }
        }
        
        // 测试失败登录
        boolean failedLogin = authService.authenticate("wronguser", "123456");
        System.out.println("错误账号登录结果: " + (failedLogin ? "成功" : "失败"));
        
        System.out.println("\n===== 启动测试 =====");
        // 运行测试
        System.out.println("可以运行 test 目录下的 UserAuthServiceTest 类进行单元测试");
        System.out.println("命令: javac backend\\test\\UserAuthServiceTest.java && java -cp backend UserAuthServiceTest");        
        System.out.println("\n后端服务演示完成");
    }
}