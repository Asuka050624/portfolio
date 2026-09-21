/**
 * UserAuthService的JUnit测试类
 */
public class UserAuthServiceTest {
    
    private UserAuthService authService;
    
    // 初始化测试环境
    public void setUp() {
        authService = new UserAuthService();
        System.out.println("测试环境初始化完成");
    }
    
    // 测试成功的登录场景
    public void testAuthenticateSuccess() {
        boolean result = authService.authenticate("user", "123456");
        System.out.println("测试成功登录: " + result);
        if (result) {
            System.out.println("✓ 测试通过：成功登录验证");
        } else {
            System.out.println("✗ 测试失败：成功登录验证");
        }
    }
    
    // 测试失败的登录场景 - 用户名错误
    public void testAuthenticateInvalidUsername() {
        boolean result = authService.authenticate("invalid", "123456");
        System.out.println("测试用户名错误: " + !result);
        if (!result) {
            System.out.println("✓ 测试通过：用户名错误验证");
        } else {
            System.out.println("✗ 测试失败：用户名错误验证");
        }
    }
    
    // 测试失败的登录场景 - 密码错误
    public void testAuthenticateInvalidPassword() {
        boolean result = authService.authenticate("user", "wrong");
        System.out.println("测试密码错误: " + !result);
        if (!result) {
            System.out.println("✓ 测试通过：密码错误验证");
        } else {
            System.out.println("✗ 测试失败：密码错误验证");
        }
    }
    
    // 测试获取用户信息
    public void testGetUserInfo() {
        User user = authService.getUserInfo("user");
        if (user != null && "user".equals(user.getUsername()) && "user@example.com".equals(user.getEmail())) {
            System.out.println("✓ 测试通过：获取用户信息");
            System.out.println("  用户名: " + user.getUsername());
            System.out.println("  邮箱: " + user.getEmail());
        } else {
            System.out.println("✗ 测试失败：获取用户信息");
        }
    }
    
    // 运行所有测试
    public void runAllTests() {
        System.out.println("========== 开始运行测试 ==========");
        setUp();
        testAuthenticateSuccess();
        testAuthenticateInvalidUsername();
        testAuthenticateInvalidPassword();
        testGetUserInfo();
        System.out.println("========== 测试运行完成 ==========");
    }
    
    // 主方法，用于直接运行测试
    public static void main(String[] args) {
        UserAuthServiceTest test = new UserAuthServiceTest();
        test.runAllTests();
    }
}