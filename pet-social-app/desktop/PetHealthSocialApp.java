import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PetHealthSocialApp extends JFrame {
    // 导航面板和内容面板
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel currentTimeLabel;
    private Timer timeUpdateTimer;
    private SimpleDateFormat dateFormat;
    
    // 数据模型
    private List<Pet> pets = new ArrayList<>();
    private List<HealthReminder> healthReminders = new ArrayList<>();
    private List<SocialPost> socialPosts = new ArrayList<>();
    private List<ServiceProvider> serviceProviders = new ArrayList<>();
    
    // UI组件
    private JButton exitButton;
    
    // 文件路径常量
    private static final String PETS_FILE = "pets.dat";
    private static final String REMINDERS_FILE = "reminders.dat";
    private static final String SOCIAL_POSTS_FILE = "social_posts.dat";
    private static final String SERVICE_PROVIDERS_FILE = "service_providers.dat";

    public PetHealthSocialApp() {
        // 设置主窗口属性
        setTitle("宠物健康管理与社交平台");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // 初始化日期格式化和时钟
        dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        currentTimeLabel = new JLabel(dateFormat.format(new Date()));
        currentTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 启动时钟更新
        timeUpdateTimer = new Timer(1000, e -> currentTimeLabel.setText(dateFormat.format(new Date())));
        timeUpdateTimer.start();
        
        // 初始化模拟数据
        initializeMockData();
        
        // 初始化每日提醒检查
        initializeDailyReminderCheck();
        
        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        // 创建主容器，使用卡片布局，用于切换欢迎页面和主界面
        JPanel container = new JPanel();
        CardLayout containerLayout = new CardLayout();
        container.setLayout(containerLayout);
        
        // 创建欢迎页面
        JPanel welcomePanel = createWelcomePanel(containerLayout);
        
        // 创建主界面
        JPanel mainPanel = createMainPanel();
        
        // 添加面板到容器
        container.add(welcomePanel, "welcome");
        container.add(mainPanel, "main");
        
        // 设置容器为内容窗格
        setContentPane(container);
        
        // 初始显示欢迎页面
        containerLayout.show(container, "welcome");
    }
    
    // 创建欢迎页面
    private JPanel createWelcomePanel(CardLayout containerLayout) {
        JPanel welcomePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制宠物相关的渐变背景
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                
                // 创建渐变背景
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 248, 255), width, height, new Color(255, 228, 196));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
                
                // 绘制简单的宠物图形（猫咪轮廓）
                g2d.setColor(new Color(105, 105, 105));
                g2d.setStroke(new BasicStroke(3));
                
                // 猫头轮廓
                g2d.drawArc(width/2 - 100, height/2 - 100, 200, 180, 0, 180);
                
                // 耳朵
                int[] ear1X = {width/2 - 100, width/2 - 130, width/2 - 80};
                int[] ear1Y = {height/2 - 100, height/2 - 180, height/2 - 150};
                int[] ear2X = {width/2 + 100, width/2 + 130, width/2 + 80};
                int[] ear2Y = {height/2 - 100, height/2 - 180, height/2 - 150};
                g2d.drawPolygon(ear1X, ear1Y, 3);
                g2d.drawPolygon(ear2X, ear2Y, 3);
                
                // 眼睛
                g2d.fillOval(width/2 - 50, height/2 - 40, 30, 40);
                g2d.fillOval(width/2 + 20, height/2 - 40, 30, 40);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(width/2 - 45, height/2 - 35, 10, 15);
                g2d.fillOval(width/2 + 25, height/2 - 35, 10, 15);
                
                // 鼻子和嘴
                g2d.setColor(new Color(105, 105, 105));
                g2d.fillOval(width/2 - 10, height/2, 20, 20);
                g2d.drawLine(width/2, height/2 + 20, width/2, height/2 + 40);
                g2d.drawArc(width/2 - 30, height/2 + 20, 60, 30, 0, -180);
            }
        };
        
        welcomePanel.setLayout(new BorderLayout());
        
        // 欢迎标题
        JLabel titleLabel = new JLabel("欢迎使用宠物健康管理与社交平台", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 36));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setBorder(new EmptyBorder(100, 0, 0, 0));
        
        // 进入按钮 - 现代化设计
        JButton enterButton = new JButton("点击进入系统");
        enterButton.setFont(new Font("微软雅黑", Font.BOLD, 24));
        enterButton.setPreferredSize(new Dimension(300, 80));
        enterButton.setBackground(new Color(70, 130, 180));
        enterButton.setForeground(Color.WHITE);
        enterButton.setBorder(new RoundedBorder(Color.WHITE, 40, 0));
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        enterButton.setFocusPainted(false);
        
        // 添加悬停和点击效果
        enterButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                enterButton.setBackground(new Color(90, 150, 200));
                enterButton.setBorder(new RoundedBorder(Color.WHITE, 40, 2));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                enterButton.setBackground(new Color(70, 130, 180));
                enterButton.setBorder(new RoundedBorder(Color.WHITE, 40, 0));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                enterButton.setBackground(new Color(60, 120, 170));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                enterButton.setBackground(new Color(90, 150, 200));
            }
        });
        enterButton.addActionListener(e -> {
            // 切换到主界面
            containerLayout.show(getContentPane(), "main");
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(enterButton);
        buttonPanel.setBorder(new EmptyBorder(50, 0, 50, 0));
        
        // 版权信息
        JLabel copyrightLabel = new JLabel("© 2024 宠物健康管理与社交平台 - 让宠物生活更美好", JLabel.CENTER);
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        copyrightLabel.setForeground(Color.GRAY);
        
        welcomePanel.add(titleLabel, BorderLayout.NORTH);
        welcomePanel.add(buttonPanel, BorderLayout.CENTER);
        welcomePanel.add(copyrightLabel, BorderLayout.SOUTH);
        
        return welcomePanel;
    }
    
    // 创建主界面面板
    private JPanel createMainPanel() {
        // 创建顶部标题栏 - 现代化设计
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        topPanel.setLayout(new BorderLayout(15, 0));
        
        JLabel titleLabel = new JLabel("宠物健康管理与社交平台");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        // 美化时钟标签
        currentTimeLabel.setForeground(Color.WHITE);
        topPanel.add(currentTimeLabel, BorderLayout.EAST);
        
        // 创建侧边导航栏 - 现代化设计
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(190, getHeight()));
        sidebarPanel.setBackground(new Color(248, 249, 250));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        
        // 创建导航按钮
        createNavigationButtons();
        
        // 创建内容面板，使用卡片布局
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // 创建各个功能面板
        JPanel petProfilePanel = createPetProfilePanel();
        JPanel healthReminderPanel = createHealthReminderPanel();
        JPanel socialPanel = createSocialPanel();
        JPanel servicePanel = createServicePanel();
        JPanel pushReminderPanel = createPushReminderPanel();
        
        // 添加面板到内容面板
        contentPanel.add(petProfilePanel, "petProfile");
        contentPanel.add(healthReminderPanel, "healthReminder");
        contentPanel.add(socialPanel, "social");
        contentPanel.add(servicePanel, "service");
        contentPanel.add(pushReminderPanel, "pushReminder");
        
        // 创建主面板并添加组件
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    // 初始化模拟数据
    private void initializeMockData() {
        // 扩充宠物数据（至少20只，包含各种类型的宠物）
        pets.add(new Pet(1, "奥利奥", "边境牧羊犬", "2020-03-15", "公", "黑白花色", "张三", "13800138001"));
        pets.add(new Pet(2, "奶茶", "布偶猫", "2021-05-20", "母", "海豹双色", "李四", "13900139002"));
        pets.add(new Pet(3, "豆豆", "柯基犬", "2019-08-10", "公", "三色", "王五", "13700137003"));
        pets.add(new Pet(4, "灰灰", "苏格兰折耳猫", "2021-01-18", "公", "灰色", "赵六", "13600136004"));
        pets.add(new Pet(5, "小白", "萨摩耶", "2020-07-05", "母", "白色", "孙七", "13500135005"));
        pets.add(new Pet(6, "小黄", "金丝雀", "2022-03-10", "公", "黄色", "周八", "13400134006"));
        pets.add(new Pet(7, "贝贝", "仓鼠", "2022-05-20", "母", "三线", "吴九", "13300133007"));
        pets.add(new Pet(8, "花花", "虎皮鹦鹉", "2022-01-15", "公", "黄绿相间", "郑十", "13200132008"));
        pets.add(new Pet(9, "奇奇", "缅甸陆龟", "2020-09-01", "公", "棕色花纹", "钱一", "13100131009"));
        pets.add(new Pet(10, "多多", "金毛犬", "2019-11-11", "公", "金黄色", "孙二", "13000130010"));
        pets.add(new Pet(11, "宝宝", "吉娃娃", "2021-02-20", "母", "棕色", "李四", "13900139011"));
        pets.add(new Pet(12, "星星", "守宫", "2022-04-05", "公", "橙色斑点", "王五", "13700137012"));
        pets.add(new Pet(13, "胖胖", "加菲猫", "2021-08-15", "公", "红虎斑", "赵六", "13600136013"));
        pets.add(new Pet(14, "松松", "松鼠", "2022-06-10", "母", "棕色", "张三", "13800138014"));
        pets.add(new Pet(15, "小黑", "黑猫", "2020-10-30", "母", "纯黑", "李四", "13900139015"));
        pets.add(new Pet(16, "亮亮", "巴西龟", "2021-03-05", "公", "绿色", "王五", "13700137016"));
        pets.add(new Pet(17, "甜甜", "泰迪犬", "2020-05-15", "母", "棕色卷毛", "赵六", "13600136017"));
        pets.add(new Pet(18, "团团", "荷兰猪", "2022-07-20", "公", "黑白花", "孙七", "13500135018"));
        pets.add(new Pet(19, "圆圆", "比熊犬", "2020-09-10", "母", "白色", "周八", "13400134019"));
        pets.add(new Pet(20, "默默", "龙猫", "2022-02-14", "公", "灰色", "吴九", "13300133020"));
        pets.add(new Pet(21, "跳跳", "兔子", "2022-08-05", "母", "垂耳白色", "郑十", "13200132021"));
        pets.add(new Pet(22, "你代", "小狗", "2023-01-10", "公", "棕色", "陈一", "13800138022"));
        pets.add(new Pet(23, "你羽", "小狗", "2023-02-15", "母", "黑色", "林二", "13900139023"));
        pets.add(new Pet(24, "你前", "猪", "2023-03-20", "公", "黑白花色", "黄三", "13700137024"));
        
        // 扩充健康提醒数据（至少20条新的提醒）
        // 获取今天的日期格式化为yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        // 为奶茶（ID:2）添加猫三联三次接种提醒
        healthReminders.add(new HealthReminder(1, 2, "疫苗接种", "猫三联第一针", "2023-12-15", "已完成", true));
        healthReminders.add(new HealthReminder(2, 2, "疫苗接种", "猫三联第二针", "2024-01-15", "已完成", true));
        healthReminders.add(new HealthReminder(3, 2, "疫苗接种", "猫三联第三针", today, "待完成", true));
        
        // 为奥利奥（ID:1）添加狂犬疫苗三次接种提醒
        healthReminders.add(new HealthReminder(4, 1, "疫苗接种", "狂犬疫苗第一针", "2023-11-15", "已完成", true));
        healthReminders.add(new HealthReminder(5, 1, "疫苗接种", "狂犬疫苗第二针", "2023-12-15", "已完成", true));
        healthReminders.add(new HealthReminder(6, 1, "疫苗接种", "狂犬疫苗第三针", "2024-01-15", "待完成", true));
        // 原有提醒数据，ID从7开始，避免与新添加的提醒ID冲突
        healthReminders.add(new HealthReminder(7, 2, "驱虫", "体内驱虫", "2023-11-20", "已完成", true));
        healthReminders.add(new HealthReminder(8, 3, "体检", "年度体检", "2024-01-10", "待完成", true));
        healthReminders.add(new HealthReminder(9, 1, "驱虫", "体外驱虫", "2023-12-05", "待完成", false));
        healthReminders.add(new HealthReminder(10, 4, "疫苗接种", "猫三联", "2024-01-15", "待完成", true));
        healthReminders.add(new HealthReminder(11, 5, "体检", "年度体检", "2024-02-01", "待完成", true));
        healthReminders.add(new HealthReminder(12, 6, "修喙", "鸟类修喙", "2024-01-08", "待完成", true));
        healthReminders.add(new HealthReminder(13, 7, "笼舍清洁", "仓鼠笼清洁", "2024-01-05", "已完成", true));
        healthReminders.add(new HealthReminder(14, 8, "剪羽", "鹦鹉剪羽", "2024-01-20", "待完成", false));
        healthReminders.add(new HealthReminder(15, 9, "泡澡", "陆龟泡澡", "2024-01-03", "已完成", true));
        healthReminders.add(new HealthReminder(16, 10, "疫苗接种", "犬六联", "2024-02-10", "待完成", true));
        healthReminders.add(new HealthReminder(17, 11, "美容", "修剪毛发", "2024-01-25", "待完成", true));
        healthReminders.add(new HealthReminder(18, 12, "喂食", "昆虫饲料补充", "2024-01-02", "已完成", true));
        healthReminders.add(new HealthReminder(19, 13, "驱虫", "猫体内驱虫", "2024-01-18", "待完成", false));
        healthReminders.add(new HealthReminder(20, 14, "笼舍检查", "松鼠笼安全检查", "2024-01-07", "已完成", true));
        healthReminders.add(new HealthReminder(21, 15, "疫苗接种", "猫瘟疫苗", "2024-02-05", "待完成", true));
        healthReminders.add(new HealthReminder(22, 16, "换水", "龟缸换水", "2024-01-01", "已完成", true));
        healthReminders.add(new HealthReminder(23, 17, "美容", "泰迪造型修剪", "2024-01-22", "待完成", true));
        healthReminders.add(new HealthReminder(24, 18, "垫料更换", "荷兰猪垫料更换", "2024-01-04", "已完成", true));
        healthReminders.add(new HealthReminder(25, 19, "驱虫", "犬体内驱虫", "2024-01-12", "待完成", false));
        healthReminders.add(new HealthReminder(26, 20, "笼舍清洁", "龙猫笼清洁", "2024-01-06", "已完成", true));
        healthReminders.add(new HealthReminder(27, 21, "剪指甲", "兔子剪指甲", "2024-01-28", "待完成", true));
        healthReminders.add(new HealthReminder(28, 2, "疫苗接种", "猫三联加强针", "2024-02-15", "待完成", true));
        healthReminders.add(new HealthReminder(29, 5, "驱虫", "犬体内驱虫", "2024-01-30", "待完成", false));
        healthReminders.add(new HealthReminder(30, 10, "体检", "老年犬体检", "2024-03-01", "待完成", true));
        
        // 社交动态数据（带预设评论）
        SocialPost post1 = new SocialPost(1, "张三", "奥利奥今天学会了新技能！", "2023-11-28 14:30", 25, 5);
        post1.addComment("李四: 好棒呀！奥利奥真聪明");
        post1.addComment("王五: 这是什么技能？能教教我家豆豆吗？");
        post1.addComment("赵六: 我家灰灰只会睡觉...");
        socialPosts.add(post1);
        
        SocialPost post2 = new SocialPost(2, "李四", "奶茶的新造型，可爱吗？", "2023-11-27 10:15", 42, 12);
        post2.addComment("张三: 太可爱了！在哪里做的造型？");
        post2.addComment("钱七: 这个颜色搭配得真好");
        post2.addComment("孙八: 奶茶看起来很享受嘛");
        socialPosts.add(post2);
        
        SocialPost post3 = new SocialPost(3, "王五", "豆豆和它的新朋友", "2023-11-26 16:45", 31, 8);
        post3.addComment("李四: 两个小可爱！在一起玩得很开心呢");
        post3.addComment("张三: 这是在哪里认识的新朋友呀？");
        socialPosts.add(post3);
        
        SocialPost post4 = new SocialPost(4, "赵六", "灰灰又在沙发上睡觉了～", "2023-11-25 09:30", 18, 3);
        post4.addComment("钱七: 好慵懒的样子，真可爱");
        socialPosts.add(post4);
        
        SocialPost post5 = new SocialPost(5, "钱七", "小白在雪地里玩得很开心！", "2023-11-24 11:20", 56, 15);
        post5.addComment("李四: 雪地里的小白真漂亮！");
        post5.addComment("张三: 我们家奥利奥也很喜欢雪");
        post5.addComment("孙八: 看它跑得多开心呀");
        socialPosts.add(post5);
        
        // 服务提供商数据（修改为河南郑州金水区，所有门店提供所有服务）
        serviceProviders.add(new ServiceProvider(1, "郑州市宠物健康中心", "综合服务", "河南省郑州市金水区农业路100号", "0371-12345678", "周一至周日 8:30-21:30"));
        serviceProviders.add(new ServiceProvider(2, "金水宠爱宠物乐园", "综合服务", "河南省郑州市金水区花园路58号", "0371-87654321", "周一至周日 9:00-20:00"));
        serviceProviders.add(new ServiceProvider(3, "中原宠物之家", "综合服务", "河南省郑州市金水区经三路66号", "0371-56781234", "周一至周日 9:30-21:00"));
        serviceProviders.add(new ServiceProvider(4, "绿城宠物医院", "综合服务", "河南省郑州市金水区文化路88号", "0371-43215678", "全天候服务"));
        serviceProviders.add(new ServiceProvider(5, "金水区萌宠乐园", "综合服务", "河南省郑州市金水区东风路120号", "0371-89012345", "周一至周日 10:00-20:30"));
        serviceProviders.add(new ServiceProvider(6, "郑州宠爱无限", "综合服务", "河南省郑州市金水区未来路99号", "0371-54326789", "周一至周日 9:00-21:00"));
        serviceProviders.add(new ServiceProvider(7, "宠物港湾", "综合服务", "河南省郑州市金水区丰产路76号", "0371-67890123", "周一至周日 8:00-22:00"));
        serviceProviders.add(new ServiceProvider(8, "金水区宠物乐园", "综合服务", "河南省郑州市金水区红专路45号", "0371-34567890", "周一至周日 9:30-20:00"));
        serviceProviders.add(new ServiceProvider(9, "郑州爱宠中心", "综合服务", "河南省郑州市金水区南阳路111号", "0371-23456789", "周一至周日 8:30-21:00"));
        serviceProviders.add(new ServiceProvider(10, "金水区宠物天地", "综合服务", "河南省郑州市金水区纬五路33号", "0371-78901234", "周一至周日 10:00-22:00"));
    }
    
    // 创建导航按钮
    private void createNavigationButtons() {
        // 设置侧边栏背景色
        sidebarPanel.setBackground(new Color(245, 245, 250));
        
        // 添加标题
        JLabel navTitleLabel = new JLabel("功能导航");
        navTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        navTitleLabel.setForeground(new Color(70, 70, 80));
        navTitleLabel.setBorder(new EmptyBorder(15, 10, 10, 10));
        sidebarPanel.add(navTitleLabel);
        
        // 导航按钮列表
        String[] navItems = {"宠物档案", "健康提醒", "社交互动", "服务预约", "推送提醒"};
        String[] navKeys = {"petProfile", "healthReminder", "social", "service", "pushReminder"};
        
        // 创建按钮样式
        Color primaryColor = new Color(70, 130, 180); // 深蓝色主色调
        Color hoverColor = new Color(50, 100, 150);
        Color activeColor = new Color(30, 80, 120);
        
        for (int i = 0; i < navItems.length; i++) {
            final String key = navKeys[i];
            JButton button = new JButton(navItems[i]);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            button.setMargin(new Insets(10, 5, 10, 5));
            button.setBackground(Color.WHITE);
            button.setForeground(primaryColor);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // 添加圆角
            if (button.getBorder() instanceof CompoundBorder) {
                CompoundBorder cb = (CompoundBorder) button.getBorder();
                LineBorder lb = (LineBorder) cb.getOutsideBorder();
                button.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(lb.getLineColor(), 15, 1),
                    cb.getInsideBorder()
                ));
            }
            
            // 添加鼠标悬停效果
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(235, 240, 255));
                    button.setForeground(hoverColor);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(Color.WHITE);
                    button.setForeground(primaryColor);
                }
            });
            
            // 添加按钮点击事件
            button.addActionListener(e -> {
                cardLayout.show(contentPanel, key);
                // 更新所有按钮样式
                for (Component comp : sidebarPanel.getComponents()) {
                    if (comp instanceof JButton && !(comp == exitButton)) {
                        JButton btn = (JButton) comp;
                        btn.setBackground(Color.WHITE);
                        btn.setForeground(primaryColor);
                    }
                }
                // 设置当前选中按钮样式
                button.setBackground(primaryColor);
                button.setForeground(Color.WHITE);
            });
            
            sidebarPanel.add(button);
            sidebarPanel.add(Box.createVerticalStrut(8));
        }
        
        // 添加底部间距
        sidebarPanel.add(Box.createVerticalGlue());
        
        // 添加退出系统按钮（放在最底部）
        sidebarPanel.add(Box.createVerticalStrut(15)); // 添加间距
        exitButton = new JButton("退出系统");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        exitButton.setMargin(new Insets(10, 5, 10, 5));
        exitButton.setBackground(new Color(220, 53, 69)); // 红色背景
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        
        // 添加圆角
        exitButton.setBorder(new RoundedBorder(exitButton.getBackground(), 15, 1));
        
        // 添加鼠标悬停效果
        exitButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                exitButton.setBackground(new Color(200, 30, 50));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                exitButton.setBackground(new Color(220, 53, 69));
            }
        });
        
        // 添加退出系统事件
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要退出系统吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0); // 退出程序
            }
        });
        
        sidebarPanel.add(exitButton);
    }
    
    // 创建宠物档案面板
    private JPanel createPetProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建标题
        JLabel titleLabel = new JLabel("宠物");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 添加宠物按钮
        JButton addPetButton = new JButton("添加宠物");
        addPetButton.addActionListener(e -> showAddPetDialog());
        titlePanel.add(addPetButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建宠物卡片面板
        JPanel petCardsPanel = new JPanel();
        petCardsPanel.setLayout(new BoxLayout(petCardsPanel, BoxLayout.Y_AXIS));
        
        // 添加每个宠物的卡片
        for (Pet pet : pets) {
            petCardsPanel.add(createPetCard(pet));
            petCardsPanel.add(Box.createVerticalStrut(15));
        }
        
        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(petCardsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 创建单个宠物卡片
    private JPanel createPetCard(Pet pet) {
        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250)); // 进一步增加高度以确保所有信息可见
        cardPanel.setPreferredSize(new Dimension(900, 250)); // 增加整体宽度
        cardPanel.setLayout(new BorderLayout(15, 15));
        cardPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 添加圆角边框和阴影
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(220, 220, 230), 15, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 添加阴影效果
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(3, 3, 6, 6, new Color(0, 0, 0, 30)),
            cardPanel.getBorder()
        ));
        
        // 宠物图片占位符 - 现代化设计
        JPanel imagePanel = new JPanel(new GridBagLayout()); // 使用GridBagLayout确保居中
        imagePanel.setPreferredSize(new Dimension(90, 90));
        imagePanel.setMaximumSize(new Dimension(90, 90));
        imagePanel.setBorder(new RoundedBorder(new Color(200, 200, 220), 45, 1)); // 圆形边框
        imagePanel.setBackground(new Color(230, 230, 240));
        
        // 将宠物名字放在灰色框框中，确保居中显示
        JLabel petNameLabel = new JLabel(pet.getName());
        petNameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        petNameLabel.setForeground(new Color(60, 60, 100));
        petNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        petNameLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePanel.add(petNameLabel, new GridBagConstraints());
        cardPanel.add(imagePanel, BorderLayout.WEST);
        
        // 宠物信息面板 - 使用更简单直接的布局
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(true);
        infoPanel.setBackground(Color.WHITE); // 确保背景为白色
        infoPanel.setPreferredSize(new Dimension(700, 250)); // 大幅增加宽度
        infoPanel.setMinimumSize(new Dimension(700, 250));
        
        // 宠物品种和年龄 - 放在最上方
        JLabel breedLabel = new JLabel(pet.getType() + " | " + pet.getAge() + "岁");
        breedLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        breedLabel.setForeground(new Color(30, 30, 60));
        breedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(breedLabel);
        infoPanel.add(Box.createVerticalStrut(8)); // 增加间距
        
        // 主人信息 - 使用简单的JLabel直接显示，确保100%可见
        String ownerName = pet.getOwnerName() != null && !pet.getOwnerName().isEmpty() ? pet.getOwnerName() : "未设置";
        String ownerContact = pet.getOwnerContact() != null && !pet.getOwnerContact().isEmpty() ? pet.getOwnerContact() : "未设置";
        
        // 创建一个独立的JLabel来显示主人信息
        JLabel ownerInfoLabel = new JLabel("主人: " + ownerName + " | 联系方式: " + ownerContact);
        ownerInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        ownerInfoLabel.setForeground(new Color(60, 60, 100));
        ownerInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 为了突出显示，添加一个带有背景色的面板包裹主人信息
        JPanel ownerInfoPanel = new JPanel();
        ownerInfoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        ownerInfoPanel.setOpaque(true);
        ownerInfoPanel.setBackground(new Color(240, 240, 250)); // 浅色背景
        ownerInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(200, 200, 220), 5, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        ownerInfoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        ownerInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ownerInfoPanel.add(ownerInfoLabel);
        
        // 直接将主人信息面板添加到infoPanel的顶部位置
        infoPanel.add(ownerInfoPanel);
        infoPanel.add(Box.createVerticalStrut(8)); // 增加间距
        
        // 基本信息 - 现代化样式
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(2, 1, 0, 5)); // 使用GridLayout确保信息完整显示
        detailsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        detailsPanel.setOpaque(true);
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // 性别信息行 - 确保完整显示
        JPanel genderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        genderRow.setOpaque(false);
        JLabel genderLabel = new JLabel("性别: " + pet.getGender());
        genderLabel.setFont(new Font("微软雅黑", Font.BOLD, 16)); // 使用大号粗体字体
        genderLabel.setForeground(Color.BLACK); // 黑色文本确保最高可见度
        genderLabel.setPreferredSize(new Dimension(100, 30)); // 设置固定大小
        genderRow.add(genderLabel);
        detailsPanel.add(genderRow);
        
        // 其他基本信息行
        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoRow.setOpaque(false);
        
        JLabel birthDateLabel = new JLabel("出生日期: " + pet.getBirthDate());
        birthDateLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        birthDateLabel.setForeground(new Color(80, 80, 100));
        infoRow.add(birthDateLabel);
        
        JLabel colorLabel = new JLabel("毛色: " + pet.getColor());
        colorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        colorLabel.setForeground(new Color(80, 80, 100));
        infoRow.add(colorLabel);
        
        detailsPanel.add(infoRow);
        
        // 添加推送状态
        for (HealthReminder reminder : healthReminders) {
            if (reminder.getPetId() == pet.getId() && reminder.getStatus().equals("待完成")) {
                JLabel pushStatusLabel = new JLabel("提醒状态: " + (reminder.isPushed() ? "已推送" : "未推送"));
                pushStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                pushStatusLabel.setForeground(reminder.isPushed() ? new Color(0, 150, 0) : new Color(220, 20, 60));
                detailsPanel.add(pushStatusLabel);
                break;
            }
        }
        
        // 添加详细信息面板到主信息面板
        infoPanel.add(detailsPanel);
        
        cardPanel.add(infoPanel, BorderLayout.CENTER);

        // 健康状态面板 - 现代化样式
        JPanel healthStatusPanel = new JPanel();
        healthStatusPanel.setLayout(new BoxLayout(healthStatusPanel, BoxLayout.Y_AXIS));
        healthStatusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        healthStatusPanel.setOpaque(false); // 设置为透明，不影响卡片背景
        
        // 获取宠物的健康提醒
        List<HealthReminder> petReminders = getPetHealthReminders(pet.getId());
        
        if (petReminders.isEmpty()) {
            // 添加健康状态标题
            JLabel healthTitleLabel = new JLabel("健康状态");
            healthTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            healthTitleLabel.setForeground(new Color(60, 60, 100));
            healthStatusPanel.add(healthTitleLabel);
            healthStatusPanel.add(Box.createVerticalStrut(3));
            
            JLabel noReminderLabel = new JLabel("暂无健康提醒");
            noReminderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            noReminderLabel.setForeground(new Color(150, 150, 150));
            healthStatusPanel.add(noReminderLabel);
        } else {
            // 显示疫苗和驱虫提醒状态
            boolean hasUpcomingVaccine = false;
            boolean hasUpcomingDeworming = false;
            
            for (HealthReminder reminder : petReminders) {
                if (reminder.getType().equals("疫苗接种") && reminder.getStatus().equals("待完成")) {
                    hasUpcomingVaccine = true;
                }
                if (reminder.getType().equals("驱虫") && reminder.getStatus().equals("待完成")) {
                    hasUpcomingDeworming = true;
                }
            }
            
            // 添加健康状态标题
            JLabel healthTitleLabel = new JLabel("健康状态");
            healthTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            healthTitleLabel.setForeground(new Color(60, 60, 100));
            healthStatusPanel.add(healthTitleLabel);
            healthStatusPanel.add(Box.createVerticalStrut(3));
            
            JLabel vaccineStatusLabel = new JLabel("疫苗接种: " + (hasUpcomingVaccine ? "有未完成" : "已完成"));
            vaccineStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            vaccineStatusLabel.setForeground(hasUpcomingVaccine ? new Color(220, 20, 60) : new Color(0, 150, 0));
            
            JLabel dewormingStatusLabel = new JLabel("驱虫: " + (hasUpcomingDeworming ? "有未完成" : "已完成"));
            dewormingStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            dewormingStatusLabel.setForeground(hasUpcomingDeworming ? new Color(220, 20, 60) : new Color(0, 150, 0));
            
            healthStatusPanel.add(vaccineStatusLabel);
            healthStatusPanel.add(dewormingStatusLabel);
        }
        
        cardPanel.add(healthStatusPanel, BorderLayout.SOUTH);

        // 操作按钮 - 现代化设计
        JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        actionsPanel.setOpaque(false);
        
        // 编辑按钮
        JButton editButton = new JButton("编辑");
        editButton.setPreferredSize(new Dimension(90, 30));
        editButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        editButton.setBorder(new RoundedBorder(new Color(70, 130, 180), 15, 1));
        editButton.setBackground(Color.WHITE);
        editButton.setForeground(new Color(70, 130, 180));
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                editButton.setBackground(new Color(70, 130, 180));
                editButton.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                editButton.setBackground(Color.WHITE);
                editButton.setForeground(new Color(70, 130, 180));
            }
        });
        
        editButton.addActionListener(e -> showEditPetDialog(pet));
        
        // 删除按钮
        JButton deleteButton = new JButton("删除");
        deleteButton.setPreferredSize(new Dimension(90, 30));
        deleteButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        deleteButton.setBorder(new RoundedBorder(new Color(220, 20, 60), 15, 1));
        deleteButton.setBackground(Color.WHITE);
        deleteButton.setForeground(new Color(220, 20, 60));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setBackground(new Color(220, 20, 60));
                deleteButton.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setBackground(Color.WHITE);
                deleteButton.setForeground(new Color(220, 20, 60));
            }
        });
        
        deleteButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "确定要删除宠物" + pet.getName() + "吗？", "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                pets.remove(pet);
                // 重新加载面板
                refreshPetProfilePanel();
            }
        });
        
        actionsPanel.add(editButton);
        actionsPanel.add(deleteButton);
        cardPanel.add(actionsPanel, BorderLayout.EAST);
        
        return cardPanel;
    }
    
    // 刷新宠物档案面板
    private void refreshPetProfilePanel() {
        // 首先确保我们有宠物数据
        if (pets.isEmpty()) {
            System.out.println("没有宠物数据可供显示");
            return;
        }
        
        // 使用更简单直接的方式刷新面板
        JPanel newPetProfilePanel = createPetProfilePanel();
        
        // 移除旧面板并添加新面板
        contentPanel.removeAll();
        
        // 重新添加所有面板
        contentPanel.add(newPetProfilePanel, "petProfile");
        contentPanel.add(createHealthReminderPanel(), "healthReminder");
        contentPanel.add(createSocialPanel(), "social");
        contentPanel.add(createServicePanel(), "service");
        contentPanel.add(createPushReminderPanel(), "pushReminder");
        
        // 强制重绘
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // 显示宠物档案面板
        cardLayout.show(contentPanel, "petProfile");
    }
    
    // 添加宠物对话框
    private void showAddPetDialog() {
        JDialog dialog = new JDialog(this, "添加宠物", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("宠物名称: "));
        JTextField nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("宠物品种: "));
        JTextField typeField = new JTextField();
        panel.add(typeField);
        
        panel.add(new JLabel("出生日期: "));
        JTextField birthDateField = new JTextField();
        panel.add(birthDateField);
        
        panel.add(new JLabel("性别: "));
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"公", "母"});
        panel.add(genderComboBox);
        
        panel.add(new JLabel("毛色: "));
        JTextField colorField = new JTextField();
        panel.add(colorField);
        
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            String name = nameField.getText();
            String type = typeField.getText();
            String birthDate = birthDateField.getText();
            String gender = (String) genderComboBox.getSelectedItem();
            String color = colorField.getText();
            
            if (!name.isEmpty() && !type.isEmpty() && !birthDate.isEmpty()) {
                int id = pets.size() > 0 ? pets.get(pets.size() - 1).getId() + 1 : 1;
                pets.add(new Pet(id, name, type, birthDate, gender, color));
                refreshPetProfilePanel();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请填写必要信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 编辑宠物对话框
    private void showEditPetDialog(Pet pet) {
        JDialog dialog = new JDialog(this, "编辑宠物", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("宠物名称: "));
        JTextField nameField = new JTextField(pet.getName());
        panel.add(nameField);
        
        panel.add(new JLabel("宠物品种: "));
        JTextField typeField = new JTextField(pet.getType());
        panel.add(typeField);
        
        panel.add(new JLabel("出生日期: "));
        JTextField birthDateField = new JTextField(pet.getBirthDate());
        panel.add(birthDateField);
        
        panel.add(new JLabel("性别: "));
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"公", "母"});
        genderComboBox.setSelectedItem(pet.getGender());
        panel.add(genderComboBox);
        
        panel.add(new JLabel("毛色: "));
        JTextField colorField = new JTextField(pet.getColor());
        panel.add(colorField);
        
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            String name = nameField.getText();
            String type = typeField.getText();
            String birthDate = birthDateField.getText();
            String gender = (String) genderComboBox.getSelectedItem();
            String color = colorField.getText();
            
            if (!name.isEmpty() && !type.isEmpty() && !birthDate.isEmpty()) {
                pet.setName(name);
                pet.setType(type);
                pet.setBirthDate(birthDate);
                pet.setGender(gender);
                pet.setColor(color);
                refreshPetProfilePanel();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请填写必要信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 创建健康提醒面板
    private JPanel createHealthReminderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建标题
        JLabel titleLabel = new JLabel("健康提醒");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 添加提醒按钮
        JButton addReminderButton = new JButton("添加提醒");
        addReminderButton.addActionListener(e -> showAddReminderDialog());
        titlePanel.add(addReminderButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建内容面板，使用垂直BoxLayout
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        
        // 创建疫苗接种专门栏目
        JPanel vaccinationPanel = createVaccinationPanel();
        contentContainer.add(vaccinationPanel);
        contentContainer.add(Box.createVerticalStrut(20)); // 添加间距
        
        // 创建表格模型，添加序号列
        String[] columnNames = {"序号", "宠物", "提醒类型", "内容", "日期", "状态", "操作"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; // 只有状态和操作列可编辑
            }
        };
        // 初始化序号计数器
        int rowCounter = 1;
        
        // 填充表格数据 - 只显示非疫苗接种类型的提醒
        for (HealthReminder reminder : healthReminders) {
            // 跳过疫苗接种类型的提醒，它们只在上面的专门栏目中显示
            if (reminder.getType().equals("疫苗接种")) {
                continue;
            }
            
            String petName = getPetNameById(reminder.getPetId());
            String status = reminder.getStatus();
            JButton editButton = new JButton("编辑");
            JButton deleteButton = new JButton("删除");
            
            editButton.addActionListener(e -> {
                int row = tableModel.getRowCount() - 1;
                while (row >= 0) {
                    if (tableModel.getValueAt(row, 5) == editButton) {
                        int reminderId = reminder.getId();
                        for (HealthReminder r : healthReminders) {
                            if (r.getId() == reminderId) {
                                showEditReminderDialog(r);
                                break;
                            }
                        }
                        break;
                    }
                    row--;
                }
            });
            
            deleteButton.addActionListener(e -> {
                int row = tableModel.getRowCount() - 1;
                while (row >= 0) {
                    if (tableModel.getValueAt(row, 5) == deleteButton) {
                        int reminderId = reminder.getId();
                        if (JOptionPane.showConfirmDialog(this, "确定要删除这条提醒吗？", "确认删除", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            for (int i = 0; i < healthReminders.size(); i++) {
                                if (healthReminders.get(i).getId() == reminderId) {
                                    healthReminders.remove(i);
                                    refreshHealthReminderPanel();
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    row--;
                }
            });
            
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            actionPanel.add(editButton);
            actionPanel.add(deleteButton);
            
            // 使用计数器生成序号，移除ID列
            tableModel.addRow(new Object[]{rowCounter++, petName, reminder.getType(), reminder.getContent(), reminder.getDate(), status, actionPanel});
        }
        
        // 创建表格
        JTable table = new JTable(tableModel);
        // 增大表格字体
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 增加行高
        table.setRowHeight(30);
        // 设置表头字体
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // 序号
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // 宠物
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // 提醒类型
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // 内容
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // 日期
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // 状态
        table.getColumnModel().getColumn(6).setPreferredWidth(150); // 操作
        
        table.getColumnModel().getColumn(6).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            if (value instanceof JPanel) {
                return (JPanel) value;
            }
            // 如果不是JPanel，返回一个空标签作为默认值
            return new JLabel(value != null ? value.toString() : "");
        });
        
        // 状态列渲染器和编辑器
        table.getColumnModel().getColumn(5).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel((String) value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (((String) value).equals("待完成")) {
                label.setForeground(Color.RED);
            } else if (((String) value).equals("已完成")) {
                label.setForeground(Color.GREEN);
            } else if (((String) value).equals("不需要")) {
                label.setForeground(Color.GRAY);
            }
            return label;
        });
        
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"待完成", "已完成", "不需要"})));
        
        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(table);
        contentContainer.add(scrollPane);
        
        // 将内容容器添加到主面板
        panel.add(contentContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 创建疫苗接种专门栏目
    private JPanel createVaccinationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("疫苗接种信息"));
        
        // 创建表格模型，添加序号列
        String[] columnNames = {"序号", "宠物", "疫苗类型", "第一针日期", "第二针日期", "第三针日期", "状态"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有状态列可编辑
            }
        };
        // 初始化序号计数器
        int rowCounter = 1;
        
        // 按宠物和疫苗类型分组收集疫苗接种信息
        Map<Integer, Map<String, List<HealthReminder>>> petVaccineGroups = new HashMap<>();
        
        for (HealthReminder reminder : healthReminders) {
            if (reminder.getType().equals("疫苗接种")) {
                int petId = reminder.getPetId();
                String content = reminder.getContent();
                
                // 确定疫苗类型
                String vaccineType = "其他疫苗";
                if (content.contains("狂犬疫苗")) vaccineType = "狂犬疫苗";
                else if (content.contains("猫三联")) vaccineType = "猫三联";
                else if (content.contains("犬六联")) vaccineType = "犬六联";
                else if (content.contains("猫瘟疫苗")) vaccineType = "猫瘟疫苗";
                
                // 按宠物和疫苗类型分组
                if (!petVaccineGroups.containsKey(petId)) {
                    petVaccineGroups.put(petId, new HashMap<>());
                }
                
                if (!petVaccineGroups.get(petId).containsKey(vaccineType)) {
                    petVaccineGroups.get(petId).put(vaccineType, new ArrayList<>());
                }
                
                petVaccineGroups.get(petId).get(vaccineType).add(reminder);
            }
        }
        
        // 填充疫苗接种表格数据
        for (Map.Entry<Integer, Map<String, List<HealthReminder>>> petEntry : petVaccineGroups.entrySet()) {
            int petId = petEntry.getKey();
            String petName = getPetNameById(petId);
            
            for (Map.Entry<String, List<HealthReminder>> vaccineEntry : petEntry.getValue().entrySet()) {
                String vaccineType = vaccineEntry.getKey();
                List<HealthReminder> vaccinations = vaccineEntry.getValue();
                
                // 初始化日期
                String firstDose = "-";
                String secondDose = "-";
                String thirdDose = "-";
                String status = "部分完成";
                
                // 根据内容分配日期
                for (HealthReminder vac : vaccinations) {
                    if (vac.getContent().contains("第一针")) {
                        firstDose = vac.getDate();
                    } else if (vac.getContent().contains("第二针")) {
                        secondDose = vac.getDate();
                    } else if (vac.getContent().contains("第三针")) {
                        thirdDose = vac.getDate();
                    } else if (vac.getContent().contains("加强针")) {
                        thirdDose = vac.getDate();
                    } else {
                        // 对于没有明确标注第几针的疫苗，默认作为第一针
                        if (firstDose.equals("-")) {
                            firstDose = vac.getDate();
                        }
                    }
                }
                
                // 确定整体状态
                boolean allCompleted = true;
                boolean allPending = true;
                for (HealthReminder vac : vaccinations) {
                    if (!vac.getStatus().equals("已完成")) {
                        allCompleted = false;
                    }
                    if (!vac.getStatus().equals("待完成")) {
                        allPending = false;
                    }
                }
                
                if (allCompleted) {
                    status = "全部完成";
                } else if (allPending) {
                    status = "全部待完成";
                } else {
                    status = "部分完成";
                }
                
                // 使用计数器生成序号
                tableModel.addRow(new Object[]{rowCounter++, petName, vaccineType, firstDose, secondDose, thirdDose, status});
            }
        }
        
        // 创建表格
        JTable table = new JTable(tableModel);
        // 设置表格属性
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // 序号
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // 宠物
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // 疫苗类型
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // 第一针日期
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // 第二针日期
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // 第三针日期
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // 状态
        
        // 状态列渲染器
        table.getColumnModel().getColumn(6).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel((String) value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (((String) value).equals("全部完成")) {
                label.setForeground(Color.GREEN);
            } else if (((String) value).equals("全部待完成")) {
                label.setForeground(Color.RED);
            } else {
                label.setForeground(Color.ORANGE);
            }
            return label;
        });
        
        // 状态列编辑器
        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"全部待完成", "部分完成", "全部完成"})));
        
        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 180));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 刷新健康提醒面板
    private void refreshHealthReminderPanel() {
        // 只重新创建和替换健康提醒面板，保留其他面板
        contentPanel.remove(1); // 移除健康提醒面板（索引为1）
        JPanel newHealthReminderPanel = createHealthReminderPanel();
        contentPanel.add(newHealthReminderPanel, "healthReminder", 1);
        
        // 显示刷新后的面板
        cardLayout.show(contentPanel, "healthReminder");
    }
    
    // 获取宠物的所有健康提醒
    private List<HealthReminder> getPetHealthReminders(int petId) {
        List<HealthReminder> petReminders = new ArrayList<>();
        for (HealthReminder reminder : healthReminders) {
            if (reminder.getPetId() == petId) {
                petReminders.add(reminder);
            }
        }
        return petReminders;
    }
    
    // 通过ID获取宠物名称
    private String getPetNameById(int petId) {
        for (Pet pet : pets) {
            if (pet.getId() == petId) {
                return pet.getName();
            }
        }
        return "未知宠物";
    }
    
    // 添加健康提醒对话框
    private void showAddReminderDialog() {
        JDialog dialog = new JDialog(this, "添加健康提醒", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("选择宠物: "));
        JComboBox<Pet> petComboBox = new JComboBox<>();
        for (Pet pet : pets) {
            petComboBox.addItem(pet);
        }
        panel.add(petComboBox);
        
        panel.add(new JLabel("提醒类型: "));
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"疫苗接种", "驱虫", "体检", "其他"});
        panel.add(typeComboBox);
        
        panel.add(new JLabel("提醒内容: "));
        JTextField contentField = new JTextField();
        panel.add(contentField);
        
        panel.add(new JLabel("提醒日期: "));
        JPanel datePanel = new JPanel(new BorderLayout());
        JTextField dateField = new JTextField();
        JButton datePickerButton = new JButton("选择日期");
        datePickerButton.addActionListener(e -> {
            String selectedDate = showDatePickerDialog(dateField.getText());
            if (selectedDate != null) {
                dateField.setText(selectedDate);
            }
        });
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(datePickerButton, BorderLayout.EAST);
        panel.add(datePanel);
        
        panel.add(new JLabel("状态: "));
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"待完成", "已完成", "不需要"});
        panel.add(statusComboBox);
        
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            Pet selectedPet = (Pet) petComboBox.getSelectedItem();
            String type = (String) typeComboBox.getSelectedItem();
            String content = contentField.getText();
            String date = dateField.getText();
            String status = (String) statusComboBox.getSelectedItem();
            
            if (selectedPet != null && !content.isEmpty() && !date.isEmpty()) {
                int id = healthReminders.size() > 0 ? healthReminders.get(healthReminders.size() - 1).getId() + 1 : 1;
                healthReminders.add(new HealthReminder(id, selectedPet.getId(), type, content, date, status));
                refreshHealthReminderPanel();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请填写必要信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 显示日期选择器对话框 - 改进为更直观的日历样式选择器
    private String showDatePickerDialog(String currentDate) {
        // 创建一个带有日历选择的对话框
        JDialog dateDialog = new JDialog(this, "选择日期", true);
        dateDialog.setSize(320, 300);
        dateDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 设置默认日期或使用当前日期
        Calendar calendar = Calendar.getInstance();
        if (currentDate != null && !currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("-");
                if (parts.length == 3) {
                    calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                }
            } catch (Exception e) {
                // 如果解析失败，使用当前日期
            }
        }

        // 使用可变的包装类来存储日期值
        final int[] yearValue = {calendar.get(Calendar.YEAR)};
        final int[] monthValue = {calendar.get(Calendar.MONTH)};
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 显示年份和月份选择
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton prevMonthButton = new JButton("<");
        JButton nextMonthButton = new JButton(">");
        JLabel monthYearLabel = new JLabel(String.format("%d年%d月", yearValue[0], monthValue[0] + 1));
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(prevMonthButton, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextMonthButton, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // 创建日历显示面板
        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        String[] dayNames = {"日", "一", "二", "三", "四", "五", "六"};
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName);
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            calendarPanel.add(dayLabel);
        }

        // 用于存储用户选择的日期
        final String[] selectedDate = {null};
        
        // 初始化日历显示
        updateCalendarDisplay(calendarPanel, calendar, day, selectedDate);

        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setPreferredSize(new Dimension(280, 180));
        panel.add(scrollPane, BorderLayout.CENTER);

        // 上个月按钮
        prevMonthButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            yearValue[0] = calendar.get(Calendar.YEAR);
            monthValue[0] = calendar.get(Calendar.MONTH);
            monthYearLabel.setText(String.format("%d年%d月", yearValue[0], monthValue[0] + 1));
            calendarPanel.removeAll();
            for (String dayName : dayNames) {
                JLabel dayLabel = new JLabel(dayName);
                dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
                calendarPanel.add(dayLabel);
            }
            updateCalendarDisplay(calendarPanel, calendar, 0, selectedDate);
            calendarPanel.revalidate();
            calendarPanel.repaint();
        });

        // 下个月按钮
        nextMonthButton.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            yearValue[0] = calendar.get(Calendar.YEAR);
            monthValue[0] = calendar.get(Calendar.MONTH);
            monthYearLabel.setText(String.format("%d年%d月", yearValue[0], monthValue[0] + 1));
            calendarPanel.removeAll();
            for (String dayName : dayNames) {
                JLabel dayLabel = new JLabel(dayName);
                dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
                calendarPanel.add(dayLabel);
            }
            updateCalendarDisplay(calendarPanel, calendar, 0, selectedDate);
            calendarPanel.revalidate();
            calendarPanel.repaint();
        });

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        okButton.addActionListener(e -> {
            if (selectedDate[0] == null) {
                // 如果没有选择日期，使用当前日期
                selectedDate[0] = String.format("%04d-%02d-%02d", yearValue[0], monthValue[0] + 1, day);
            }
            dateDialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            selectedDate[0] = null;
            dateDialog.dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dateDialog.add(panel);
        dateDialog.setVisible(true);

        return selectedDate[0];
    }
    
    // 更新日历显示
    private void updateCalendarDisplay(JPanel calendarPanel, Calendar calendar, int selectedDay, String[] selectedDate) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // 保存当前日期
        int todayYear = Calendar.getInstance().get(Calendar.YEAR);
        int todayMonth = Calendar.getInstance().get(Calendar.MONTH);
        int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        // 设置为当月的第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // 获取当月第一天是星期几
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0表示星期日

        // 获取当月的天数
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 添加上个月的空白
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // 添加当月的日期按钮
        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDayValue = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setHorizontalAlignment(SwingConstants.CENTER);
            
            // 如果是当天，高亮显示
            if (year == todayYear && month == todayMonth && day == todayDay) {
                dayButton.setBackground(Color.LIGHT_GRAY);
            }
            
            // 如果是默认选中的日期，设为选中状态
            if (day == selectedDay && selectedDay != 0) {
                dayButton.setBackground(Color.YELLOW);
                selectedDate[0] = String.format("%04d-%02d-%02d", year, month + 1, day);
            }
            
            // 添加日期选择监听器
            dayButton.addActionListener(e -> {
                // 清除其他按钮的选中状态
                for (Component component : calendarPanel.getComponents()) {
                    if (component instanceof JButton) {
                        ((JButton) component).setBackground(null);
                    }
                }
                
                // 设置当前按钮为选中状态
                dayButton.setBackground(Color.YELLOW);
                selectedDate[0] = String.format("%04d-%02d-%02d", year, month + 1, currentDayValue);
            });
            
            calendarPanel.add(dayButton);
        }
    }
    
    // 编辑健康提醒对话框
    private void showEditReminderDialog(HealthReminder reminder) {
        JDialog dialog = new JDialog(this, "编辑健康提醒", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("选择宠物: "));
        JComboBox<Pet> petComboBox = new JComboBox<>();
        for (Pet pet : pets) {
            petComboBox.addItem(pet);
            if (pet.getId() == reminder.getPetId()) {
                petComboBox.setSelectedItem(pet);
            }
        }
        panel.add(petComboBox);
        
        panel.add(new JLabel("提醒类型: "));
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"疫苗接种", "驱虫", "体检", "其他"});
        typeComboBox.setSelectedItem(reminder.getType());
        panel.add(typeComboBox);
        
        panel.add(new JLabel("提醒内容: "));
        JTextField contentField = new JTextField(reminder.getContent());
        panel.add(contentField);
        
        panel.add(new JLabel("提醒日期: "));
        JPanel datePanel = new JPanel(new BorderLayout());
        JTextField dateField = new JTextField(reminder.getDate());
        JButton datePickerButton = new JButton("选择日期");
        datePickerButton.addActionListener(e -> {
            String selectedDate = showDatePickerDialog(dateField.getText());
            if (selectedDate != null) {
                dateField.setText(selectedDate);
            }
        });
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(datePickerButton, BorderLayout.EAST);
        panel.add(datePanel);
        
        panel.add(new JLabel("状态: "));
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"待完成", "已完成", "不需要"});
        statusComboBox.setSelectedItem(reminder.getStatus());
        panel.add(statusComboBox);
        
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            Pet selectedPet = (Pet) petComboBox.getSelectedItem();
            String type = (String) typeComboBox.getSelectedItem();
            String content = contentField.getText();
            String date = dateField.getText();
            String status = (String) statusComboBox.getSelectedItem();
            
            if (selectedPet != null && !content.isEmpty() && !date.isEmpty()) {
                reminder.setPetId(selectedPet.getId());
                reminder.setType(type);
                reminder.setContent(content);
                reminder.setDate(date);
                reminder.setStatus(status);
                refreshHealthReminderPanel();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请填写必要信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 创建社交互动面板
    private JPanel createSocialPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建标题
        JLabel titleLabel = new JLabel("社交互动");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 添加发帖按钮
        JButton addPostButton = new JButton("发布动态");
        addPostButton.addActionListener(e -> showAddPostDialog());
        titlePanel.add(addPostButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建社交动态面板
        JPanel postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        
        // 添加每个动态
        for (SocialPost post : socialPosts) {
            postsPanel.add(createSocialPost(post));
            postsPanel.add(Box.createVerticalStrut(15));
        }
        
        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(postsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 创建单个社交动态
    private JPanel createSocialPost(SocialPost post) {
        JPanel postPanel = new JPanel();
        postPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        postPanel.setBackground(Color.WHITE);
        postPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
        postPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 用户信息和时间
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel usernameLabel = new JLabel(post.getUsername());
        usernameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        JLabel timeLabel = new JLabel(post.getTime());
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        timeLabel.setForeground(Color.GRAY);
        
        headerPanel.add(usernameLabel, BorderLayout.WEST);
        headerPanel.add(timeLabel, BorderLayout.EAST);
        
        // 动态内容
        JLabel contentLabel = new JLabel("<html>" + post.getContent() + "</html>");
        contentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        contentLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // 互动栏
        JPanel interactionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        
        JLabel likesLabel = new JLabel("点赞: " + post.getLikes());
        JButton likeButton = new JButton("👍");
        likeButton.setPreferredSize(new Dimension(30, 25));
        // 实现点赞功能限制 - 每个用户只能点赞一次
        likeButton.addActionListener(e -> {
            if (!post.isLiked()) {
                post.setLikes(post.getLikes() + 1);
                post.setLiked(true);
                likeButton.setEnabled(false); // 禁用点赞按钮
                likeButton.setBackground(Color.LIGHT_GRAY);
                likesLabel.setText("点赞: " + post.getLikes());
            }
        });
        
        JLabel commentsLabel = new JLabel("评论: " + post.getComments());
        JButton commentButton = new JButton("💬");
        commentButton.setPreferredSize(new Dimension(30, 25));
        
        // 评论展示面板
        JPanel commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setVisible(false); // 初始隐藏评论面板
        
        commentButton.addActionListener(e -> {
            String comment = JOptionPane.showInputDialog(this, "写下你的评论:", "添加评论", JOptionPane.PLAIN_MESSAGE);
            if (comment != null && !comment.trim().isEmpty()) {
                // 添加评论到列表
                post.addComment("我: " + comment);
                commentsLabel.setText("评论: " + post.getComments());
                
                // 显示评论面板
                commentsPanel.setVisible(true);
                
                // 更新评论面板
                updateCommentsPanel(commentsPanel, post);
                
                JOptionPane.showMessageDialog(this, "评论已添加！", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 如果没有输入评论，切换评论面板的显示状态
                // 切换评论面板的显示状态并更新评论内容
                commentsPanel.setVisible(!commentsPanel.isVisible());
                if (commentsPanel.isVisible()) {
                    updateCommentsPanel(commentsPanel, post);
                }
            }
        });
        
        interactionPanel.add(likeButton);
        interactionPanel.add(likesLabel);
        interactionPanel.add(commentButton);
        interactionPanel.add(commentsLabel);
        
        // 添加分割线
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        postPanel.add(headerPanel);
        postPanel.add(contentLabel);
        postPanel.add(interactionPanel);
        postPanel.add(separator);
        postPanel.add(commentsPanel);
        
        return postPanel;
    }
    
    // 更新评论面板
    private void updateCommentsPanel(JPanel commentsPanel, SocialPost post) {
        commentsPanel.removeAll();
        
        if (post.getComments() == 0) {
            JLabel noCommentsLabel = new JLabel("暂无评论");
            noCommentsLabel.setForeground(Color.GRAY);
            noCommentsLabel.setBorder(new EmptyBorder(10, 20, 10, 0));
            commentsPanel.add(noCommentsLabel);
        } else {
            // 添加所有评论
            for (String comment : post.getCommentList()) {
                JLabel commentLabel = new JLabel("<html><span style='color: #666;'>" + comment + "</span></html>");
                commentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                commentLabel.setBorder(new EmptyBorder(5, 20, 5, 0));
                commentsPanel.add(commentLabel);
            }
        }
        
        // 添加收起评论按钮
        JButton hideCommentsButton = new JButton("收起评论");
        hideCommentsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        hideCommentsButton.addActionListener(e -> commentsPanel.setVisible(false));
        hideCommentsButton.setPreferredSize(new Dimension(100, 25));
        hideCommentsButton.setMargin(new Insets(2, 5, 2, 5));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(hideCommentsButton);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        commentsPanel.add(buttonPanel);
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }
    
    // 发布动态对话框
    private void showAddPostDialog() {
        JDialog dialog = new JDialog(this, "发布动态", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel contentLabel = new JLabel("动态内容: ");
        JTextArea contentArea = new JTextArea(8, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("发布");
        JButton cancelButton = new JButton("取消");
        
        confirmButton.addActionListener(e -> {
            String content = contentArea.getText().trim();
            if (!content.isEmpty()) {
                int id = socialPosts.size() > 0 ? socialPosts.get(socialPosts.size() - 1).getId() + 1 : 1;
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                socialPosts.add(new SocialPost(id, "我", content, time, 0, 0));
                refreshSocialPanel();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "内容不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    // 刷新社交面板
    private void refreshSocialPanel() {
        // 只重新创建和替换社交互动面板，保留其他面板
        contentPanel.remove(2); // 移除社交互动面板（索引为2）
        JPanel newSocialPanel = createSocialPanel();
        contentPanel.add(newSocialPanel, "social", 2);
        
        // 显示刷新后的面板
        cardLayout.show(contentPanel, "social");
    }
    
    // 刷新服务预约面板
    private void refreshServicePanel() {
        // 只重新创建和替换服务预约面板，保留其他面板
        contentPanel.remove(3); // 移除服务预约面板（索引为3）
        JPanel newServicePanel = createServicePanel();
        contentPanel.add(newServicePanel, "service", 3);
        
        // 显示刷新后的面板
        cardLayout.show(contentPanel, "service");
    }
    
    // 创建服务预约面板
    private JPanel createServicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建顶部区域的容器面板，使用垂直BoxLayout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(true);
        topPanel.setBackground(Color.WHITE);
        
        // 创建标题
        JLabel titleLabel = new JLabel("服务预约");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(true);
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 添加刷新按钮
        JButton refreshButton = new JButton("刷新列表");
        refreshButton.addActionListener(e -> refreshServicePanel());
        titlePanel.add(refreshButton, BorderLayout.EAST);
        
        topPanel.add(titlePanel);
        
        // 添加宠物主人信息区域
        JPanel ownerInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ownerInfoPanel.setBorder(new EmptyBorder(5, 0, 10, 0));
        ownerInfoPanel.setOpaque(true);
        ownerInfoPanel.setBackground(Color.WHITE);
        
        // 假设当前登录用户有一个宠物，取第一个宠物的主人信息作为示例
        String ownerName = "未知主人";
        String ownerContact = "未设置联系方式";
        if (!pets.isEmpty()) {
            Pet firstPet = pets.get(0);
            ownerName = firstPet.getOwnerName();
            ownerContact = firstPet.getOwnerContact() != null ? firstPet.getOwnerContact() : "未设置联系方式";
        }
        
        JLabel ownerInfoLabel = new JLabel("当前用户: " + ownerName + " | 联系电话: " + ownerContact);
        ownerInfoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        ownerInfoLabel.setForeground(Color.GRAY);
        ownerInfoPanel.add(ownerInfoLabel);
        
        topPanel.add(ownerInfoPanel);
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        searchPanel.setOpaque(true);
        searchPanel.setBackground(Color.WHITE);
        
        JLabel searchLabel = new JLabel("搜索: ");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("搜索");
        
        // 添加服务类型过滤器
        JLabel typeFilterLabel = new JLabel("类型: ");
        JComboBox<String> typeFilterComboBox = new JComboBox<>(new String[]{"全部"});
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(typeFilterLabel);
        searchPanel.add(typeFilterComboBox);
        searchPanel.add(searchButton);
        
        topPanel.add(searchPanel);
        
        // 将顶部组合面板添加到主面板的NORTH位置
        panel.add(topPanel, BorderLayout.NORTH);
        
        // 创建服务提供商列表
        JPanel providersPanel = new JPanel();
        providersPanel.setLayout(new GridLayout(serviceProviders.size(), 1, 0, 15));
        providersPanel.setOpaque(true);
        providersPanel.setBackground(Color.WHITE);
        providersPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // 添加每个服务提供商卡片
        for (ServiceProvider provider : serviceProviders) {
            JPanel card = createServiceProviderCard(provider);
            providersPanel.add(card);
        }
        
        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(providersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 创建单个服务提供商卡片
    private JPanel createServiceProviderCard(ServiceProvider provider) {
        // 使用GridBagLayout确保所有组件都能正确显示
        JPanel cardPanel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        cardPanel.setLayout(layout);
        cardPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setOpaque(true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 服务商图片占位符
        JPanel imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(100, 120));
        imagePanel.setBackground(new Color(220, 220, 220));
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel providerTypeLabel = new JLabel(provider.getType());
        providerTypeLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        providerTypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        providerTypeLabel.setForeground(Color.BLACK);
        imagePanel.add(providerTypeLabel);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        cardPanel.add(imagePanel, gbc);
        
        // 服务商信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(true);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setPreferredSize(new Dimension(600, 120));
        
        // 名称标签
        JLabel nameLabel = new JLabel(provider.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setOpaque(true);
        nameLabel.setBackground(Color.WHITE);
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        
        // 地址标签
        JLabel addressLabel = new JLabel("地址: " + provider.getAddress());
        addressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        addressLabel.setForeground(Color.BLACK);
        addressLabel.setOpaque(true);
        addressLabel.setBackground(Color.WHITE);
        infoPanel.add(addressLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        
        // 电话标签
        JLabel phoneLabel = new JLabel("电话: " + provider.getPhone());
        phoneLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        phoneLabel.setForeground(Color.BLACK);
        phoneLabel.setOpaque(true);
        phoneLabel.setBackground(Color.WHITE);
        infoPanel.add(phoneLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        
        // 营业时间标签
        JLabel hoursLabel = new JLabel("营业时间: " + provider.getHours());
        hoursLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        hoursLabel.setForeground(Color.BLACK);
        hoursLabel.setOpaque(true);
        hoursLabel.setBackground(Color.WHITE);
        infoPanel.add(hoursLabel);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        cardPanel.add(infoPanel, gbc);
        
        // 预约按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setPreferredSize(new Dimension(100, 120));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        JButton bookButton = new JButton("预约");
        bookButton.setPreferredSize(new Dimension(80, 35));
        bookButton.setMaximumSize(new Dimension(80, 35));
        bookButton.setMinimumSize(new Dimension(80, 35));
        bookButton.addActionListener(e -> showBookServiceDialog(provider));
        
        JPanel centerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerButtonPanel.setOpaque(true);
        centerButtonPanel.setBackground(Color.WHITE);
        centerButtonPanel.add(bookButton);
        
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(centerButtonPanel);
        buttonPanel.add(Box.createVerticalGlue());
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        cardPanel.add(buttonPanel, gbc);
        
        // 鼠标悬停效果
        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cardPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
                cardPanel.setBackground(new Color(245, 245, 255));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                cardPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                cardPanel.setBackground(Color.WHITE);
            }
        });
        
        return cardPanel;
    }
    
    // 创建推送提醒面板
    private JPanel createPushReminderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // 创建标题
        JLabel titleLabel = new JLabel("推送提醒");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // 添加设置按钮
        JButton settingsButton = new JButton("提醒设置");
        settingsButton.addActionListener(e -> showReminderSettingsDialog());
        titlePanel.add(settingsButton, BorderLayout.EAST);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建表格模型
        String[] columnNames = {"宠物", "提醒类型", "内容", "日期", "状态", "推送状态"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // 只有状态列可编辑
            }
        };
        
        // 获取今天的日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        
        // 找出今天需要提醒的项目
        for (HealthReminder reminder : healthReminders) {
            if (reminder.getDate().equals(today)) {
                String petName = getPetNameById(reminder.getPetId());
                String pushStatus = reminder.isPushed() ? "已推送" : "未推送";
                tableModel.addRow(new Object[]{petName, reminder.getType(), reminder.getContent(), reminder.getDate(), reminder.getStatus(), pushStatus});
            }
        }
        
        // 如果今天没有提醒，显示提示信息
        if (tableModel.getRowCount() == 0) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JLabel emptyLabel = new JLabel("今天没有需要提醒的事项");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            emptyPanel.add(emptyLabel);
            panel.add(emptyPanel, BorderLayout.CENTER);
            return panel;
        }
        
        // 创建表格
        JTable table = new JTable(tableModel);
        // 设置表格属性
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // 宠物
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // 提醒类型
        table.getColumnModel().getColumn(2).setPreferredWidth(250); // 内容
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // 日期
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // 推送状态
        
        // 状态列渲染器
        table.getColumnModel().getColumn(4).setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel((String) value);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            if (((String) value).equals("待完成")) {
                label.setForeground(Color.RED);
            } else if (((String) value).equals("已完成")) {
                label.setForeground(Color.GREEN);
            } else if (((String) value).equals("不需要")) {
                label.setForeground(Color.GRAY);
            }
            return label;
        });
        
        // 状态列编辑器
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"待完成", "已完成", "不需要"})));
        
        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    // 显示提醒设置对话框
    private void showReminderSettingsDialog() {
        JDialog dialog = new JDialog(this, "提醒设置", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("每日提醒时间: "));
        JComboBox<String> timeComboBox = new JComboBox<>();
        // 添加时间段选项
        for (int hour = 8; hour <= 20; hour++) {
            timeComboBox.addItem(String.format("%02d:00", hour));
            timeComboBox.addItem(String.format("%02d:30", hour));
        }
        timeComboBox.setSelectedItem("09:00");
        panel.add(timeComboBox);
        
        panel.add(new JLabel("提醒方式: "));
        JComboBox<String> methodComboBox = new JComboBox<>(new String[]{"弹窗提醒", "桌面通知", "声音提醒"});
        panel.add(methodComboBox);
        
        panel.add(new JLabel("提前提醒天数: "));
        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 7, 1));
        panel.add(daysSpinner);
        
        panel.add(new JLabel(""));
        JCheckBox enableCheckBox = new JCheckBox("启用提醒功能", true);
        panel.add(enableCheckBox);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存设置");
        JButton cancelButton = new JButton("取消");
        
        saveButton.addActionListener(e -> {
            String time = (String) timeComboBox.getSelectedItem();
            String method = (String) methodComboBox.getSelectedItem();
            int days = (int) daysSpinner.getValue();
            boolean enabled = enableCheckBox.isSelected();
            
            // 这里可以保存设置到配置文件
            JOptionPane.showMessageDialog(dialog, "提醒设置已保存！", "成功", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 初始化每日提醒检查
    private void initializeDailyReminderCheck() {
        // 设置定时器，每天检查一次
        Timer timer = new Timer(86400000, e -> checkAndPushDailyReminders()); // 24小时
        timer.setInitialDelay(getTimeUntilNextCheck()); // 计算到下一次检查的延迟时间
        timer.start();
        
        // 立即检查一次
        checkAndPushDailyReminders();
    }
    
    // 计算到下一次检查的延迟时间（今天的9:00）
    private int getTimeUntilNextCheck() {
        Calendar now = Calendar.getInstance();
        Calendar nextCheck = Calendar.getInstance();
        nextCheck.set(Calendar.HOUR_OF_DAY, 9);
        nextCheck.set(Calendar.MINUTE, 0);
        nextCheck.set(Calendar.SECOND, 0);
        
        if (now.after(nextCheck)) {
            nextCheck.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return (int) (nextCheck.getTimeInMillis() - now.getTimeInMillis());
    }
    
    // 检查并推送每日提醒 - 已禁用初始提醒对话框
    private void checkAndPushDailyReminders() {
        // 方法保留但不显示任何对话框，以避免程序启动时显示疫苗提醒
        // 所有提醒信息将通过主界面的推送提醒面板显示
        return;
    }
    
    // 计算未来日期（在给定日期基础上添加指定天数）
    private String calculateFutureDate(String baseDate, int daysToAdd) {
        try {
            String[] parts = baseDate.split("-");
            if (parts.length == 3) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
                return String.format("%04d-%02d-%02d", calendar.get(Calendar.YEAR), 
                                    calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            }
        } catch (Exception e) {
            // 解析失败，返回原日期
        }
        return baseDate;
    }
    
    // 预约服务对话框
    private void showBookServiceDialog(ServiceProvider provider) {
        JDialog dialog = new JDialog(this, "预约服务 - " + provider.getName(), true);
        dialog.setSize(400, 400); // 增加高度以适应新增的组件
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("服务商名称: "));
        JLabel nameLabel = new JLabel(provider.getName());
        panel.add(nameLabel);

        panel.add(new JLabel("服务商类型: "));
        JLabel typeLabel = new JLabel(provider.getType());
        panel.add(typeLabel);
        
        panel.add(new JLabel("地址: "));
        JLabel addressLabel = new JLabel(provider.getAddress());
        panel.add(addressLabel);
        
        panel.add(new JLabel("电话: "));
        JLabel phoneLabel = new JLabel(provider.getPhone());
        panel.add(phoneLabel);
        
        panel.add(new JLabel("营业时间: "));
        JLabel hoursLabel = new JLabel(provider.getHours());
        panel.add(hoursLabel);

        panel.add(new JLabel("选择宠物: "));
        JComboBox<Pet> petComboBox = new JComboBox<>();
        for (Pet pet : pets) {
            petComboBox.addItem(pet);
        }
        panel.add(petComboBox);

        panel.add(new JLabel("预约日期: "));
        JPanel datePanel = new JPanel(new BorderLayout());
        JTextField dateField = new JTextField();
        JButton datePickerButton = new JButton("选择日期");
        datePickerButton.addActionListener(e -> {
            String selectedDate = showDatePickerDialog(dateField.getText());
            if (selectedDate != null) {
                dateField.setText(selectedDate);
            }
        });
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(datePickerButton, BorderLayout.EAST);
        panel.add(datePanel);

        panel.add(new JLabel("预约时间: "));
        JComboBox<String> timeComboBox = new JComboBox<>();
        
        // 填充每半小时一个的时间选项，从00:00到23:30
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                String time = String.format("%02d:%02d", hour, minute);
                timeComboBox.addItem(time);
            }
        }
        
        panel.add(timeComboBox);

        panel.add(new JLabel("预约服务: "));
        JComboBox<String> serviceComboBox = new JComboBox<>(new String[]{"疫苗接种", "驱虫", "体检", "美容", "寄养", "其他"});
        panel.add(serviceComboBox);

        panel.add(new JLabel("备注: "));
        JTextField notesField = new JTextField();
        panel.add(notesField);

        // 疫苗多针提醒选项
        JCheckBox multiVaccineCheckBox = new JCheckBox("添加多针疫苗提醒");
        multiVaccineCheckBox.setEnabled(serviceComboBox.getSelectedItem().equals("疫苗接种"));
        
        // 当服务类型改变时，更新复选框状态
        serviceComboBox.addActionListener(e -> {
            multiVaccineCheckBox.setEnabled(serviceComboBox.getSelectedItem().equals("疫苗接种"));
            if (!serviceComboBox.getSelectedItem().equals("疫苗接种")) {
                multiVaccineCheckBox.setSelected(false);
            }
        });
        
        panel.add(new JLabel()); // 空白标签，保持布局
        panel.add(multiVaccineCheckBox);

        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确认预约");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            Pet selectedPet = (Pet) petComboBox.getSelectedItem();
            String date = dateField.getText();
            String time = timeComboBox.getSelectedItem().toString();
            String service = (String) serviceComboBox.getSelectedItem();
            String notes = notesField.getText();
            boolean addMultiVaccine = multiVaccineCheckBox.isSelected();

            if (selectedPet != null && !date.isEmpty() && !time.isEmpty()) {
                // 这里可以添加预约记录到系统
                String appointmentDetails = "预约成功！\n" +
                                           "服务商: " + provider.getName() + " (" + provider.getType() + ")\n" +
                                           "宠物: " + selectedPet.getName() + " (" + selectedPet.getType() + ")\n" +
                                           "日期: " + date + " " + time + "\n" +
                                           "服务类型: " + service;
                if (!notes.isEmpty()) {
                    appointmentDetails += "\n备注: " + notes;
                }
                
                JOptionPane.showMessageDialog(dialog, appointmentDetails, "预约成功", JOptionPane.INFORMATION_MESSAGE);
                
                // 添加到健康提醒 - 为所有服务类型添加提醒
                int id = healthReminders.size() > 0 ? healthReminders.get(healthReminders.size() - 1).getId() + 1 : 1;
                String reminderContent = service;
                if (!notes.isEmpty()) {
                    reminderContent += " - " + notes;
                }
                
                if (service.equals("疫苗接种")) {
                    // 疫苗接种特殊处理，添加详细信息
                    reminderContent = "第一针疫苗接种";
                    if (!notes.isEmpty()) {
                        reminderContent += " - " + notes;
                    }
                    healthReminders.add(new HealthReminder(id, selectedPet.getId(), "疫苗接种", reminderContent, date, "待完成", false));
                          
                    if (addMultiVaccine) {
                        // 添加第二针提醒（间隔21天）
                        String secondDoseDate = calculateFutureDate(date, 21);
                        id++;
                        healthReminders.add(new HealthReminder(id, selectedPet.getId(), "疫苗接种", "第二针疫苗接种", secondDoseDate, "待完成", false));
                     
                        // 添加第三针提醒（再间隔21天）
                        String thirdDoseDate = calculateFutureDate(secondDoseDate, 21);
                        id++;
                        healthReminders.add(new HealthReminder(id, selectedPet.getId(), "疫苗接种", "第三针疫苗接种", thirdDoseDate, "待完成", false));
                        
                        JOptionPane.showMessageDialog(dialog, "已为宠物添加3针疫苗接种提醒！\n第一针: " + date + "\n第二针: " + secondDoseDate + "\n第三针: " + thirdDoseDate, "提示", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "已为宠物添加健康提醒！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    // 为其他所有服务类型添加提醒
                    healthReminders.add(new HealthReminder(id, selectedPet.getId(), service, reminderContent, date, "待完成", false));
                    JOptionPane.showMessageDialog(dialog, "已为宠物添加健康提醒！", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                
                // 刷新健康提醒面板，让新添加的提醒立即显示
                refreshHealthReminderPanel();
                
                // 刷新健康提醒面板，让新添加的提醒立即显示
                refreshHealthReminderPanel();
                
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "请填写必要信息", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // 自定义圆角边框类
    class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;
        private int thickness;
        
        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - thickness, height - thickness, radius, radius);
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius/2;
            return insets;
        }
    }
    
    // 自定义阴影边框类
    class ShadowBorder extends AbstractBorder {
        private int top, left, bottom, right;
        private Color shadowColor;
        
        public ShadowBorder(int top, int left, int bottom, int right, Color shadowColor) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.shadowColor = shadowColor;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // 绘制阴影
            g.setColor(shadowColor);
            g.fillRect(x + left, y + height - bottom, width - left - right, bottom);
            g.fillRect(x + width - right, y + top, right, height - top - bottom);
            
            // 绘制阴影的圆角效果（简化版）
            if (right > 0 && bottom > 0) {
                g.fillOval(x + width - right, y + height - bottom, right, bottom);
            }
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(top, left, bottom, right);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = left;
            insets.top = top;
            insets.right = right;
            insets.bottom = bottom;
            return insets;
        }
    }
    
    // 数据类定义
    class Pet {
        private int id;
        private String name;
        private String type;
        private String birthDate;
        private String gender;
        private String color;
        private String ownerName; // 宠物主人名字
        private String ownerContact; // 宠物主人联系方式
        
        public Pet(int id, String name, String type, String birthDate, String gender, String color) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.birthDate = birthDate;
            this.gender = gender;
            this.color = color;
            this.ownerName = "未知主人"; // 默认值
            this.ownerContact = null;
        }
        
        public Pet(int id, String name, String type, String birthDate, String gender, String color, String ownerName) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.birthDate = birthDate;
            this.gender = gender;
            this.color = color;
            this.ownerName = ownerName;
            this.ownerContact = null;
        }
        
        public Pet(int id, String name, String type, String birthDate, String gender, String color, String ownerName, String ownerContact) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.birthDate = birthDate;
            this.gender = gender;
            this.color = color;
            this.ownerName = ownerName;
            this.ownerContact = ownerContact;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getBirthDate() { return birthDate; }
        public String getGender() { return gender; }
        public String getColor() { return color; }
        public String getOwnerName() { return ownerName; }        
        public String getOwnerContact() { return ownerContact; }
        
        // 计算宠物年龄
        public int getAge() {
            try {
                // 解析出生日期
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date birth = sdf.parse(birthDate);
                Date now = new Date();
                
                // 计算年龄差
                Calendar calBirth = Calendar.getInstance();
                Calendar calNow = Calendar.getInstance();
                calBirth.setTime(birth);
                calNow.setTime(now);
                
                int age = calNow.get(Calendar.YEAR) - calBirth.get(Calendar.YEAR);
                
                // 检查是否还没过生日
                if (calNow.get(Calendar.DAY_OF_YEAR) < calBirth.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                
                return Math.max(0, age); // 确保年龄不为负数
            } catch (Exception e) {
                return 0; // 解析失败时返回0
            }
        }
        
        public void setName(String name) { this.name = name; }
        public void setType(String type) { this.type = type; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
        public void setGender(String gender) { this.gender = gender; }
        public void setColor(String color) { this.color = color; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
        public void setOwnerContact(String ownerContact) { this.ownerContact = ownerContact; }
        
        @Override
        public String toString() { return name + " (" + type + ")"; }
    }
    
    class HealthReminder {
        private int id;
        private int petId;
        private String type;
        private String content;
        private String date;
        private String status;
        private boolean pushed; // 是否已推送提醒给主人
        
        public HealthReminder(int id, int petId, String type, String content, String date, String status) {
            this.id = id;
            this.petId = petId;
            this.type = type;
            this.content = content;
            this.date = date;
            this.status = status;
            this.pushed = false; // 默认未推送
        }
        
        public HealthReminder(int id, int petId, String type, String content, String date, String status, boolean pushed) {
            this.id = id;
            this.petId = petId;
            this.type = type;
            this.content = content;
            this.date = date;
            this.status = status;
            this.pushed = pushed;
        }
        
        public int getId() { return id; }
        public int getPetId() { return petId; }
        public String getType() { return type; }
        public String getContent() { return content; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public boolean isPushed() { return pushed; }
        
        public void setPetId(int petId) { this.petId = petId; }
        public void setType(String type) { this.type = type; }
        public void setContent(String content) { this.content = content; }
        public void setDate(String date) { this.date = date; }
        public void setStatus(String status) { this.status = status; }
        public void setPushed(boolean pushed) { this.pushed = pushed; }
    }
    
    class SocialPost {
        private int id;
        private String username;
        private String content;
        private String time;
        private int likes;
        private int comments;
        private boolean liked; // 是否已点赞
        private List<String> commentList; // 评论列表
        
        public SocialPost(int id, String username, String content, String time, int likes, int comments) {
            this.id = id;
            this.username = username;
            this.content = content;
            this.time = time;
            this.likes = likes;
            this.comments = comments;
            this.liked = false;
            this.commentList = new ArrayList<>();
        }
        
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getContent() { return content; }
        public String getTime() { return time; }
        public int getLikes() { return likes; }
        public int getComments() { return comments; }
        public boolean isLiked() { return liked; }
        public List<String> getCommentList() { return commentList; }
        
        public void setLikes(int likes) { this.likes = likes; }
        public void setComments(int comments) { this.comments = comments; }
        public void setLiked(boolean liked) { this.liked = liked; }
        
        // 添加评论
        public void addComment(String comment) {
            this.commentList.add(comment);
            this.comments++;
        }
    }
    
    class ServiceProvider {
        private int id;
        private String name;
        private String type;
        private String address;
        private String phone;
        private String hours;
        
        public ServiceProvider(int id, String name, String type, String address, String phone, String hours) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.address = address;
            this.phone = phone;
            this.hours = hours;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getHours() { return hours; }
    }
    
    public static void main(String[] args) {
        // 在事件调度线程中启动应用程序
        SwingUtilities.invokeLater(() -> {
            PetHealthSocialApp app = new PetHealthSocialApp();
            app.setVisible(true);
        });
    }
}