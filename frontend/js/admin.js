/**
 * PharmaTrack - Administrator & Staff Dashboard Management
 */

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('admin');
    if (!requireAuth(['ROLE_ADMIN', 'ROLE_STAFF'])) return;
    await loadDashboardMetrics();
});

async function loadDashboardMetrics() {
    try {
        const res = await api.get('/dashboard/metrics');
        if (res.data) {
            const data = res.data;

            // Metrics KPI cards
            setElText('kpi-medicines', data.totalActiveMedicines);
            setElText('kpi-stock', data.totalEligibleStockUnits);
            setElText('kpi-lowstock', data.lowStockCount);
            setElText('kpi-nearexpiry', data.nearExpiryBatchesCount);
            setElText('kpi-expired', data.expiredBatchesCount);
            setElText('kpi-pending-orders', data.pendingOrdersCount);

            // Render Alert Section
            renderAlerts(data);

            // Render Recent Orders Table
            renderRecentOrders(data.recentOrders || []);

            // Render Recent Stock Movements
            renderRecentMovements(data.recentMovements || []);
        }
    } catch (err) {
        console.error('Failed to load metrics', err);
    }
}

function renderAlerts(data) {
    const alertContainer = document.getElementById('dashboard-alerts');
    if (!alertContainer) return;

    let alertsHtml = '';

    if (data.expiredBatchesCount > 0) {
        alertsHtml += `
            <div class="alert alert-danger">
                <span>⚠️</span>
                <div>
                    <strong>Action Required: ${data.expiredBatchesCount} Expired Batch(es) Detected.</strong>
                    These batches have been automatically blocked from customer ordering. Please dispose of or isolate them in inventory.
                    <a href="admin-batches.html?filter=expired" style="margin-left: 0.5rem; text-decoration: underline; font-weight: 600;">View Expired Batches →</a>
                </div>
            </div>
        `;
    }

    if (data.nearExpiryBatchesCount > 0) {
        alertsHtml += `
            <div class="alert alert-warning">
                <span>🔔</span>
                <div>
                    <strong>Attention: ${data.nearExpiryBatchesCount} Batch(es) Expiring within 30 days.</strong>
                    PharmaTrack's FEFO engine prioritizes dispensing these first to avoid wastage.
                    <a href="admin-batches.html?filter=near-expiry" style="margin-left: 0.5rem; text-decoration: underline; font-weight: 600;">View Near-Expiry Stock →</a>
                </div>
            </div>
        `;
    }

    if (data.lowStockCount > 0) {
        alertsHtml += `
            <div class="alert alert-info">
                <span>📦</span>
                <div>
                    <strong>Stock Replenishment Needed: ${data.lowStockCount} Medicine(s) Below Threshold.</strong>
                    <a href="admin-suppliers.html" style="margin-left: 0.5rem; text-decoration: underline; font-weight: 600;">Create Purchase Order →</a>
                </div>
            </div>
        `;
    }

    alertContainer.innerHTML = alertsHtml;
}

function renderRecentOrders(orders) {
    const tbody = document.getElementById('recent-orders-body');
    if (!tbody) return;

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b; padding: 2rem;">No recent orders.</td></tr>';
        return;
    }

    tbody.innerHTML = orders.map(order => {
        const formattedDate = new Date(order.createdAt).toLocaleDateString('en-US', {
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        const statusClass = `status-${order.status.toLowerCase()}`;

        return `
            <tr>
                <td><strong>#${escapeHtml(order.orderNumber)}</strong></td>
                <td>${escapeHtml(order.customerName)}</td>
                <td>${formattedDate}</td>
                <td><strong>$${Number(order.totalAmount).toFixed(2)}</strong></td>
                <td>
                    ${order.requiresPrescription ? 
                        (order.prescriptionVerifiedBy ? 
                            '<span class="otc-badge" style="font-size: 0.7rem;">Rx Verified</span>' : 
                            '<span class="rx-badge" style="font-size: 0.7rem;">Rx Pending Review</span>'
                        ) : '<span style="color: #64748b; font-size: 0.8rem;">OTC</span>'
                    }
                </td>
                <td><span class="status-badge ${statusClass}">${order.status.replace(/_/g, ' ')}</span></td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="openOrderModal(${order.id})">Manage</button>
                </td>
            </tr>
        `;
    }).join('');
}

function renderRecentMovements(movements) {
    const container = document.getElementById('recent-movements-list');
    if (!container) return;

    if (movements.length === 0) {
        container.innerHTML = '<div style="color: #64748b; text-align: center; padding: 2rem;">No recent stock movements.</div>';
        return;
    }

    container.innerHTML = movements.map(m => {
        const time = new Date(m.createdAt).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        let typeBadge = '<span class="status-badge status-confirmed">' + m.movementType + '</span>';
        let qtyColor = '#10b981';

        if (m.movementType.includes('DISPOSAL') || m.movementType.includes('DECREASE')) {
            qtyColor = '#ef4444';
        }

        return `
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 0; border-bottom: 1px solid #f1f5f9; font-size: 0.85rem;">
                <div>
                    <div style="font-weight: 600; color: #0f172a;">${escapeHtml(m.medicineName)} (Batch: ${escapeHtml(m.batchNumber)})</div>
                    <div style="color: #64748b; margin-top: 0.15rem;">${escapeHtml(m.reason || m.movementType)} • ${time} by ${escapeHtml(m.createdBy || 'SYSTEM')}</div>
                </div>
                <div style="text-align: right;">
                    <div style="font-weight: 700; color: ${qtyColor};">${m.quantity} units</div>
                    <div style="font-size: 0.75rem; color: #64748b;">Remaining: ${m.quantityAfter}</div>
                </div>
            </div>
        `;
    }).join('');
}

function setElText(id, text) {
    const el = document.getElementById(id);
    if (el) el.innerText = text;
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
