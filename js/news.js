// 页面加载完成后执行
$(document).ready(function() {
    // 返回按钮点击事件
    $('#backBtn').click(function() {
        window.location.href = 'index.html';
    });
    
    // 关闭资讯详情弹窗
    $('.close-modal').click(function() {
        $('#newsDetailModal').hide();
    });
    
    // 搜索功能
    $('#searchBtn').click(function() {
        searchNews();
    });
    
    $('#searchInput').keypress(function(e) {
        if (e.which === 13) { // 回车键
            searchNews();
        }
    });
    
    // 分类切换
    $('.category-tabs .tab').click(function() {
        // 更新激活状态
        $('.category-tabs .tab').removeClass('active');
        $(this).addClass('active');
        
        // 筛选资讯
        const category = $(this).data('category');
        filterNewsByCategory(category);
    });
    
    // 点击资讯项查看详情
    $('.news-item').click(function() {
        const newsId = $(this).attr('data-id');
        if (newsId) {
            showNewsDetail(newsId);
        }
    });
    
    // 提交评论
    $('#submitComment').click(function() {
        submitComment();
    });
});

// 搜索资讯
function searchNews() {
    const keyword = $('#searchInput').val().toLowerCase();
    
    if (!keyword.trim()) {
        // 如果搜索框为空，显示所有资讯
        $('.news-item').show();
        return;
    }
    
    // 根据关键词筛选资讯
    $('.news-item').each(function() {
        const title = $(this).find('h3').text().toLowerCase();
        const content = $(this).find('p').text().toLowerCase();
        
        if (title.includes(keyword) || content.includes(keyword)) {
            $(this).show();
        } else {
            $(this).hide();
        }
    });
}

