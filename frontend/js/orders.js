/**
 * PharmaTrack - Customer Order Tracking & History Script
 */

let myOrders = [];
let orderToCancel = null;

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('orders');
    if (!requireAuth()) return;
    await loadCustomerOrders();
});

async function loadCustomerOrders() {
    const listContainer = document.getElementById('orders-container');
    if (!listContainer) return;

    listContainer.innerHTML = '<div style="text-align: center; padding: 3rem; color: #64748b;">Loading your orders...</div>';

    try {
        const res = await api.get('/orders/my-orders');
        if (res.data) {
            myOrders = res.data;
            renderOrdersList(myOrders);
        }
    } catch (err) {
        listContainer.innerHTML = '<div style="text-align: center; padding: 3rem; color: #ef4444;">Failed to load order history.</div>';
    }
}

function renderOrdersList(orders) {
    const container = document.getElementById('orders-container');
    if (!container) return;

    if (orders.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 4rem 1rem; background: #ffffff; border-radius: 12px; border: 1px dashed #cbd5e1;">
                <div style="font-size: 3rem; margin-bottom: 0.5rem;">📦</div>
                <h3>No Orders Yet</h3>
                <p style="color: #64748b; margin-bottom: 1.5rem;">You haven't placed any medicine orders with PharmaTrack yet.</p>
                <a href="index.html" class="btn btn-primary">Browse Medicine Catalog</a>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => {
        const canCancel = (order.status === 'PENDING' || order.status === 'CONFIRMED');
        const formattedDate = new Date(order.createdAt).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        const statusClass = `status-${order.status.toLowerCase()}`;

        // Render item list
        const itemsHtml = order.items.map(i => `
            <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem;">
                <div>
                    <span style="font-weight: 600; color: #0f172a;">${escapeHtml(i.medicineName)}</span>
                    <span style="color: #64748b; margin-left: 0.5rem;">× ${i.quantity} (Batch: ${escapeHtml(i.batchNumber || 'N/A')})</span>
                </div>
                <div style="font-weight: 600;">$${Number(i.subtotal).toFixed(2)}</div>
            </div>
        `).join('');

        return `
            <div class="card" style="margin-bottom: 1.5rem;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 1rem; padding-bottom: 1rem; border-bottom: 1px solid #e2e8f0;">
                    <div>
                        <div style="font-size: 1.15rem; font-weight: 700; color: #0284c7;">#${order.orderNumber}</div>
                        <div style="font-size: 0.85rem; color: #64748b;">Placed on ${formattedDate}</div>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span class="status-badge ${statusClass}">${order.status.replace(/_/g, ' ')}</span>
                        ${order.requiresPrescription ? '<span class="rx-badge">Rx Verified Order</span>' : ''}
                    </div>
                </div>

                <div style="margin-bottom: 1rem;">
                    ${itemsHtml}
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; padding-top: 0.5rem;">
                    <div style="font-size: 0.85rem; color: #475569;">
                        <div><strong>Delivery Address:</strong> ${escapeHtml(order.deliveryAddress)}</div>
                        <div><strong>Payment:</strong> ${order.paymentMethod.replace(/_/g, ' ')} (${order.paymentStatus})</div>
                        ${order.cancellationReason ? `<div style="color: #ef4444; margin-top: 0.25rem;"><strong>Cancellation Note:</strong> ${escapeHtml(order.cancellationReason)}</div>` : ''}
                    </div>
                    <div style="text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 0.5rem;">
                        <div style="font-size: 1.25rem; font-weight: 800; color: #0f172a;">
                            Total: $${Number(order.totalAmount).toFixed(2)}
                        </div>
                        ${canCancel ? `
                            <button class="btn btn-outline btn-sm" style="color: #ef4444; border-color: #fca5a5;" onclick="openCancelModal(${order.id}, '${order.orderNumber}')">
                                Cancel Order
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function openCancelModal(orderId, orderNumber) {
    orderToCancel = orderId;
    document.getElementById('cancel-modal-ord-no').innerText = orderNumber;
    document.getElementById('cancel-modal').style.display = 'flex';
}

function closeCancelModal() {
    orderToCancel = null;
    document.getElementById('cancel-modal').style.display = 'none';
}

async function confirmCancelOrder() {
    if (!orderToCancel) return;
    const reason = document.getElementById('cancel-reason').value;

    try {
        const res = await api.post(`/orders/${orderToCancel}/cancel`, { reason });
        if (res.success) {
            showToast('Order cancelled and reserved stock released back to inventory', 'info');
            closeCancelModal();
            await loadCustomerOrders();
        }
    } catch (err) {
        // Handled by api.js
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
