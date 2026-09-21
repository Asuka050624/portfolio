// 页面加载完成后执行
$(document).ready(function() {
    // 返回按钮点击事件
    $('#backBtn').click(function() {
        window.history.back();
    });
    
    // 关闭错误弹窗
    $('.close-modal, .confirm-btn').click(function() {
        $('#errorModal').hide();
    });
    
    // 注册表单提交
    $('#registerForm').submit(function(e) {
        e.preventDefault();
        
        // 获取表单数据
        const username = $('#regUsername').val();
        const password = $('#regPassword').val();
        const confirmPassword = $('#regConfirmPassword').val();
        const phone = $('#regPhone').val();
        const email = $('#regEmail').val();
        
        // 表单验证
        if (!validateForm(username, password, confirmPassword, phone)) {
            return;
        }
        
        // 模拟注册请求
        console.log('注册请求:', { username, password, phone, email });
        
        // 模拟密码加密
        const encryptedPassword = encryptPassword(password);
        
        // 模拟注册成功
        alert('注册成功，请登录');
        window.location.href = 'index.html';
    });
});

// 表单验证
function validateForm(username, password, confirmPassword, phone) {
    // 空数据验证
    if (!username) {
        showError('请输入用户名');
        return false;
    }
    
    if (!password) {
        showError('请输入密码');
        return false;
    }
    
    if (!confirmPassword) {
        showError('请确认密码');
        return false;
    }
    
    if (!phone) {
        showError('请输入手机号码');
        return false;
    }
    
    // 密码一致性验证
    if (password !== confirmPassword) {
        showError('两次输入的密码不一致');
        return false;
    }
    
    // 用户名长度验证
    if (username.length < 3 || username.length > 20) {
        showError('用户名长度应在3-20个字符之间');
        return false;
    }
    
    // 密码强度验证
    if (password.length < 6) {
        showError('密码长度至少为6个字符');
        return false;
    }
    
    // 手机号码格式验证
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(phone)) {
        showError('请输入正确的手机号码');
        return false;
    }
    
    // 模拟检查用户名唯一性
    if (checkUsernameExists(username)) {
        showError('用户名已存在，请选择其他用户名');
        return false;
    }
    
    return true;
}

// 显示错误信息
function showError(message) {
    $('#errorMessage').text(message);
    $('#errorModal').show();
}

// 模拟检查用户名是否存在
function checkUsernameExists(username) {
    // 这里应该是异步请求后端检查，这里简单模拟
    const existingUsernames = ['admin', 'user1', 'test'];
    return existingUsernames.includes(username);
}

// 模拟密码加密函数
function encryptPassword(password) {
    // 这里应该使用更安全的加密方式，这里简单模拟
    console.log('对密码进行加密处理');
    // 实际项目中应该使用如bcrypt、SHA256等加密算法
    return password; // 仅作示例，实际应返回加密后的密码
}