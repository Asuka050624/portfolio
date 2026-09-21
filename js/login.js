// 登录脚本 - 增加大小写不敏感和更好的用户体验
console.log('登录脚本开始执行');

// 等待DOM完全加载
window.onload = function() {
    console.log('DOM已完全加载');
    
    // 元素检查
    const form = document.getElementById('loginForm');
    const backBtn = document.getElementById('back-btn');
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const usernameInput = document.getElementById('username');
    const loginBtn = document.querySelector('.login-btn');
    
    console.log('元素检查:', {
        form: !!form,
        backBtn: !!backBtn,
        togglePassword: !!togglePassword,
        passwordInput: !!passwordInput,
        usernameInput: !!usernameInput,
        loginBtn: !!loginBtn
    });
    
    // 返回按钮功能
    if (backBtn) {
        backBtn.onclick = function() {
            console.log('返回按钮点击');
            history.back();
        };
    }
    
    // 密码显示切换
    if (togglePassword && passwordInput) {
        togglePassword.onclick = function() {
            console.log('切换密码显示');
            passwordInput.type = passwordInput.type === 'password' ? 'text' : 'password';
        };
    }
    
    // 核心登录功能 - 同时绑定按钮点击和表单提交
    if (loginBtn) {
        loginBtn.onclick = function() {
            handleLogin();
        };
    }
    
    if (form) {
        form.onsubmit = function(e) {
            e.preventDefault();
            console.log('表单提交');
            handleLogin();
        };
    }
    
    // 回车键支持
    if (passwordInput) {
        passwordInput.onkeypress = function(e) {
            if (e.key === 'Enter') {
                console.log('回车键按下');
                handleLogin();
            }
        };
    }
};

// 登录处理函数
function handleLogin() {
    console.log('开始登录处理');
    
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    
    console.log('输入值:', {username, password: '******'});
    
    if (!username || !password) {
        console.log('验证失败: 空输入');
        alert('请输入用户名和密码');
        return;
    }
    
    // 修改为大小写不敏感，提高用户体验
    if (username.toLowerCase() === 'user' && password === '123456') {
        console.log('验证成功');
        
        // 保存登录状态 - 使用统一的user对象格式，与评论系统保持一致
        try {
            const user = {
                username: username,
                userId: 'user1',
                isLogin: true
            };
            localStorage.setItem('user', JSON.stringify(user));
            console.log('登录状态已保存');
        } catch (e) {
            console.log('localStorage错误:', e);
        }
        
        // 登录成功提示
        alert('登录成功！');
        
        // 跳转回保存的页面或来源页或用户中心
        console.log('准备跳转...');
        setTimeout(function() {
            // 优先检查localStorage中保存的跳转URL
            const redirectUrl = localStorage.getItem('redirectUrl');
            if (redirectUrl) {
                console.log('跳转到保存的页面:', redirectUrl);
                localStorage.removeItem('redirectUrl'); // 清除保存的URL
                window.location.href = redirectUrl;
            } else if (document.referrer) {
                console.log('返回来源页面:', document.referrer);
                window.location.href = document.referrer;
            } else {
                console.log('跳转到用户中心');
                window.location.href = 'user_center.html';
            }
        }, 100);
    } else {
        console.log('验证失败: 账号密码错误');
        alert('用户名或密码错误，请尝试：\n用户名: user\n密码: 123456');
    }
}