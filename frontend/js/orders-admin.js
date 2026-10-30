/**
 * PharmaTrack - Staff & Admin Order Fulfillment & Prescription Review Script
 */

let allStaffOrders = [];
let selectedOrder = null;

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('orders-manage');
    if (!requireAuth(['ROLE_ADMIN', 'ROLE_STAFF'])) return;

    await loadStaffOrders();

    const searchInput = document.getElementById('order-search');
    if (searchInput) searchInput.addEventListener('input', filterOrders);

    const statusFilter = document.getElementById('order-status-filter');
    if (statusFilter) statusFilter.addEventListener('change', filterOrders);
});

async function loadStaffOrders() {
    const tbody = document.getElementById('orders-admin-body');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 3rem; color: #64748b;">Loading customer orders...</td></tr>';

    try {
        const res = await api.get('/orders/staff/all');
        if (res.data) {
            allStaffOrders = res.data;
            filterOrders();
        }
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: #ef4444; padding: 2rem;">Failed to load orders.</td></tr>';
    }
}

function filterOrders() {
    const query = (document.getElementById('order-search')?.value || '').toLowerCase().trim();
    const status = document.getElementById('order-status-filter')?.value || '';

    const filtered = allStaffOrders.filter(o => {
        const matchesQuery = !query ||
            o.orderNumber.toLowerCase().includes(query) ||
            o.customerName.toLowerCase().includes(query) ||
            o.customerPhone.toLowerCase().includes(query);

        const matchesStatus = !status || o.status === status;
        return matchesQuery && matchesStatus;
    });

    renderOrdersTable(filtered);
}

function renderOrdersTable(orders) {
    const tbody = document.getElementById('orders-admin-body');
    if (!tbody) return;

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: #64748b; padding: 2rem;">No orders found.</td></tr>';
        return;
    }

    tbody.innerHTML = orders.map(o => {
        const date = new Date(o.createdAt).toLocaleDateString('en-US', {
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        let rxBadge = '<span style="color: #64748b; font-size: 0.8rem;">OTC</span>';
        if (o.requiresPrescription) {
            if (o.prescriptionVerifiedBy) {
                rxBadge = `<span class="otc-badge" style="font-size: 0.7rem;">Verified by ${escapeHtml(o.prescriptionVerifiedBy)}</span>`;
            } else {
                rxBadge = `<span class="rx-badge" style="font-size: 0.7rem;">Rx Pending Review</span>`;
            }
        }

        const statusClass = `status-${o.status.toLowerCase()}`;

        return `
            <tr>
                <td><strong>#${escapeHtml(o.orderNumber)}</strong></td>
                <td>
                    <strong>${escapeHtml(o.customerName)}</strong>
                    <div style="font-size: 0.78rem; color: #64748b;">${escapeHtml(o.customerPhone)}</div>
                </td>
                <td>${date}</td>
                <td><strong>$${Number(o.totalAmount).toFixed(2)}</strong></td>
                <td>${rxBadge}</td>
                <td><small>${o.paymentMethod.replace(/_/g, ' ')} (${o.paymentStatus})</small></td>
                <td><span class="status-badge ${statusClass}">${o.status.replace(/_/g, ' ')}</span></td>
                <td>
                    <button class="btn btn-primary btn-sm" onclick="openManageOrderModal(${o.id})">
                        Process
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function openManageOrderModal(id) {
    const order = allStaffOrders.find(o => o.id === id);
    if (!order) return;
    selectedOrder = order;

    document.getElementById('manage-modal-title').innerText = `Process Order #${order.orderNumber}`;
    document.getElementById('m-cust-name').innerText = order.customerName;
    document.getElementById('m-cust-phone').innerText = order.customerPhone;
    document.getElementById('m-cust-address').innerText = order.deliveryAddress;

    // Prescription Review
    const rxBox = document.getElementById('m-rx-box');
    if (rxBox) {
        if (order.requiresPrescription) {
            rxBox.style.display = 'block';
            document.getElementById('m-rx-note').innerText = order.prescriptionNote || 'Doctor note attached during checkout';
            document.getElementById('m-rx-verifier').value = order.prescriptionVerifiedBy || '';
        } else {
            rxBox.style.display = 'none';
        }
    }

    // Render items and batches
    const itemsList = document.getElementById('m-order-items-list');
    if (itemsList) {
        itemsList.innerHTML = order.items.map(item => `
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0; border-bottom: 1px solid #f1f5f9; font-size: 0.88rem;">
                <div>
                    <strong>${escapeHtml(item.medicineName)}</strong>
                    <div style="color: #64748b; font-size: 0.78rem;">
                        Allocated Batch: <code>${escapeHtml(item.batchNumber)}</code> (Exp: ${item.batchExpiryDate})
                    </div>
                </div>
                <div style="text-align: right;">
                    <div>Qty: <strong>${item.quantity}</strong></div>
                    <div style="font-weight: 600;">$${Number(item.subtotal).toFixed(2)}</div>
                </div>
            </div>
        `).join('');
    }

    // Set new status dropdown
    const statusSelect = document.getElementById('m-new-status');
    if (statusSelect) {
        statusSelect.value = order.status;
    }

    document.getElementById('manage-order-modal').style.display = 'flex';
}

function closeManageOrderModal() {
    selectedOrder = null;
    document.getElementById('manage-order-modal').style.display = 'none';
}

async function saveOrderStatusUpdate() {
    if (!selectedOrder) return;

    const newStatus = document.getElementById('m-new-status').value;
    const reason = document.getElementById('m-status-reason').value;
    const rxVerifier = document.getElementById('m-rx-verifier')?.value;

    if (selectedOrder.requiresPrescription && (!rxVerifier || rxVerifier.trim() === '')) {
        if (['CONFIRMED', 'PROCESSING', 'READY_FOR_DISPATCH', 'COMPLETED'].includes(newStatus)) {
            showToast('Prescription-required orders must be verified by pharmacist signature before processing', 'error');
            return;
        }
    }

    const payload = {
        status: newStatus,
        reason: reason,
        prescriptionVerifiedBy: rxVerifier
    };

    try {
        const res = await api.patch(`/orders/${selectedOrder.id}/status`, payload);
        if (res.success) {
            showToast(`Order status updated to ${newStatus}`, 'success');
            closeManageOrderModal();
            await loadStaffOrders();
        }
    } catch (err) {}
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
