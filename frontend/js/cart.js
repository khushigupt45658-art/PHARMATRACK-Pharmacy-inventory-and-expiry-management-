/**
 * PharmaTrack - Cart & Transactional Checkout Script
 */

document.addEventListener('DOMContentLoaded', () => {
    renderNavbar('cart');
    renderCart();

    const checkoutForm = document.getElementById('checkout-form');
    if (checkoutForm) {
        // Pre-fill user profile if logged in
        const user = api.getUser();
        if (user) {
            const nameField = document.getElementById('cust-name');
            const emailField = document.getElementById('cust-email');
            const phoneField = document.getElementById('cust-phone');
            if (nameField && user.fullName) nameField.value = user.fullName;
            if (emailField && user.email) emailField.value = user.email;
            if (phoneField && user.phoneNumber) phoneField.value = user.phoneNumber;
        }

        checkoutForm.addEventListener('submit', handleCheckout);
    }
});

function getCart() {
    return JSON.parse(localStorage.getItem('pharmatrack_cart') || '[]');
}

function saveCart(cart) {
    localStorage.setItem('pharmatrack_cart', JSON.stringify(cart));
    const badge = document.getElementById('nav-cart-badge');
    if (badge) {
        const count = cart.reduce((sum, item) => sum + item.quantity, 0);
        badge.innerText = count;
    }
    renderCart();
}

function renderCart() {
    const cart = getCart();
    const tableBody = document.getElementById('cart-items-body');
    const emptyState = document.getElementById('cart-empty-state');
    const contentSection = document.getElementById('cart-content-section');
    const prescriptionSection = document.getElementById('cart-prescription-section');

    if (!tableBody) return;

    if (cart.length === 0) {
        if (emptyState) emptyState.style.display = 'block';
        if (contentSection) contentSection.style.display = 'none';
        return;
    }

    if (emptyState) emptyState.style.display = 'none';
    if (contentSection) contentSection.style.display = 'grid';

    let subtotal = 0;
    let hasPrescriptionItems = false;

    tableBody.innerHTML = cart.map((item, index) => {
        const lineTotal = item.unitPrice * item.quantity;
        subtotal += lineTotal;
        if (item.prescriptionRequired) hasPrescriptionItems = true;

        const rxPill = item.prescriptionRequired 
            ? `<span class="rx-badge" style="font-size: 0.65rem;">Rx Required</span>`
            : `<span class="otc-badge" style="font-size: 0.65rem;">OTC</span>`;

        return `
            <tr>
                <td>
                    <div style="font-weight: 600; color: #0f172a;">${escapeHtml(item.medicineName)}</div>
                    <div style="margin-top: 0.2rem;">${rxPill}</div>
                </td>
                <td>$${Number(item.unitPrice).toFixed(2)}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <button class="btn btn-outline btn-sm" onclick="updateQty(${index}, -1)" style="padding: 0.2rem 0.5rem;">-</button>
                        <span style="font-weight: 600; min-width: 20px; text-align: center;">${item.quantity}</span>
                        <button class="btn btn-outline btn-sm" onclick="updateQty(${index}, 1)" style="padding: 0.2rem 0.5rem;">+</button>
                    </div>
                </td>
                <td style="font-weight: 700;">$${lineTotal.toFixed(2)}</td>
                <td>
                    <button class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fca5a5;" onclick="removeItem(${index})">Remove</button>
                </td>
            </tr>
        `;
    }).join('');

    // Update totals
    const subtotalEl = document.getElementById('cart-subtotal');
    const totalEl = document.getElementById('cart-total');
    if (subtotalEl) subtotalEl.innerText = '$' + subtotal.toFixed(2);
    if (totalEl) totalEl.innerText = '$' + subtotal.toFixed(2);

    // Show or hide prescription upload section
    if (prescriptionSection) {
        prescriptionSection.style.display = hasPrescriptionItems ? 'block' : 'none';
    }
}

function updateQty(index, delta) {
    const cart = getCart();
    if (!cart[index]) return;

    cart[index].quantity += delta;
    if (cart[index].quantity <= 0) {
        cart.splice(index, 1);
    }
    saveCart(cart);
}

function removeItem(index) {
    const cart = getCart();
    cart.splice(index, 1);
    saveCart(cart);
    showToast('Item removed from cart', 'info');
}

async function handleCheckout(e) {
    e.preventDefault();

    const user = api.getUser();
    if (!user) {
        showToast('Please login to complete your order checkout', 'warning');
        setTimeout(() => {
            window.location.href = 'login.html?redirect=cart.html';
        }, 1000);
        return;
    }

    const cart = getCart();
    if (cart.length === 0) {
        showToast('Your cart is empty', 'error');
        return;
    }

    const customerName = document.getElementById('cust-name').value;
    const customerPhone = document.getElementById('cust-phone').value;
    const customerEmail = document.getElementById('cust-email')?.value;
    const deliveryAddress = document.getElementById('cust-address').value;
    const paymentMethod = document.getElementById('cust-payment').value;
    const prescriptionNote = document.getElementById('cust-prescription')?.value;

    const hasRx = cart.some(i => i.prescriptionRequired);
    if (hasRx && (!prescriptionNote || prescriptionNote.trim().length < 5)) {
        showToast('Please enter doctor/prescription details for prescription medications in your cart', 'warning');
        document.getElementById('cust-prescription').focus();
        return;
    }

    const checkoutBtn = document.getElementById('btn-checkout');
    if (checkoutBtn) {
        checkoutBtn.disabled = true;
        checkoutBtn.innerText = 'Verifying Stock & Processing...';
    }

    const payload = {
        customerName,
        customerPhone,
        customerEmail,
        deliveryAddress,
        paymentMethod,
        prescriptionNote: hasRx ? prescriptionNote : null,
        items: cart.map(i => ({
            medicineId: i.medicineId,
            medicineName: i.medicineName,
            quantity: i.quantity,
            unitPrice: i.unitPrice,
            prescriptionRequired: i.prescriptionRequired
        }))
    };

    try {
        const res = await api.post('/orders/checkout', payload);
        if (res.success && res.data) {
            // Clear cart
            localStorage.removeItem('pharmatrack_cart');
            saveCart([]);

            showToast(`Order #${res.data.orderNumber} placed successfully!`, 'success');

            // Render order confirmation
            const modalConfirm = document.getElementById('order-confirmed-modal');
            if (modalConfirm) {
                document.getElementById('conf-order-num').innerText = res.data.orderNumber;
                document.getElementById('conf-order-total').innerText = '$' + Number(res.data.totalAmount).toFixed(2);
                document.getElementById('conf-order-status').innerText = res.data.status;
                modalConfirm.style.display = 'flex';
            } else {
                window.location.href = 'orders.html';
            }
        }
    } catch (err) {
        // Handled by api.js
    } finally {
        if (checkoutBtn) {
            checkoutBtn.disabled = false;
            checkoutBtn.innerText = 'Place Order';
        }
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text.toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
