/**
 * LUNETTE GIFTS — BOUTIQUE COMMON JAVASCRIPT
 * Opening door animation, petals, cart helpers, toast notifications, auth checks
 */

document.addEventListener('DOMContentLoaded', () => {
    initOpeningScreen();
    createFloatingPetals();
    updateCartCountBadge();
    checkAuthNav();
});

// 1. OPENING DOOR ANIMATION
function initOpeningScreen() {
    const openingScreen = document.getElementById('opening-screen');
    const doorWrapper = document.querySelector('.door-wrapper');
    const skipBtn = document.querySelector('.skip-intro-btn');

    if (!openingScreen) return;

    // Check if user already saw the intro in this session or previously
    const introShown = localStorage.getItem('introShown');
    if (introShown) {
        openingScreen.classList.add('hidden');
        return;
    }

    // Sequence:
    // 0s: Door closed with handles
    // 0.8s: Backlight intensifies, doors slowly open
    // 2.2s: Petals burst and logo reveals
    // 3.8s: Opening screen gently fades into main website
    setTimeout(() => {
        if (doorWrapper) doorWrapper.classList.add('doors-open');
    }, 800);

    setTimeout(() => {
        dismissOpeningScreen();
    }, 3800);

    if (skipBtn) {
        skipBtn.addEventListener('click', () => {
            dismissOpeningScreen();
        });
    }

    function dismissOpeningScreen() {
        openingScreen.classList.add('hidden');
        localStorage.setItem('introShown', 'true');
    }
}

// 2. FLOATING PETALS
function createFloatingPetals() {
    const hero = document.querySelector('.hero-section');
    if (!hero) return;

    for (let i = 0; i < 6; i++) {
        const petal = document.createElement('div');
        petal.className = 'floating-petal-bg';
        petal.style.left = `${Math.random() * 85 + 5}%`;
        petal.style.top = `${Math.random() * 70 + 10}%`;
        petal.style.animationDelay = `${Math.random() * 5}s`;
        petal.style.animationDuration = `${8 + Math.random() * 6}s`;
        hero.appendChild(petal);
    }
}

// 3. CART STORAGE HELPERS
function getCart() {
    try {
        return JSON.parse(localStorage.getItem('lunette_cart')) || [];
    } catch (e) {
        return [];
    }
}

function saveCart(cart) {
    localStorage.setItem('lunette_cart', JSON.stringify(cart));
    updateCartCountBadge();
}

function updateCartCountBadge() {
    const cart = getCart();
    const count = cart.reduce((acc, item) => acc + (item.quantity || 1), 0);
    const badges = document.querySelectorAll('.cart-badge');
    badges.forEach(b => {
        b.textContent = count;
        b.style.display = count > 0 ? 'flex' : 'none';
    });
}

// 4. TOAST NOTIFICATIONS
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.position = 'fixed';
        container.style.bottom = '24px';
        container.style.right = '24px';
        container.style.zIndex = '999999';
        container.style.display = 'flex';
        container.style.flexDirection = 'column';
        container.style.gap = '10px';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.style.background = type === 'success' ? '#2C2523' : '#A85364';
    toast.style.color = '#FFFFFF';
    toast.style.padding = '12px 20px';
    toast.style.borderRadius = '30px';
    toast.style.boxShadow = '0 8px 24px rgba(0,0,0,0.18)';
    toast.style.fontFamily = "'Poppins', sans-serif";
    toast.style.fontSize = '13px';
    toast.style.fontWeight = '500';
    toast.style.display = 'flex';
    toast.style.alignItems = 'center';
    toast.style.gap = '8px';
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(15px)';
    toast.style.transition = 'all 0.3s ease';

    toast.innerHTML = `<span>🌸</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 50);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(15px)';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

// 5. AUTH STATE IN HEADER
function checkAuthNav() {
    const token = localStorage.getItem('lunette_token');
    const role = localStorage.getItem('lunette_role');
    const authLink = document.getElementById('header-account-link');

    if (!authLink) return;

    if (token) {
        if (role === 'ROLE_ADMIN') {
            authLink.href = './admin/index.html';
            authLink.title = 'Owner Dashboard';
        } else if (role === 'ROLE_WORKER') {
            authLink.href = './staff/index.html';
            authLink.title = 'Staff Dashboard';
        } else {
            authLink.href = './customer/account.html';
            authLink.title = 'My Account';
        }
    } else {
        authLink.href = './login.html';
        authLink.title = 'Sign In / Register';
    }
}
