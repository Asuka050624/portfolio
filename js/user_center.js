$(document).ready(function() {
    // 获取DOM元素
    const backBtn = document.getElementById('backBtn');
    const homeBtn = document.getElementById('homeBtn');
    const myOrders = document.getElementById('myOrders');
    const myReservations = document.getElementById('myReservations');
    const myFeedbacks = document.getElementById('myFeedbacks');
    const settings = document.getElementById('settings');
    const logoutBtn = document.getElementById('logoutBtn');
    const successModal = document.getElementById('successModal');
    const successMessage = document.getElementById('successMessage');
    const closeSuccessModal = document.getElementById('closeSuccessModal');
    const currentTime = document.getElementById('currentTime');

    // 显示当前时间
    function showCurrentTime() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hour = String(now.getHours()).padStart(2, '0');
        const minute = String(now.getMinutes()).padStart(2, '0');
        currentTime.textContent = `${year}-${month}-${day} ${hour}:${minute}`;
    }
    showCurrentTime();
    setInterval(showCurrentTime, 60000);

    // 检查用户登录状态
    function checkUserLogin() {
        const isLoggedIn = localStorage.getItem('isLoggedIn');
        const username = localStorage.getItem('username');
        
        // 兼容旧版本存储方式
        const userObject = localStorage.getItem('user');
        
        if (isLoggedIn === 'true' && username) {
            return { isLoggedIn: true, username: username, email: username + '@example.com' };
        } else if (userObject) {
            try {
                const user = JSON.parse(userObject);
                return { isLoggedIn: true, username: user.username || '用户', email: user.email || '--' };
            } catch (e) {
                console.error('解析用户数据失败:', e);
                return { isLoggedIn: false };
            }
        }
        
        return { isLoggedIn: false };
    }

    // 显示用户信息
    function displayUserInfo(user) {
        const userNameEl = document.getElementById('userName');
        const userEmailEl = document.getElementById('userEmail');
        
        if (userNameEl && userEmailEl) {
            userNameEl.textContent = user.username || '未知用户';
            userEmailEl.textContent = user.email || '--';
        }
    }

    // 退出登录
    function logout() {
        // 清除登录状态
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('username');
        localStorage.removeItem('user'); // 同时清除旧版本存储
        
        // 显示成功提示
        showSuccessMessage('退出登录成功');
    }

    // 显示成功提示
    function showSuccessMessage(message) {
        if (successMessage && successModal) {
            successMessage.textContent = message;
            successModal.style.display = 'flex';
        }
    }

    // 检查并显示用户信息
    const userInfo = checkUserLogin();
    if (userInfo.isLoggedIn) {
        displayUserInfo(userInfo);
    } else {
        // 如果未登录，不自动重定向到登录页面
        // 改为显示未登录状态
        displayUserInfo({username: '未登录', email: '--'});
    }

    // 返回按钮点击事件
    if (backBtn) {
        backBtn.addEventListener('click', function() {
            window.history.back();
        });
    }

    // 首页按钮点击事件
    if (homeBtn) {
        homeBtn.addEventListener('click', function() {
            window.location.href = 'index.html';
        });
    }

    // 我的订单点击事件
    if (myOrders) {
        myOrders.addEventListener('click', function() {
            showSuccessMessage('功能开发中');
        });
    }

    // 我的预约点击事件
    if (myReservations) {
        myReservations.addEventListener('click', function() {
            showSuccessMessage('功能开发中');
        });
    }

    // 我的反馈点击事件
    if (myFeedbacks) {
        myFeedbacks.addEventListener('click', function() {
            showSuccessMessage('功能开发中');
        });
    }

    // 个人设置点击事件
    if (settings) {
        settings.addEventListener('click', function() {
            showSuccessMessage('功能开发中');
        });
    }

    // 退出登录点击事件
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }

    // 关闭成功提示弹窗
    if (closeSuccessModal) {
        closeSuccessModal.addEventListener('click', function() {
            if (successModal) {
                successModal.style.display = 'none';
            }
            
            // 检查用户是否已退出登录（用户对象已被清除）
            const currentUserInfo = checkUserLogin();
            if (!currentUserInfo.isLoggedIn) {
                // 在关闭弹窗后刷新页面，显示未登录状态
                location.reload();
            }
        });
    }
});