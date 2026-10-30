/**
 * PharmaTrack - Inventory, Batches, FEFO & Expiry Management Script
 */

let allBatches = [];
let batchForAdjustment = null;

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('batches');
    if (!requireAuth(['ROLE_ADMIN', 'ROLE_STAFF'])) return;

    await loadSuppliersDropdown();
    await loadMedicinesDropdown();
    await loadBatches();

    // Check query params for instant filter
    const urlParams = new URLSearchParams(window.location.search);
    const filterParam = urlParams.get('filter');
    if (filterParam) {
        const filterSelect = document.getElementById('batch-status-filter');
        if (filterSelect) {
            filterSelect.value = filterParam.toUpperCase();
            filterBatches();
        }
    }

    const searchInput = document.getElementById('batch-search');
    if (searchInput) searchInput.addEventListener('input', filterBatches);

    const filterSelect = document.getElementById('batch-status-filter');
    if (filterSelect) filterSelect.addEventListener('change', filterBatches);

    const formNewBatch = document.getElementById('form-new-batch');
    if (formNewBatch) formNewBatch.addEventListener('submit', handleCreateBatch);

    const formAdjustment = document.getElementById('form-stock-adjustment');
    if (formAdjustment) formAdjustment.addEventListener('submit', handleStockAdjustment);
});

async function loadBatches() {
    const tbody = document.getElementById('batches-table-body');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; padding: 3rem; color: #64748b;">Loading batches and inventory records...</td></tr>';

    try {
        const res = await api.get('/inventory/batches');
        if (res.data) {
            allBatches = res.data;
            filterBatches();
        }
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: #ef4444; padding: 2rem;">Failed to load batches.</td></tr>';
    }
}

async function loadMedicinesDropdown() {
    try {
        const res = await api.get('/medicines/admin/all');
        const select = document.getElementById('new-batch-medicine');
        if (!select || !res.data) return;

        select.innerHTML = '<option value="">Select Medicine...</option>' +
            res.data.map(m => `<option value="${m.id}">${m.name} (${m.skuCode})</option>`).join('');
    } catch (err) {}
}

async function loadSuppliersDropdown() {
    try {
        const res = await api.get('/suppliers');
        const select = document.getElementById('new-batch-supplier');
        if (!select || !res.data) return;

        select.innerHTML = '<option value="">Select Supplier...</option>' +
            res.data.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
    } catch (err) {}
}

function filterBatches() {
    const query = (document.getElementById('batch-search')?.value || '').toLowerCase().trim();
    const statusFilter = document.getElementById('batch-status-filter')?.value || '';

    const filtered = allBatches.filter(b => {
        const matchesQuery = !query ||
            b.medicineName.toLowerCase().includes(query) ||
            b.batchNumber.toLowerCase().includes(query) ||
            (b.supplierName && b.supplierName.toLowerCase().includes(query));

        let matchesStatus = true;
        if (statusFilter === 'EXPIRED') {
            matchesStatus = b.expired;
        } else if (statusFilter === 'NEAR_EXPIRY' || statusFilter === 'NEAR-EXPIRY') {
            matchesStatus = b.nearExpiry;
        } else if (statusFilter === 'ACTIVE') {
            matchesStatus = (!b.expired && !b.nearExpiry && b.availableQuantity > 0);
        } else if (statusFilter === 'DEPLETED') {
            matchesStatus = (b.availableQuantity <= 0);
        }

        return matchesQuery && matchesStatus;
    });

    renderBatchesTable(filtered);
}