// 根据分类筛选资讯
function filterNewsByCategory(category) {
    if (category === 'all') {
        // 显示所有资讯
        $('.news-item').show();
    } else {
        // 根据分类筛选
        $('.news-item').each(function() {
            const itemCategory = $(this).data('category');
            if (itemCategory === category) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    }
}

// 显示资讯详情
function showNewsDetail(newsId) {
    // 从模态框显示改为页面跳转
    window.location.href = `news_detail_${newsId}.html`;
}

// 模拟获取详细内容
function getDetailedContent(newsId) {
    const contents = {
        1: `
            <p>春季是万物复苏的季节，但同时也是宠物疾病高发的时期。随着气温的回升和湿度的增加，各种病原体也开始活跃起来。以下是春季常见的宠物疾病及预防措施：</p>
            <h4>1. 寄生虫感染</h4>
            <p>春季是寄生虫繁殖的高峰期，跳蚤、虱子、螨虫等体外寄生虫以及蛔虫、钩虫等体内寄生虫都会对宠物的健康造成威胁。</p>
            <p><strong>预防措施：</strong>定期为宠物进行驱虫，保持宠物生活环境的清洁卫生，避免宠物接触可能被寄生虫污染的环境。</p>
            <h4>2. 呼吸道疾病</h4>
            <p>春季气温变化较大，忽冷忽热的天气容易导致宠物免疫力下降，引发呼吸道疾病。</p>
            <p><strong>预防措施：</strong>注意给宠物保暖，避免忽冷忽热，保持室内空气流通，定期带宠物进行体检。</p>
            <h4>3. 过敏反应</h4>
            <p>春季花粉、尘螨等过敏原增多，容易引发宠物的过敏反应，表现为皮肤瘙痒、红肿、打喷嚏等症状。</p>
            <p><strong>预防措施：</strong>减少宠物接触过敏原的机会，保持宠物皮肤清洁，必要时咨询兽医进行脱敏治疗。</p>
        `,
        2: `
            <p>作为新手狗主人，掌握一些基础的训练技巧对于建立良好的主宠关系至关重要。以下是几个基础的训练技巧：</p>
            <h4>1. 坐下训练</h4>
            <p>这是最基础的训练之一。准备一些狗狗喜欢的零食，站在狗狗面前，手持零食放在狗狗的头顶上方，然后慢慢向后移动。当狗狗的头部抬起时，它的臀部自然会下降，这时发出"坐下"的指令。一旦狗狗坐下，立即给予零食奖励和口头表扬。</p>
            <h4>2. 握手训练</h4>
            <p>在狗狗已经学会坐下的基础上，可以进行握手训练。先让狗狗坐下，然后用一只手轻轻抬起它的前爪，同时发出"握手"的指令。当狗狗配合完成动作后，给予奖励。</p>
            <h4>3. 定点排便训练</h4>
            <p>这是非常重要的家居训练。观察狗狗的排便规律，通常在饭后、睡醒后会有便意。当发现狗狗有排便迹象时，立即将它带到指定的排便地点。如果狗狗在正确的地点排便，要及时给予奖励。</p>
        `,
        3: `
            <p>猫咪在不同的生长阶段有着不同的营养需求，了解这些需求有助于为您的猫咪提供健康、均衡的饮食。</p>
            <h4>1. 幼猫期（0-6个月）</h4>
            <p>幼猫期是猫咪生长发育最快的阶段，需要大量的蛋白质、脂肪、维生素和矿物质。建议选择专门为幼猫设计的猫粮，其营养密度更高，更容易消化吸收。</p>
            <h4>2. 成猫期（7个月-7岁）</h4>
            <p>成猫的营养需求相对稳定，但仍需要注意保持均衡的饮食。选择适合成猫的猫粮，控制热量摄入，避免猫咪过度肥胖。</p>
            <h4>3. 老年猫期（7岁以上）</h4>
            <p>老年猫的新陈代谢减慢，活动量减少，需要调整饮食结构。选择专门为老年猫设计的猫粮，其蛋白质含量适当降低，添加了更多的关节保护成分和抗氧化物质。</p>
        `,
        4: `
            <p>随着人们生活水平的提高和观念的转变，宠物已经成为很多家庭的重要成员。宠物行业也在迎来新的发展机遇和挑战。</p>
            <h4>1. 宠物医疗服务升级</h4>
            <p>随着宠物医疗技术的进步和人们对宠物健康的重视，宠物医疗服务正在向专业化、精细化方向发展。宠物医院的设备和技术不断更新，提供的服务也更加多样化。</p>
            <h4>2. 宠物食品高端化</h4>
            <p>消费者对宠物食品的要求越来越高，有机、天然、无添加的高端宠物食品受到青睐。同时，针对不同品种、不同年龄段、不同健康状况的宠物食品也越来越细分。</p>
            <h4>3. 宠物智能产品普及</h4>
            <p>随着智能科技的发展，各种宠物智能产品如智能喂食器、智能项圈、宠物监控摄像头等逐渐普及，为宠物主人提供了更多便利。</p>
        `,
        5: `
            <p>很多宠物主人往往忽视了宠物的口腔健康，但实际上，口腔问题可能会导致更严重的健康问题。</p>
            <h4>1. 宠物口腔问题的危害</h4>
            <p>口腔问题如龋齿、牙周炎等不仅会导致宠物疼痛、进食困难，还可能引发全身性疾病，如心脏、肾脏等器官的病变。</p>
            <h4>2. 如何保持宠物口腔健康</h4>
            <ul>
                <li>定期刷牙：使用宠物专用的牙刷和牙膏，每周至少为宠物刷牙2-3次。</li>
                <li>提供洁牙玩具和零食：一些特殊设计的玩具和零食可以帮助清洁宠物的牙齿。</li>
                <li>定期检查：每年带宠物到兽医处进行口腔检查，及时发现和治疗口腔问题。</li>
            </ul>
            <p>保持宠物的口腔健康需要主人的持续关注和护理。从小培养宠物接受口腔护理的习惯，可以让这个过程更加顺利。</p>
        `
    };
    
    return contents[newsId] || '<p>详细内容加载中...</p>';
}

// 加载评论
function loadComments(newsId) {
    const commentsList = $('#commentsList');
    commentsList.empty();
    
    // 模拟加载评论数据
    const comments = getComments(newsId);
    
    if (comments.length === 0) {
        commentsList.append('<p class="no-comments">暂无评论，快来发表第一条评论吧！</p>');
    } else {
        comments.forEach(function(comment) {
            const commentItem = `
                <div class="comment-item">
                    <div class="comment-author">${comment.author}</div>
                    <div class="comment-time">${comment.time}</div>
                    <div class="comment-content">${comment.content}</div>
                </div>
            `;
            commentsList.append(commentItem);
        });
    }
}

// 模拟获取评论数据
function getComments(newsId) {
    // 这里可以根据newsId返回不同的评论数据
    const allComments = {
        1: [
            {
                author: '宠物爱好者',
                time: '2023-03-16',
                content: '非常实用的文章，我家狗狗去年春天就得了皮肤病，今年一定要提前预防。'
            },
            {
                author: '铲屎官一枚',
                time: '2023-03-17',
                content: '请问体外驱虫和体内驱虫可以同时进行吗？'
            }
        ],
        2: [
            {
                author: '新手狗主人',
                time: '2023-03-11',
                content: '谢谢分享，正好我刚养了一只小狗，这些技巧很有用！'
            }
        ],
        3: [],
        4: [],
        5: []
    };
    
    return allComments[newsId] || [];
}

// 提交评论
function submitComment() {
    const commentText = $('#commentText').val().trim();
    
    if (!commentText) {
        alert('请输入评论内容');
        return;
    }
    
    // 检查用户是否登录
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user || !user.isLogin) {
        alert('请先登录');
        window.location.href = 'login.html';
        return;
    }
    
    const username = user.username || '用户';
    const now = new Date();
    const time = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0');
    
    // 创建新评论
    const newComment = {
        author: username,
        time: time,
        content: commentText
    };
    
    // 添加到评论列表
    const commentsList = $('#commentsList');
    const commentItem = `
        <div class="comment-item">
            <div class="comment-author">${newComment.author}</div>
            <div class="comment-time">${newComment.time}</div>
            <div class="comment-content">${newComment.content}</div>
        </div>
    `;
    
    // 如果是第一条评论，移除"暂无评论"提示
    if (commentsList.find('.no-comments').length > 0) {
        commentsList.empty();
    }
    
    commentsList.append(commentItem);
    
    // 清空输入框
    $('#commentText').val('');
    
    // 模拟保存评论到本地存储
    saveComment(newComment);
    
    alert('评论提交成功');
}

// 模拟保存评论
function saveComment(comment) {
    // 实际项目中应该发送到服务器保存
    console.log('保存评论:', comment);
    // 这里可以添加保存到localStorage的逻辑
}