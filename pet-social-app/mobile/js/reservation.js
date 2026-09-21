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
    
    // 日期选择器默认值设置为明天
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const defaultDate = tomorrow.toISOString().split('T')[0];
    $('#date').val(defaultDate);
    
    // 监听宠物类型选择，当选择'其他'时显示具体宠物类型输入框
    $('#petType').change(function() {
        const petType = $(this).val();
        if (petType === 'other') {
            $('#otherPetTypeContainer').show();
        } else {
            $('#otherPetTypeContainer').hide();
        }
    });
    
    // 预约表单提交
    $('#reservationForm').submit(function(e) {
        e.preventDefault();
        
        // 获取表单数据
        const formData = {
            petName: $('#petName').val(),
            petType: $('#petType').val(),
            otherPetType: $('#petType').val() === 'other' ? $('#otherPetType').val() : null,
            symptoms: $('#symptoms').val(),
            doctor: $('#doctor').val(),
            date: $('#date').val(),
            time: $('#time').val(),
            contactName: $('#contactName').val(),
            contactPhone: $('#contactPhone').val()
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
        
        // 模拟提交预约请求
        submitReservation(formData);
    });
    
    // 加载我的预约列表
    loadMyReservations();
});

// 表单验证
function validateForm(formData) {
    // 空数据验证
    if (!formData.petName) {
        alert('请输入宠物名称');
        return false;
    }
    
    if (!formData.petType) {
        alert('请选择宠物类型');
        return false;
    }
    
    // 如果选择了'其他'宠物类型，必须填写具体的宠物类型
    if (formData.petType === 'other' && (!formData.otherPetType || formData.otherPetType.trim() === '')) {
        alert('请输入具体的宠物类型');
        return false;
    }
    
    if (!formData.symptoms) {
        alert('请描述宠物症状或就诊需求');
        return false;
    }
    
    if (!formData.doctor) {
        alert('请选择预约医生');
        return false;
    }
    
    if (!formData.date) {
        alert('请选择预约日期');
        return false;
    }
    
    if (!formData.time) {
        alert('请选择预约时间');
        return false;
    }
    
    if (!formData.contactName) {
        alert('请输入联系人姓名');
        return false;
    }
    
    if (!formData.contactPhone) {
        alert('请输入联系电话');
        return false;
    }
    
    // 联系电话格式验证
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(formData.contactPhone)) {
        alert('请输入正确的手机号码');
        return false;
    }
    
    // 检查预约日期是否有效（不能是过去的日期）
    const selectedDate = new Date(formData.date);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (selectedDate < today) {
        alert('预约日期不能是过去的日期');
        return false;
    }
    
    return true;
}

// 模拟提交预约请求
function submitReservation(formData) {
    console.log('提交预约请求:', formData);
    
    // 生成预约ID
    const reservationId = 'RES' + Date.now();
    
    // 获取当前登录用户
    const user = JSON.parse(localStorage.getItem('user'));
    const username = user.username || '用户';
    
    // 构建完整的预约信息
    const reservationInfo = {
        id: reservationId,
        username: username,
        petName: formData.petName,
        petType: formData.petType,
        symptoms: formData.symptoms,
        doctor: formData.doctor,
        doctorName: getDoctorName(formData.doctor),
        date: formData.date,
        time: formData.time,
        contactName: formData.contactName,
        contactPhone: formData.contactPhone,
        status: 'pending', // 待确认状态
        createTime: new Date().toISOString()
    };
    
    // 保存预约信息到本地存储
    saveReservationToLocalStorage(reservationInfo);
    
    // 显示成功提示
    $('#successModal').show();
    
    // 清空表单
    $('#reservationForm')[0].reset();
    
    // 重新设置日期默认值
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const defaultDate = tomorrow.toISOString().split('T')[0];
    $('#date').val(defaultDate);
    
    // 更新预约列表
    loadMyReservations();
}

// 获取医生名称
function getDoctorName(doctorId) {
    const doctors = {
        '1': '张医生 - 宠物内科',
        '2': '李医生 - 宠物外科',
        '3': '王医生 - 宠物皮肤科',
        '4': '赵医生 - 宠物眼科'
    };
    
    return doctors[doctorId] || doctorId;
}

