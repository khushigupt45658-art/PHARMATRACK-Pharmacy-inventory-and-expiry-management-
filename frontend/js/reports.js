/**
 * PharmaTrack - Real-Time Reports & CSV Export Engine
 */

let activeTab = 'inventory';

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('reports');
    if (!requireAuth(['ROLE_ADMIN', 'ROLE_STAFF'])) return;

    await loadActiveReport();
});

function switchReportTab(tabName) {
    activeTab = tabName;
    document.querySelectorAll('.report-tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabName);
    });
    loadActiveReport();
}

async function loadActiveReport() {
    const tableBody = document.getElementById('report-table-body');
    const tableHeaders = document.getElementById('report-table-headers');
    const summaryCard = document.getElementById('report-summary-info');

    if (!tableBody || !tableHeaders) return;

    tableBody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 3rem; color: #64748b;">Generating report from live database...</td></tr>';

    try {
        let endpoint = '/reports/inventory';
        if (activeTab === 'low-stock') endpoint = '/reports/low-stock';
        if (activeTab === 'near-expiry') endpoint = '/reports/near-expiry';
        if (activeTab === 'expired') endpoint = '/reports/expired';
        if (activeTab === 'movements') endpoint = '/reports/movements';

        const res = await api.get(endpoint);
        if (res.data) {
            renderReportData(res.data);
        }
    } catch (err) {
        tableBody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: #ef4444; padding: 2rem;">Error compiling report.</td></tr>';
    }
}

