// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 模拟商品数据
    const products = [
        { id: 1, name: "优质宠物罐头", price: 29.90, originalPrice: 39.90, category: "食品", image: "images/product1.jpg", sales: 128, stock: 500, description: "采用优质食材制作，富含宠物所需的各种营养成分，口感鲜美，易于消化吸收。" },
        { id: 2, name: "宠物互动玩具球", price: 19.90, originalPrice: 29.90, category: "玩具", image: "images/product2.jpg", sales: 95, stock: 300, description: "互动玩具球，能够发出声音吸引宠物注意力，增强主人与宠物的互动，提高宠物活跃度。" },
        { id: 3, name: "宠物秋冬保暖衣服", price: 59.90, originalPrice: 89.90, category: "服饰", image: "images/product3.jpg", sales: 78, stock: 200, description: "专为秋冬季节设计的宠物保暖衣服，材质柔软舒适，保暖效果好，让您的宠物温暖过冬。" },
        { id: 4, name: "宠物沐浴露", price: 39.90, originalPrice: 49.90, category: "洗护", image: "images/product4.jpg", sales: 156, stock: 400, description: "温和不刺激的宠物专用沐浴露，能够有效清洁宠物毛发，保持毛发柔顺光泽，不损伤皮肤。" },
        { id: 5, name: "猫粮幼猫专用", price: 89.90, originalPrice: 109.90, category: "食品", image: "images/product5.jpg", sales: 112, stock: 350, description: "专门为幼猫设计的配方粮，富含幼猫成长所需的营养成分，促进健康成长。" },
        { id: 6, name: "猫咪逗猫棒", price: 15.90, originalPrice: 25.90, category: "玩具", image: "images/product6.jpg", sales: 210, stock: 500, description: "有趣的逗猫棒，能够激发猫咪的狩猎本能，提供丰富的互动乐趣。" }
    ];

    // 初始化搜索按钮交互
    function initSearchToggle() {
        const searchToggleBtn = document.getElementById('searchToggleBtn');
        const searchContainer = document.getElementById('searchContainer');
        const searchInput = document.getElementById('searchInput');

        if (searchToggleBtn && searchContainer) {
            searchToggleBtn.addEventListener('click', function() {
                searchContainer.classList.toggle('show');
                if (searchContainer.classList.contains('show')) {
                    // 显示搜索框后自动聚焦
                    if (searchInput) searchInput.focus();
                }
            });
        }
    }

    // 渲染商品列表
    function renderProducts(productList) {
        const productListEl = document.querySelector('.product-list');
        productListEl.innerHTML = '';
        
        if (productList.length === 0) {
            productListEl.innerHTML = '<div class="no-data">暂无相关商品</div>';
            return;
        }
        
        productList.forEach(product => {
            const productItem = document.createElement('div');
            productItem.className = 'product-item';
            productItem.setAttribute('data-id', product.id);
            productItem.setAttribute('data-category', product.category);
            
            productItem.innerHTML = `
                <div class="product-image" style="height: 180px; overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: #f5f5f5;">
                    <img src="${product.image}" alt="${product.name}" style="max-height: 100%; max-width: 100%; object-fit: contain;">
                </div>
                <div class="product-content">
                    <h4>${product.name}</h4>
                    <p>销量: ${product.sales}</p>
                    <div class="product-footer">
                        <span class="price">¥${product.price.toFixed(2)}</span>
                        <button class="add-to-cart" data-id="${product.id}">加入购物车</button>
                    </div>
                </div>
            `;
            
            // 绑定点击事件
            productItem.addEventListener('click', function() {
                const productId = this.getAttribute('data-id');
                showProductDetail(productId);
            });

            // 绑定加入购物车按钮事件（阻止冒泡，避免触发商品详情）
            const addToCartBtn = productItem.querySelector('.add-to-cart');
            addToCartBtn.addEventListener('click', function(e) {
                e.stopPropagation(); // 阻止事件冒泡
                const productId = this.getAttribute('data-id');
                addToCart(productId);
            });
            
            productListEl.appendChild(productItem);
        });
    }

    // 显示商品详情
    function showProductDetail(productId) {
        const product = products.find(p => p.id == productId);
        if (!product) return;
        
        // 检查是否存在商品详情模态框，如果不存在则创建
        let modal = document.getElementById('productDetailModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'productDetailModal';
            modal.className = 'cart-modal';
            modal.innerHTML = `<div class="cart-modal-content"><div class="modal-content"></div></div>`;
            document.body.appendChild(modal);
        }
        
        const modalContent = modal.querySelector('.modal-content');
        
        modalContent.innerHTML = `
            <div class="modal-header">
                <h3>商品详情</h3>
                <button class="close-modal">×</button>
            </div>
            <div class="modal-body">
                <div class="product-detail-image" style="height: 250px; overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: #f5f5f5;">
                    <img src="${product.image}" alt="${product.name}" style="max-height: 100%; max-width: 100%; object-fit: contain;">
                </div>
                <div class="product-detail-info">
                    <h3>${product.name}</h3>
                    <div class="product-price">
                        <span class="current-price">¥${product.price.toFixed(2)}</span>
                        <span class="original-price">¥${product.originalPrice.toFixed(2)}</span>
                    </div>
                    <div class="product-meta">
                        <span>分类: ${product.category}</span>
                        <span>销量: ${product.sales}</span>
                        <span>库存: ${product.stock}</span>
                    </div>
                    <p class="product-description">${product.description}</p>
                </div>
            </div>
            <div class="modal-footer">
                <button class="add-to-cart-btn" data-id="${product.id}">加入购物车</button>
                <button class="buy-now-btn" data-id="${product.id}">立即购买</button>
            </div>
        `;
        
        modal.style.display = 'block';
        modal.classList.add('show');
        
        // 绑定关闭按钮事件
        modal.querySelector('.close-modal').addEventListener('click', function() {
            modal.style.display = 'none';
            modal.classList.remove('show');
        });
        
        // 绑定加入购物车按钮事件
        modal.querySelector('.add-to-cart-btn').addEventListener('click', function() {
            addToCart(product.id);
            modal.style.display = 'none';
            modal.classList.remove('show');
        });
        
        // 绑定立即购买按钮事件
        modal.querySelector('.buy-now-btn').addEventListener('click', function() {
            buyNow(product.id);
        });
    }

    // 初始化搜索功能（增强版）
    function initSearch() {
        const searchBtn = document.getElementById('searchBtn');
        const searchInput = document.getElementById('searchInput');
        
        if (searchBtn && searchInput) {
            searchBtn.addEventListener('click', function() {
                performSearch();
            });

            // 回车搜索
            searchInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    performSearch();
                }
            });
        }

        // 执行搜索
        function performSearch() {
            if (!searchInput) return;
            
            const keyword = searchInput.value.toLowerCase().trim();
            if (!keyword) {
                renderProducts(products);
                return;
            }
            
            // 增强版搜索：支持部分匹配和关键词拆分搜索
            const keywords = keyword.split(/\s+/); // 拆分关键词
            
            const searchResult = products.filter(p => {
                const productText = `${p.name.toLowerCase()} ${p.description.toLowerCase()} ${p.category.toLowerCase()}`;
                // 只要有一个关键词匹配即可
                return keywords.some(k => productText.includes(k));
            });
            
            renderProducts(searchResult);
        }
    }

    // 添加到购物车
    function addToCart(productId) {
        // 从localStorage获取购物车数据
        let cart = JSON.parse(localStorage.getItem('cart') || '[]');
        
        // 查找是否已存在该商品
        let existingItem = cart.find(item => item.id == productId);
        
        if (existingItem) {
            // 如果已存在，增加数量
            existingItem.quantity++;
        } else {
            // 如果不存在，添加新项
            const product = products.find(p => p.id == productId);
            cart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                image: product.image,
                quantity: 1
            });
        }
        
        // 保存购物车数据
        localStorage.setItem('cart', JSON.stringify(cart));
        
        // 更新购物车计数
        updateCartCount();
        
        // 显示添加成功提示 - 创建自定义提示
        showAddToCartSuccess();
    }
    
    // 显示添加成功提示
    function showAddToCartSuccess() {
        // 创建提示元素
        let successMessage = document.createElement('div');
        successMessage.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background-color: rgba(76, 175, 80, 0.9);
            color: white;
            padding: 15px 25px;
            border-radius: 4px;
            z-index: 9999;
            font-size: 16px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
            opacity: 0;
            transition: opacity 0.3s ease;
        `;
        successMessage.textContent = '已成功添加到购物车！';
        
        // 添加到页面
        document.body.appendChild(successMessage);
        
        // 显示提示
        setTimeout(() => {
            successMessage.style.opacity = '1';
        }, 10);
        
        // 2秒后自动消失
        setTimeout(() => {
            successMessage.style.opacity = '0';
            setTimeout(() => {
                if (document.body.contains(successMessage)) {
                    document.body.removeChild(successMessage);
                }
            }, 300);
        }, 2000);
    }

    // 立即购买
    function buyNow(productId) {
        // 将商品信息保存到localStorage，用于结算页面
        const product = products.find(p => p.id == productId);
        localStorage.setItem('orderItems', JSON.stringify([{
            id: product.id,
            name: product.name,
            price: product.price,
            image: product.image,
            quantity: 1
        }]));
        
        // 显示支付方式选择
        showPaymentModal();
    }

    // 更新购物车计数
    function updateCartCount() {
        const cart = JSON.parse(localStorage.getItem('cart') || '[]');
        const totalCount = cart.reduce((sum, item) => sum + item.quantity, 0);
        const cartCountEl = document.getElementById('cartCount');
        if (cartCountEl) {
            cartCountEl.textContent = totalCount;
        }
    }

    // 初始化购物车相关功能
    function initCart() {
        // 初始化购物车计数
        updateCartCount();
        
        // 绑定购物车图标点击事件
        const cartIcon = document.getElementById('cartIcon');
        const cartModal = document.getElementById('cartModal');
        const closeCartModal = document.getElementById('closeCartModal');
        
        if (cartIcon && cartModal) {
            cartIcon.addEventListener('click', function() {
                cartModal.classList.toggle('show');
                if (cartModal.classList.contains('show')) {
                    renderCartItems();
                }
            });
        }
        
        if (closeCartModal && cartModal) {
            closeCartModal.addEventListener('click', function() {
                cartModal.classList.remove('show');
            });
        }
    }

    // 渲染购物车商品
    function renderCartItems() {
        const cart = JSON.parse(localStorage.getItem('cart') || '[]');
        const cartItemsEl = document.getElementById('cartItems');
        const cartTotalAmountEl = document.getElementById('cartTotalAmount');
        const checkoutBtn = document.getElementById('checkoutBtn');
        
        if (!cartItemsEl) return;
        
        if (cart.length === 0) {
            cartItemsEl.innerHTML = '<div class="no-cart-items">购物车空空如也</div>';
            if (cartTotalAmountEl) cartTotalAmountEl.textContent = '¥0.00';
            if (checkoutBtn) {
                checkoutBtn.disabled = true;
                checkoutBtn.style.opacity = '0.6';
            }
            return;
        }
        
        cartItemsEl.innerHTML = '';
        let totalAmount = 0;
        
        cart.forEach(item => {
            const itemTotal = item.price * item.quantity;
            totalAmount += itemTotal;
            
            const cartItem = document.createElement('div');
            cartItem.className = 'cart-item';
            cartItem.innerHTML = `
                <div class="cart-item-image">
                    <img src="${item.image}" alt="${item.name}">
                </div>
                <div class="cart-item-info">
                    <div class="cart-item-name">${item.name}</div>
                    <div class="cart-item-price">¥${item.price.toFixed(2)}</div>
                    <div class="cart-item-quantity">
                        <button class="quantity-btn decrease" data-id="${item.id}">-</button>
                        <input type="text" class="quantity-value" value="${item.quantity}" readonly>
                        <button class="quantity-btn increase" data-id="${item.id}">+</button>
                    </div>
                </div>
            `;
            
            cartItemsEl.appendChild(cartItem);
        });
        
        if (cartTotalAmountEl) {
            cartTotalAmountEl.textContent = `¥${totalAmount.toFixed(2)}`;
        }
        
        if (checkoutBtn) {
            checkoutBtn.disabled = false;
            checkoutBtn.style.opacity = '1';
        }
        
        // 绑定数量变更事件
        document.querySelectorAll('.quantity-btn.decrease').forEach(btn => {
            btn.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                updateCartItemQuantity(id, -1);
            });
        });
        
        document.querySelectorAll('.quantity-btn.increase').forEach(btn => {
            btn.addEventListener('click', function() {
                const id = this.getAttribute('data-id');
                updateCartItemQuantity(id, 1);
            });
        });
    }

    // 更新购物车商品数量
    function updateCartItemQuantity(id, change) {
        let cart = JSON.parse(localStorage.getItem('cart') || '[]');
        const item = cart.find(item => item.id == id);
        
        if (item) {
            item.quantity += change;
            if (item.quantity <= 0) {
                cart = cart.filter(item => item.id != id);
            }
            
            localStorage.setItem('cart', JSON.stringify(cart));
            updateCartCount();
            renderCartItems();
        }
    }

    // 显示支付模态框
    function showPaymentModal() {
        const paymentModal = document.getElementById('paymentModal');
        if (paymentModal) {
            paymentModal.classList.add('show');
        }
        
        // 绑定支付确认事件
        const confirmPayment = document.getElementById('confirmPayment');
        const cancelPayment = document.getElementById('cancelPayment');
        
        if (confirmPayment) {
            confirmPayment.addEventListener('click', function() {
                alert('支付成功！');
                // 清空购物车
                localStorage.removeItem('cart');
                updateCartCount();
                if (paymentModal) paymentModal.classList.remove('show');
                if (document.getElementById('cartModal')) {
                    document.getElementById('cartModal').classList.remove('show');
                }
            });
        }
        
        if (cancelPayment && paymentModal) {
            cancelPayment.addEventListener('click', function() {
                paymentModal.classList.remove('show');
            });
        }
    }

    // 初始化所有功能
    initSearchToggle();
    initSearch();
    initCart();
    renderProducts(products);
});