// 保存预约信息到本地存储
function saveReservationToLocalStorage(reservationInfo) {
    // 获取现有的预约列表
    let reservations = JSON.parse(localStorage.getItem('reservations')) || [];
    
    // 添加新的预约
    reservations.push(reservationInfo);
    
    // 保存回本地存储
    localStorage.setItem('reservations', JSON.stringify(reservations));
}

// 加载我的预约列表
function loadMyReservations() {
    // 检查用户是否登录
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user || !user.isLogin) {
        $('#reservationList').html('<p class="no-reservations">请先登录查看您的预约记录</p>');
        return;
    }
    
    const username = user.username || '用户';
    
    // 获取本地存储的预约列表
    const reservations = JSON.parse(localStorage.getItem('reservations')) || [];
    
    // 筛选当前用户的预约
    const userReservations = reservations.filter(res => res.username === username);
    
    const reservationList = $('#reservationList');
    reservationList.empty();
    
    if (userReservations.length === 0) {
        reservationList.html('<p class="no-reservations">暂无预约记录</p>');
    } else {
        // 按日期倒序排序
        userReservations.sort((a, b) => new Date(b.date) - new Date(a.date));
        
        // 添加预约项
        userReservations.forEach(function(reservation) {
            const statusText = getStatusText(reservation.status);
            const statusClass = getStatusClass(reservation.status);
            
            const reservationItem = `
                <div class="reservation-item">
                    <div class="reservation-header">
                        <span class="reservation-id">预约ID: ${reservation.id}</span>
                        <span class="reservation-status ${statusClass}">${statusText}</span>
                    </div>
                    <div class="reservation-content">
                        <div class="reservation-info">
                            <p><strong>宠物名称:</strong> ${reservation.petName}</p>
                            <p><strong>宠物类型:</strong> ${getPetTypeText(reservation.petType)}</p>
                            <p><strong>预约医生:</strong> ${reservation.doctorName}</p>
                            <p><strong>预约时间:</strong> ${reservation.date} ${reservation.time}</p>
                            <p><strong>症状描述:</strong> ${reservation.symptoms}</p>
                            <p><strong>联系人:</strong> ${reservation.contactName}</p>
                            <p><strong>联系电话:</strong> ${reservation.contactPhone}</p>
                        </div>
                        ${reservation.status === 'pending' ? `<div class="reservation-actions"><button class="cancel-reservation" data-id="${reservation.id}">取消预约</button></div>` : ''}
                    </div>
                </div>
            `;
            
            reservationList.append(reservationItem);
        });
        
        // 绑定取消预约按钮事件
        $('.cancel-reservation').on('click', function() {
            const reservationId = $(this).data('id');
            if (confirm('确定要取消此预约吗？')) {
                cancelReservation(reservationId);
            }
        });
    }
}

// 取消预约
function cancelReservation(reservationId) {
    // 获取本地存储的预约列表
    let reservations = JSON.parse(localStorage.getItem('reservations')) || [];
    
    // 查找并更新预约状态
    for (let i = 0; i < reservations.length; i++) {
        if (reservations[i].id === reservationId) {
            reservations[i].status = 'canceled';
            break;
        }
    }
    
    // 保存回本地存储
    localStorage.setItem('reservations', JSON.stringify(reservations));
    
    // 重新加载预约列表
    loadMyReservations();
    
    // 显示提示
    alert('预约已取消');
}

// 获取状态文本
function getStatusText(status) {
    const statusMap = {
        'pending': '待确认',
        'confirmed': '已确认',
        'completed': '已完成',
        'canceled': '已取消'
    };
    
    return statusMap[status] || status;
}

// 获取状态样式类
function getStatusClass(status) {
    const statusMap = {
        'pending': 'status-pending',
        'confirmed': 'status-confirmed',
        'completed': 'status-completed',
        'canceled': 'status-canceled'
    };
    
    return statusMap[status] || '';
}

// 获取宠物类型文本
function getPetTypeText(petType) {
    const petTypeMap = {
        'dog': '狗狗',
        'cat': '猫咪',
        'bird': '鸟类',
        'fish': '鱼类',
        'other': '其他'
    };
    
    return petTypeMap[petType] || petType;
}