/**
 * PharmaTrack - Authentication & Role Guard Script
 */

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;

            try {
                const res = await api.post('/auth/login', { username, password });
                if (res.success && res.data) {
                    api.setSession(res.data.token, res.data);
                    showToast('Login successful! Redirecting...', 'success');
                    redirectByRole(res.data.role);
                }
            } catch (err) {
                // Handled by api.js toast
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('reg-username').value;
            const email = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            const fullName = document.getElementById('reg-fullname').value;
            const phoneNumber = document.getElementById('reg-phone').value;

            try {
                const res = await api.post('/auth/register', {
                    username,
                    email,
                    password,
                    fullName,
                    phoneNumber
                });
                if (res.success && res.data) {
                    api.setSession(res.data.token, res.data);
                    showToast('Account registered successfully! Redirecting...', 'success');
                    redirectByRole(res.data.role);
                }
            } catch (err) {
                // Handled by api.js toast
            }
        });
    }
});

// Quick Demo Login Helper for Examiner / Viva Evaluation
async function quickLogin(role) {
    let username = 'customer';
    let password = 'Customer@123';

    if (role === 'admin') {
        username = 'admin';
        password = 'Admin@123';
    } else if (role === 'staff') {
        username = 'staff';
        password = 'Staff@123';
    }

    try {
        const res = await api.post('/auth/login', { username, password });
        if (res.success && res.data) {
            api.setSession(res.data.token, res.data);
            showToast(`Logged in as ${res.data.fullName} (${res.data.role})`, 'success');
            redirectByRole(res.data.role);
        }
    } catch (err) {
        showToast('Login failed: ' + err.message, 'error');
    }
}

function redirectByRole(role) {
    setTimeout(() => {
        if (role === 'ROLE_ADMIN') {
            window.location.href = 'admin-dashboard.html';
        } else if (role === 'ROLE_STAFF') {
            window.location.href = 'staff-dashboard.html';
        } else {
            window.location.href = 'index.html';
        }
    }, 600);
}

// Role Guard function to protect admin/staff pages
function requireAuth(allowedRoles = []) {
    const user = api.getUser();
    if (!user || !api.getToken()) {
        showToast('Authentication required. Please login.', 'warning');
        window.location.href = 'login.html';
        return false;
    }

    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        showToast('Access denied: You do not have permissions for this section.', 'error');
        window.location.href = 'index.html';
        return false;
    }

    return true;
}