function renderBatchesTable(batches) {
    const tbody = document.getElementById('batches-table-body');
    if (!tbody) return;

    if (batches.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; padding: 3rem; color: #64748b;">No batches found matching criteria.</td></tr>';
        return;
    }

    tbody.innerHTML = batches.map(b => {
        let statusBadge = '';
        if (b.expired) {
            statusBadge = `<span class="stock-tag stock-out">EXPIRED (${Math.abs(b.daysUntilExpiry)} days ago)</span>`;
        } else if (b.nearExpiry) {
            statusBadge = `<span class="stock-tag stock-low">NEAR EXPIRY (${b.daysUntilExpiry} days left)</span>`;
        } else if (b.availableQuantity <= 0) {
            statusBadge = `<span class="status-badge" style="background: #e2e8f0; color: #475569;">DEPLETED</span>`;
        } else {
            statusBadge = `<span class="stock-tag stock-in">ACTIVE (${b.daysUntilExpiry} days left)</span>`;
        }

        return `
            <tr>
                <td><strong>#${b.id}</strong></td>
                <td>
                    <div style="font-weight: 600; color: #0f172a;">${escapeHtml(b.medicineName)}</div>
                    <div style="font-size: 0.78rem; color: #64748b;">${escapeHtml(b.medicineSku || '')}</div>
                </td>
                <td><code>${escapeHtml(b.batchNumber)}</code></td>
                <td>${b.manufacturingDate}</td>
                <td><strong>${b.expiryDate}</strong></td>
                <td><strong>${b.availableQuantity}</strong> / ${b.quantityReceived}</td>
                <td>$${Number(b.purchaseCost).toFixed(2)}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="openAdjustmentModal(${b.id})">Adjust Stock</button>
                </td>
            </tr>
        `;
    }).join('');
}

// Create Batch
function openNewBatchModal() {
    document.getElementById('new-batch-modal').style.display = 'flex';
}

function closeNewBatchModal() {
    document.getElementById('new-batch-modal').style.display = 'none';
}

async function handleCreateBatch(e) {
    e.preventDefault();

    const medicineId = document.getElementById('new-batch-medicine').value;
    const batchNumber = document.getElementById('new-batch-number').value;
    const manufacturingDate = document.getElementById('new-batch-mfg').value;
    const expiryDate = document.getElementById('new-batch-exp').value;
    const quantityReceived = parseInt(document.getElementById('new-batch-qty').value, 10);
    const purchaseCost = parseFloat(document.getElementById('new-batch-cost').value);
    const supplierId = document.getElementById('new-batch-supplier').value || null;

    if (new Date(expiryDate) <= new Date(manufacturingDate)) {
        showToast('Expiry date must be after manufacturing date', 'error');
        return;
    }

    const payload = {
        medicineId: parseInt(medicineId, 10),
        batchNumber,
        manufacturingDate,
        expiryDate,
        quantityReceived,
        purchaseCost,
        supplierId: supplierId ? parseInt(supplierId, 10) : null
    };

    try {
        const res = await api.post('/inventory/batches', payload);
        if (res.success) {
            showToast('Batch added and stock recorded into FEFO inventory', 'success');
            closeNewBatchModal();
            document.getElementById('form-new-batch').reset();
            await loadBatches();
        }
    } catch (err) {}
}

// Stock Adjustment
function openAdjustmentModal(batchId) {
    const batch = allBatches.find(b => b.id === batchId);
    if (!batch) return;
    batchForAdjustment = batch;

    document.getElementById('adj-modal-batch-info').innerText =
        `${batch.medicineName} (Batch: ${batch.batchNumber}, Available: ${batch.availableQuantity})`;

    document.getElementById('stock-adjustment-modal').style.display = 'flex';
}

function closeAdjustmentModal() {
    batchForAdjustment = null;
    document.getElementById('stock-adjustment-modal').style.display = 'none';
}

async function handleStockAdjustment(e) {
    e.preventDefault();
    if (!batchForAdjustment) return;

    const adjustmentType = document.getElementById('adj-type').value;
    const quantity = parseInt(document.getElementById('adj-qty').value, 10);
    const reason = document.getElementById('adj-reason').value;
    const notes = document.getElementById('adj-notes')?.value;

    if (!reason || reason.trim() === '') {
        showToast('Reason is strictly mandatory for stock adjustment audits', 'error');
        return;
    }

    const payload = {
        batchId: batchForAdjustment.id,
        adjustmentType,
        quantity,
        reason,
        notes
    };

    try {
        const res = await api.post('/inventory/adjustments', payload);
        if (res.success) {
            showToast('Stock adjusted and movement logged to audit trail', 'success');
            closeAdjustmentModal();
            document.getElementById('form-stock-adjustment').reset();
            await loadBatches();
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