function renderReportData(data) {
    const tableBody = document.getElementById('report-table-body');
    const tableHeaders = document.getElementById('report-table-headers');
    const summaryCard = document.getElementById('report-summary-info');

    if (summaryCard) {
        summaryCard.innerHTML = `
            <div style="display: flex; gap: 2rem; align-items: center; flex-wrap: wrap;">
                <div>
                    <span style="font-size: 0.85rem; color: #64748b;">Total Records:</span>
                    <strong style="font-size: 1.25rem; margin-left: 0.35rem; color: #0f172a;">${data.totalRecords}</strong>
                </div>
                ${data.totalValue && data.totalValue > 0 ? `
                    <div>
                        <span style="font-size: 0.85rem; color: #64748b;">Valuation / Financial Impact:</span>
                        <strong style="font-size: 1.25rem; margin-left: 0.35rem; color: #0284c7;">$${Number(data.totalValue).toFixed(2)}</strong>
                    </div>
                ` : ''}
                <div style="margin-left: auto;">
                    <button class="btn btn-primary btn-sm" onclick="exportCsv()">
                        📥 Export to CSV
                    </button>
                </div>
            </div>
        `;
    }

    if (activeTab === 'inventory') {
        tableHeaders.innerHTML = `
            <th>Medicine Name</th>
            <th>SKU</th>
            <th>Category</th>
            <th>Unit Price</th>
            <th>Eligible Stock</th>
            <th>Stock Valuation</th>
            <th>Status</th>
        `;

        tableBody.innerHTML = data.records.map(r => `
            <tr>
                <td><strong>${escapeHtml(r.name)}</strong></td>
                <td><code>${escapeHtml(r.skuCode)}</code></td>
                <td>${escapeHtml(r.categoryName)}</td>
                <td>$${Number(r.unitPrice).toFixed(2)}</td>
                <td><strong>${r.stock}</strong></td>
                <td><strong>$${Number(r.valuation).toFixed(2)}</strong></td>
                <td>
                    <span class="stock-tag ${r.status === 'OUT_OF_STOCK' ? 'stock-out' : (r.status === 'LOW_STOCK' ? 'stock-low' : 'stock-in')}">
                        ${r.status.replace(/_/g, ' ')}
                    </span>
                </td>
            </tr>
        `).join('');
    } else if (activeTab === 'low-stock') {
        tableHeaders.innerHTML = `
            <th>Medicine Name</th>
            <th>SKU</th>
            <th>Category</th>
            <th>Unit Price</th>
            <th>Current Stock</th>
            <th>Status</th>
        `;

        tableBody.innerHTML = data.records.map(r => `
            <tr>
                <td><strong>${escapeHtml(r.name)}</strong></td>
                <td><code>${escapeHtml(r.skuCode)}</code></td>
                <td>${escapeHtml(r.categoryName)}</td>
                <td>$${Number(r.unitPrice).toFixed(2)}</td>
                <td style="color: #ef4444; font-weight: 700;">${r.stock} units</td>
                <td><span class="stock-tag stock-low">${r.status}</span></td>
            </tr>
        `).join('');
    } else if (activeTab === 'near-expiry') {
        tableHeaders.innerHTML = `
            <th>Medicine Name</th>
            <th>Batch No.</th>
            <th>Expiry Date</th>
            <th>Days Remaining</th>
            <th>Available Stock</th>
            <th>Unit Cost</th>
            <th>Loss Risk</th>
        `;

        tableBody.innerHTML = data.records.map(b => {
            const risk = Number(b.purchaseCost * b.availableQuantity).toFixed(2);
            return `
                <tr>
                    <td><strong>${escapeHtml(b.medicineName)}</strong></td>
                    <td><code>${escapeHtml(b.batchNumber)}</code></td>
                    <td style="color: #f59e0b; font-weight: 700;">${b.expiryDate}</td>
                    <td><strong>${b.daysUntilExpiry} days</strong></td>
                    <td><strong>${b.availableQuantity}</strong></td>
                    <td>$${Number(b.purchaseCost).toFixed(2)}</td>
                    <td style="color: #b45309; font-weight: 700;">$${risk}</td>
                </tr>
            `;
        }).join('');
    } else if (activeTab === 'expired') {
        tableHeaders.innerHTML = `
            <th>Medicine Name</th>
            <th>Batch No.</th>
            <th>Expiry Date</th>
            <th>Days Expired</th>
            <th>Expired Stock</th>
            <th>Unit Cost</th>
            <th>Total Loss</th>
        `;

        tableBody.innerHTML = data.records.map(b => {
            const loss = Number(b.purchaseCost * b.availableQuantity).toFixed(2);
            return `
                <tr>
                    <td><strong>${escapeHtml(b.medicineName)}</strong></td>
                    <td><code>${escapeHtml(b.batchNumber)}</code></td>
                    <td style="color: #ef4444; font-weight: 700;">${b.expiryDate}</td>
                    <td><strong>${Math.abs(b.daysUntilExpiry)} days ago</strong></td>
                    <td style="color: #ef4444; font-weight: 700;">${b.availableQuantity}</td>
                    <td>$${Number(b.purchaseCost).toFixed(2)}</td>
                    <td style="color: #b91c1c; font-weight: 800;">$${loss}</td>
                </tr>
            `;
        }).join('');
    } else if (activeTab === 'movements') {
        tableHeaders.innerHTML = `
            <th>Timestamp</th>
            <th>Medicine & Batch</th>
            <th>Type</th>
            <th>Quantity</th>
            <th>Qty After</th>
            <th>Reference</th>
            <th>Reason</th>
            <th>User</th>
        `;

        tableBody.innerHTML = data.records.map(m => {
            const time = new Date(m.createdAt).toLocaleString();
            return `
                <tr>
                    <td><small>${time}</small></td>
                    <td><strong>${escapeHtml(m.medicineName)}</strong><br><small>Batch: ${escapeHtml(m.batchNumber)}</small></td>
                    <td><span class="status-badge status-confirmed">${m.movementType}</span></td>
                    <td><strong>${m.quantity}</strong></td>
                    <td>${m.quantityAfter}</td>
                    <td><code>${escapeHtml(m.referenceNumber || '')}</code></td>
                    <td>${escapeHtml(m.reason || '')}</td>
                    <td>${escapeHtml(m.createdBy || 'SYSTEM')}</td>
                </tr>
            `;
        }).join('');
    }
}

async function exportCsv() {
    let typeParam = 'INVENTORY';
    if (activeTab === 'low-stock') typeParam = 'LOW_STOCK';
    if (activeTab === 'near-expiry') typeParam = 'NEAR_EXPIRY';
    if (activeTab === 'expired') typeParam = 'EXPIRED';
    if (activeTab === 'movements') typeParam = 'MOVEMENTS';

    try {
        const token = api.getToken();
        const response = await fetch(`${API_BASE}/reports/export/csv?type=${typeParam}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Failed to generate CSV export');

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `pharmatrack-${typeParam.toLowerCase()}-report.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showToast('CSV report downloaded successfully', 'success');
    } catch (err) {
        showToast('Failed to export CSV: ' + err.message, 'error');
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
