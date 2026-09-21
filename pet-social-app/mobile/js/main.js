document.addEventListener('DOMContentLoaded', function() {
    // 轮播图初始化 - 平滑滚动模式
    const carouselItems = document.querySelectorAll('.carousel-item');
    const indicators = document.querySelectorAll('.indicator');
    const carouselInner = document.querySelector('.carousel-inner');
    let currentSlide = 0;
    
    function showSlide(index) {
        console.log('切换到轮播图索引:', index);
        
        // 移除所有active类
        indicators.forEach(indicator => {
            indicator.classList.remove('active');
        });
        
        // 添加当前active类
        indicators[index].classList.add('active');
        
        // 使用transform实现平滑滚动
        const slideWidth = -100 * index / carouselItems.length;
        carouselInner.style.transform = `translateX(${slideWidth}%)`;
        
        currentSlide = index;
    }
    
    // 初始化显示第一张图片
    // 确保所有图片都可见，因为我们使用的是平移而不是显示/隐藏
    carouselItems.forEach(item => {
        item.style.display = 'block';
    });
    showSlide(0);
    
    // 为指示器添加点击事件，确保事件正确绑定
    indicators.forEach((indicator, index) => {
        indicator.style.cursor = 'pointer'; // 确保鼠标指针显示为手型
        indicator.addEventListener('click', function() {
            console.log('点击了指示器:', index);
            showSlide(index);
        });
    });
    
    // 自动轮播
    setInterval(() => {
        let nextSlide = (currentSlide + 1) % carouselItems.length;
        showSlide(nextSlide);
    }, 3000);
    
    // 更新当前时间 - 添加秒数显示
    function updateCurrentTime() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const seconds = String(now.getSeconds()).padStart(2, '0');
        const timeString = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        const timeElement = document.getElementById('currentTime');
        if (timeElement) {
            timeElement.textContent = timeString;
        }
    }
    
    updateCurrentTime();
    // 由于现在显示秒数，需要每秒更新一次
    setInterval(updateCurrentTime, 1000);
    
    // 用户头像点击事件 - 确保点击后跳转到登录页面
    const userAvatar = document.getElementById('userAvatar');
    if (userAvatar) {
        userAvatar.addEventListener('click', function() {
            console.log('头像被点击，跳转到登录页面');
            // 保存当前页面URL，以便登录成功后返回
            localStorage.setItem('redirectUrl', window.location.href);
            // 移除任何可能阻止跳转的事件
            window.onbeforeunload = null;
            // 直接跳转到登录页面
            window.location.href = 'login.html';
        });
    }
    
    // 功能模块点击事件 - 为四个主要功能按钮添加页面跳转
    const newsModule = document.getElementById('newsModule');
    if (newsModule) {
        newsModule.addEventListener('click', function() {
            console.log('宠物资讯按钮被点击');
            window.onbeforeunload = null;
            window.location.href = 'news.html';
        });
    }
    
    const reservationModule = document.getElementById('reservationModule');
    if (reservationModule) {
        reservationModule.addEventListener('click', function() {
            console.log('挂号预约按钮被点击');
            window.onbeforeunload = null;
            window.location.href = 'reservation.html';
        });
    }
    
    const shopModule = document.getElementById('shopModule');
    if (shopModule) {
        shopModule.addEventListener('click', function() {
            console.log('宠物商城按钮被点击');
            window.onbeforeunload = null;
            window.location.href = 'shop.html';
        });
    }
    
    const feedbackModule = document.getElementById('feedbackModule');
    if (feedbackModule) {
        feedbackModule.addEventListener('click', function() {
            console.log('反馈中心按钮被点击');
            window.onbeforeunload = null;
            window.location.href = 'feedback.html';
        });
    }
    
    // 商品详情查看功能
    const productItems = document.querySelectorAll('.product-item');
    productItems.forEach(item => {
        item.addEventListener('click', function() {
            const productId = this.dataset.id;
            const productDetail = document.getElementById('product-detail');
            if (productDetail) {
                productDetail.style.display = 'flex';
            }
        });
    });
    
    // 关闭商品详情
    const closeDetail = document.querySelector('.close-detail');
    if (closeDetail) {
        closeDetail.addEventListener('click', function() {
            const productDetail = document.getElementById('product-detail');
            if (productDetail) {
                productDetail.style.display = 'none';
            }
        });
    }
});