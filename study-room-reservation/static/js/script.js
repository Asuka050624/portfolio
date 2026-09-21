// 等待页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 1. 获取弹窗、协议链接、关闭按钮
    const modal = document.getElementById('agreementModal');
    const agreementLink = document.querySelector('.agreement-link');
    const closeBtn = document.querySelector('.close-btn');

    // 2. 点击“用户协议”→ 显示弹窗
    if (agreementLink) {
        agreementLink.addEventListener('click', function() {
            modal.style.display = 'flex';
        });
    }

    // 3. 点击关闭按钮→ 隐藏弹窗
    if (closeBtn) {
        closeBtn.addEventListener('click', function() {
            modal.style.display = 'none';
        });
    }

    // 4. 点击弹窗外部→ 隐藏弹窗
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});
// 轮播Banner逻辑（和你截图一致的自动滚动）
document.addEventListener('DOMContentLoaded', function() {
    const carouselItems = document.querySelectorAll('.carousel-item');
    const indicators = document.querySelectorAll('.indicator');
    let currentIndex = 0;
    const intervalTime = 5000; // 5秒自动切换一次

    // 切换Banner的函数
    function switchCarousel(index) {
        // 隐藏所有Banner，移除活跃状态
        carouselItems.forEach(item => item.classList.remove('active'));
        indicators.forEach(ind => ind.classList.remove('active'));
        // 显示当前Banner，添加活跃状态
        carouselItems[index].classList.add('active');
        indicators[index].classList.add('active');
        currentIndex = index;
    }

    // 自动轮播
    let carouselInterval = setInterval(function() {
        currentIndex = (currentIndex + 1) % carouselItems.length;
        switchCarousel(currentIndex);
    }, intervalTime);

    // 点击圆点指示器切换Banner
    indicators.forEach(indicator => {
        indicator.addEventListener('click', function() {
            clearInterval(carouselInterval); // 清除自动轮播
            const index = parseInt(this.getAttribute('data-index'));
            switchCarousel(index);
            // 重新启动自动轮播
            carouselInterval = setInterval(function() {
                currentIndex = (currentIndex + 1) % carouselItems.length;
                switchCarousel(currentIndex);
            }, intervalTime);
        });
    });
});