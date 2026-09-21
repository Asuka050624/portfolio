// 初始化模拟数据
function initMockData() {
    // 检查是否已经初始化过数据
    if (localStorage.getItem('initData') === 'true') {
        return;
    }
    
    // 模拟用户数据（已登录状态）
    const mockUser = {
        username: '测试用户',
        isLogin: true,
        timestamp: new Date().getTime(),
        avatar: 'images/user.png'
    };
    
    // 模拟预约数据
    const mockReservations = [
        {
            id: 'RES1634567890123',
            username: '测试用户',
            petName: '小白',
            petType: 'dog',
            symptoms: '最近食欲不佳，精神状态较差',
            doctor: '1',
            doctorName: '张医生 - 宠物内科',
            date: new Date().toISOString().split('T')[0],
            time: '10:00',
            contactName: '李先生',
            contactPhone: '13800138000',
            status: 'confirmed',
            createTime: new Date(Date.now() - 86400000).toISOString()
        },
        {
            id: 'RES1634567890456',
            username: '测试用户',
            petName: '咪咪',
            petType: 'cat',
            symptoms: '眼睛有些红肿，经常流泪',
            doctor: '4',
            doctorName: '赵医生 - 宠物眼科',
            date: new Date(Date.now() + 86400000).toISOString().split('T')[0],
            time: '14:00',
            contactName: '王先生',
            contactPhone: '13900139000',
            status: 'pending',
            createTime: new Date(Date.now() - 3600000).toISOString()
        }
    ];
    
    // 模拟反馈数据
    const mockFeedbacks = [
        {
            id: 'FB1634567890123',
            username: '测试用户',
            type: 'suggestion',
            typeText: '建议',
            content: '希望能够增加更多的宠物食品选择，特别是有机天然的食品，这样对宠物的健康更有好处。',
            contact: 'test@example.com',
            status: 'replied',
            submitTime: new Date(Date.now() - 172800000).toLocaleString('zh-CN'),
            reply: '感谢您的建议！我们正在积极寻找更多优质的宠物食品供应商，相信很快就会有更多选择。'
        },
        {
            id: 'FB1634567890456',
            username: '测试用户',
            type: 'bug',
            typeText: '问题反馈',
            content: '在预约页面，选择日期时有时候会出现日历显示不正确的问题，希望能够修复。',
            contact: '13700137000',
            status: 'processing',
            submitTime: new Date(Date.now() - 86400000).toLocaleString('zh-CN'),
            reply: ''
        }
    ];
    
    // 模拟购物车数据
    const mockCart = [
        {
            id: 1,
            name: "优质宠物罐头",
            price: 29.90,
            originalPrice: 39.90,
            category: "食品",
            image: "images/product1.jpg",
            sales: 128,
            stock: 500,
            description: "采用优质食材制作，富含宠物所需的各种营养成分，口感鲜美，易于消化吸收。",
            quantity: 2
        },
        {
            id: 4,
            name: "宠物沐浴露",
            price: 39.90,
            originalPrice: 49.90,
            category: "洗护",
            image: "images/product4.jpg",
            sales: 156,
            stock: 400,
            description: "温和不刺激的宠物专用沐浴露，能够有效清洁宠物毛发，保持毛发柔顺光泽，不损伤皮肤。",
            quantity: 1
        }
    ];
    
    // 保存数据到本地存储
    localStorage.setItem('user', JSON.stringify(mockUser));
    localStorage.setItem('reservations', JSON.stringify(mockReservations));
    localStorage.setItem('feedbacks', JSON.stringify(mockFeedbacks));
    localStorage.setItem('cart', JSON.stringify(mockCart));
    
    // 标记已初始化
    localStorage.setItem('initData', 'true');
}

// 页面加载时执行初始化
$(document).ready(function() {
    // 仅在用户中心、预约和反馈页面执行初始化
    const currentPage = window.location.pathname.split('/').pop();
    if (['user_center.html', 'reservation.html', 'feedback.html', 'shop.html'].includes(currentPage)) {
        initMockData();
    }
});