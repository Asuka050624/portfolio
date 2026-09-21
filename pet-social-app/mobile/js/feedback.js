// 页面加载完成后执行
$(document).ready(function() {
    // 返回按钮点击事件
    $('#backBtn').click(function() {
        window.history.back();
    });
    
    // 关闭成功提示弹窗
    $('.close-modal, .confirm-btn').click(function() {
        $('#successModal').hide();
    });
    
    // 反馈表单提交
    $('#feedbackForm').submit(function(e) {
        e.preventDefault();
        
        // 获取表单数据
        const formData = {
            type: $('#feedbackType').val(),
            content: $('#feedbackContent').val(),
            contact: $('#contactInfo').val()
        };
        
        // 表单验证
        if (!validateForm(formData)) {
            return;
        }
        
        // 检查用户是否登录
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user || !user.isLogin) {
        alert('请先登录');
        window.location.href = 'login.html';
        return;
    }
        
        // 提交反馈
        submitFeedback(formData);
    });
    
    // 加载我的反馈列表
    loadMyFeedbacks();
});

// 表单验证
function validateForm(formData) {
    // 检查反馈类型
    if (!formData.type) {
        alert('请选择反馈类型');
        return false;
    }
    
    // 检查反馈内容
    if (!formData.content.trim()) {
        alert('请输入反馈内容');
        return false;
    }
    
    // 反馈内容长度限制
    if (formData.content.length < 10) {
        alert('反馈内容至少需要10个字符');
        return false;
    }
    
    // 联系方式格式验证（如果填写了）
    if (formData.contact.trim()) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const phoneRegex = /^1[3-9]\d{9}$/;
        
        if (!emailRegex.test(formData.contact) && !phoneRegex.test(formData.contact)) {
            alert('请输入正确的邮箱或手机号码');
            return false;
        }
    }
    
    return true;
}

// 提交反馈
function submitFeedback(formData) {
    // 生成反馈ID
    const feedbackId = 'FB' + Date.now();
    
    // 获取当前登录用户
    const user = JSON.parse(localStorage.getItem('user'));
    const username = user.username || '用户';
    
    // 构建完整的反馈信息
    const feedbackInfo = {
        id: feedbackId,
        username: username,
        type: formData.type,
        typeText: getTypeText(formData.type),
        content: formData.content,
        contact: formData.contact,
        status: 'pending', // 待处理状态
        submitTime: new Date().toLocaleString('zh-CN'),
        reply: '' // 初始无回复
    };
    
    // 保存反馈信息到本地存储
    saveFeedbackToLocalStorage(feedbackInfo);
    
    // 显示成功提示
    $('#successModal').show();
    
    // 清空表单
    $('#feedbackForm')[0].reset();
    
    // 更新反馈列表
    loadMyFeedbacks();
}

// 获取反馈类型文本
function getTypeText(type) {
    const typeMap = {
        'suggestion': '建议',
        'bug': '问题反馈',
        'compliment': '表扬',
        'other': '其他'
    };
    
    return typeMap[type] || type;
}

// 保存反馈信息到本地存储
function saveFeedbackToLocalStorage(feedbackInfo) {
    // 获取现有的反馈列表
    let feedbacks = JSON.parse(localStorage.getItem('feedbacks')) || [];
    
    // 添加新的反馈
    feedbacks.push(feedbackInfo);
    
    // 保存回本地存储
    localStorage.setItem('feedbacks', JSON.stringify(feedbacks));
}

// 加载我的反馈列表
function loadMyFeedbacks() {
    // 检查用户是否登录
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user || !user.isLogin) {
        $('#feedbackList').html('<p class="no-feedbacks">请先登录查看您的反馈记录</p>');
        return;
    }
    
    const username = user.username || '用户';
    
    // 获取本地存储的反馈列表
    const feedbacks = JSON.parse(localStorage.getItem('feedbacks')) || [];
    
    // 筛选当前用户的反馈
    const userFeedbacks = feedbacks.filter(fb => fb.username === username);
    
    const feedbackList = $('#feedbackList');
    feedbackList.empty();
    
    if (userFeedbacks.length === 0) {
        feedbackList.html('<p class="no-feedbacks">暂无反馈记录</p>');
    } else {
        // 按提交时间倒序排序
        userFeedbacks.sort((a, b) => new Date(b.submitTime) - new Date(a.submitTime));
        
        // 添加反馈项
        userFeedbacks.forEach(function(feedback) {
            const statusText = getStatusText(feedback.status);
            const statusClass = getStatusClass(feedback.status);
            
            const feedbackItem = `
                <div class="feedback-item">
                    <div class="feedback-header">
                        <span class="feedback-type">${feedback.typeText}</span>
                        <span class="feedback-status ${statusClass}">${statusText}</span>
                    </div>
                    <div class="feedback-content">
                        <p>${feedback.content}</p>
                        ${feedback.contact ? `<p><strong>联系方式:</strong> ${feedback.contact}</p>` : ''}
                        ${feedback.reply ? `
                            <div class="feedback-reply">
                                <p><strong>管理员回复:</strong></p>
                                <p>${feedback.reply}</p>
                            </div>
                        ` : ''}
                    </div>
                    <div class="feedback-footer">
                        <span class="submit-time">提交时间: ${feedback.submitTime}</span>
                    </div>
                </div>
            `;
            
            feedbackList.append(feedbackItem);
        });
    }
}

// 获取状态文本
function getStatusText(status) {
    const statusMap = {
        'pending': '待处理',
        'processing': '处理中',
        'replied': '已回复',
        'closed': '已关闭'
    };
    
    return statusMap[status] || status;
}

// 获取状态样式类
function getStatusClass(status) {
    const statusMap = {
        'pending': 'status-pending',
        'processing': 'status-processing',
        'replied': 'status-replied',
        'closed': 'status-closed'
    };
    
    return statusMap[status] || '';
}