/**
 * PharmaTrack - API Client & Central Utility
 */

const API_BASE = window.location.origin.includes(':8080') 
    ? '/api' 
    : 'http://localhost:8080/api';

const TOKEN_KEY = 'pharmatrack_jwt_token';
const USER_KEY = 'pharmatrack_user_data';

const api = {
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    setSession(token, user) {
        localStorage.setItem(TOKEN_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },

    clearSession() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    },

    getUser() {
        const u = localStorage.getItem(USER_KEY);
        try {
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    },

    async request(endpoint, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(`${API_BASE}${endpoint}`, config);

            // Handle blob downloads (CSV)
            if (options.responseType === 'blob') {
                if (!response.ok) throw new Error('Download failed');
                return await response.blob();
            }

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                if (response.status === 401) {
                    this.clearSession();
                    if (!window.location.pathname.endsWith('login.html')) {
                        window.location.href = 'login.html?expired=true';
                    }
                }
                const errorMessage = data.message || `Request failed (${response.status})`;
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            showToast(error.message, 'error');
            throw error;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    },

    patch(endpoint, body) {
        return this.request(endpoint, {
            method: 'PATCH',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// Global Toast System
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';
    if (type === 'warning') icon = '🔔';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.transition = 'all 0.3s ease-out';
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Global Nav Bar Setup Helper
function renderNavbar(activePage = '') {
    const navContainer = document.getElementById('navbar-mount');
    if (!navContainer) return;

    const user = api.getUser();
    const cart = JSON.parse(localStorage.getItem('pharmatrack_cart') || '[]');
    const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);

    let roleTag = '';
    let navLinksHtml = `
        <li><a href="index.html" class="${activePage === 'home' ? 'active' : ''}">Browse Medicines</a></li>
    `;

    if (user) {
        if (user.role === 'ROLE_ADMIN') {
            roleTag = `<span class="role-tag role-admin">Administrator</span>`;
            navLinksHtml += `
                <li><a href="admin-dashboard.html" class="${activePage === 'admin' ? 'active' : ''}">Admin Portal</a></li>
                <li><a href="admin-medicines.html" class="${activePage === 'medicines' ? 'active' : ''}">Medicines</a></li>
                <li><a href="admin-batches.html" class="${activePage === 'batches' ? 'active' : ''}">Batches & Expiry</a></li>
                <li><a href="admin-orders.html" class="${activePage === 'orders-manage' ? 'active' : ''}">Orders</a></li>
                <li><a href="admin-reports.html" class="${activePage === 'reports' ? 'active' : ''}">Reports</a></li>
            `;
        } else if (user.role === 'ROLE_STAFF') {
            roleTag = `<span class="role-tag role-staff">Pharmacy Staff</span>`;
            navLinksHtml += `
                <li><a href="staff-dashboard.html" class="${activePage === 'staff' ? 'active' : ''}">Staff Dashboard</a></li>
                <li><a href="admin-batches.html" class="${activePage === 'batches' ? 'active' : ''}">Inventory & Batches</a></li>
                <li><a href="admin-orders.html" class="${activePage === 'orders-manage' ? 'active' : ''}">Process Orders</a></li>
            `;
        } else {
            roleTag = `<span class="role-tag role-customer">Customer</span>`;
            navLinksHtml += `
                <li><a href="orders.html" class="${activePage === 'orders' ? 'active' : ''}">My Orders</a></li>
            `;
        }
    }

    navContainer.innerHTML = `
        <nav class="navbar">
            <a href="index.html" class="brand">
                <div class="brand-icon">⚕</div>
                <div>PharmaTrack</div>
            </a>
            <ul class="nav-links">
                ${navLinksHtml}
            </ul>
            <div class="nav-actions">
                <a href="cart.html" class="cart-btn">
                    🛒 Cart
                    <span class="badge-count" id="nav-cart-badge">${cartCount}</span>
                </a>
                ${user ? `
                    <div class="user-badge">
                        <span>👤 ${user.fullName || user.username}</span>
                        ${roleTag}
                    </div>
                    <button class="btn btn-outline btn-sm" style="color: #cbd5e1; border-color: rgba(255,255,255,0.2);" onclick="handleLogout()">Logout</button>
                ` : `
                    <a href="login.html" class="btn btn-primary btn-sm">Login / Register</a>
                `}
            </div>
        </nav>
    `;
}

function handleLogout() {
    api.clearSession();
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
        window.location.href = 'login.html';
    }, 500);
}
