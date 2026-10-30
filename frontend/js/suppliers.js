/**
 * PharmaTrack - Suppliers & Restocking (Purchase Orders) Management Script
 */

let allSuppliers = [];
let allPOs = [];
let allMedicines = [];

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('suppliers');
    if (!requireAuth(['ROLE_ADMIN', 'ROLE_STAFF'])) return;

    await loadSuppliers();
    await loadPurchaseOrders();
    await loadMedicines();

    const formSupplier = document.getElementById('form-supplier');
    if (formSupplier) formSupplier.addEventListener('submit', handleSaveSupplier);

    const formPO = document.getElementById('form-purchase-order');
    if (formPO) formPO.addEventListener('submit', handleCreatePO);
});

async function loadSuppliers() {
    const tbody = document.getElementById('suppliers-table-body');
    if (!tbody) return;

    try {
        const res = await api.get('/suppliers?activeOnly=false');
        if (res.data) {
            allSuppliers = res.data;
            renderSuppliersTable(allSuppliers);
            populateSupplierDropdown(allSuppliers);
        }
    } catch (err) {}
}

async function loadPurchaseOrders() {
    const tbody = document.getElementById('po-table-body');
    if (!tbody) return;

    try {
        const res = await api.get('/suppliers/purchase-orders');
        if (res.data) {
            allPOs = res.data;
            renderPOTable(allPOs);
        }
    } catch (err) {}
}

async function loadMedicines() {
    try {
        const res = await api.get('/medicines/admin/all');
        if (res.data) {
            allMedicines = res.data;
            const select = document.getElementById('po-med-select');
            if (select) {
                select.innerHTML = '<option value="">Select Medicine...</option>' +
                    allMedicines.map(m => `<option value="${m.id}">${m.name} (${m.skuCode})</option>`).join('');
            }
        }
    } catch (err) {}
}

function populateSupplierDropdown(suppliers) {
    const select = document.getElementById('po-supplier-select');
    if (select) {
        select.innerHTML = '<option value="">Select Supplier...</option>' +
            suppliers.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
    }
}

function renderSuppliersTable(suppliers) {
    const tbody = document.getElementById('suppliers-table-body');
    if (!tbody) return;

    if (suppliers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2rem;">No suppliers registered.</td></tr>';
        return;
    }

    tbody.innerHTML = suppliers.map(s => `
        <tr>
            <td><strong>#${s.id}</strong></td>
            <td><strong>${escapeHtml(s.name)}</strong></td>
            <td>${escapeHtml(s.contactPerson || 'N/A')}</td>
            <td>${escapeHtml(s.email || '')}<br><small>${escapeHtml(s.phone || '')}</small></td>
            <td>${escapeHtml(s.address || 'N/A')}</td>
            <td>
                <span class="status-badge ${s.active ? 'status-completed' : 'status-cancelled'}">
                    ${s.active ? 'Active' : 'Inactive'}
                </span>
            </td>
        </tr>
    `).join('');
}

function renderPOTable(orders) {
    const tbody = document.getElementById('po-table-body');
    if (!tbody) return;

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #64748b; padding: 2rem;">No purchase / restocking records.</td></tr>';
        return;
    }

    tbody.innerHTML = orders.map(po => {
        const isPending = po.status === 'PENDING';
        return `
            <tr>
                <td><strong>#${escapeHtml(po.poNumber)}</strong></td>
                <td>${escapeHtml(po.supplierName)}</td>
                <td>${po.orderDate}</td>
                <td>${po.receivedDate || '<span style="color: #64748b;">Pending Receipt</span>'}</td>
                <td><strong>$${Number(po.totalAmount).toFixed(2)}</strong></td>
                <td>
                    <span class="status-badge ${po.status === 'RECEIVED' ? 'status-completed' : 'status-pending'}">
                        ${po.status}
                    </span>
                </td>
                <td>
                    ${isPending ? `
                        <button class="btn btn-success btn-sm" onclick="receivePO(${po.id})">
                            ✓ Mark Received & Add Stock
                        </button>
                    ` : '<span style="color: #10b981; font-weight: 600;">Stock Added</span>'}
                </td>
            </tr>
        `;
    }).join('');
}

async function receivePO(poId) {
    if (!confirm('Mark this Purchase Order as Received? This will immediately add and record new batch stock to inventory.')) return;

    try {
        const res = await api.post(`/suppliers/purchase-orders/${poId}/receive`, {});
        if (res.success) {
            showToast('Stock received and batch inventory replenished!', 'success');
            await loadPurchaseOrders();
        }
    } catch (err) {}
}

function openSupplierModal() {
    document.getElementById('supplier-modal').style.display = 'flex';
}

function closeSupplierModal() {
    document.getElementById('supplier-modal').style.display = 'none';
}

async function handleSaveSupplier(e) {
    e.preventDefault();
    const name = document.getElementById('supp-name').value;
    const contactPerson = document.getElementById('supp-contact').value;
    const email = document.getElementById('supp-email').value;
    const phone = document.getElementById('supp-phone').value;
    const address = document.getElementById('supp-address').value;

    try {
        const res = await api.post('/suppliers', { name, contactPerson, email, phone, address, active: true });
        if (res.success) {
            showToast('Supplier registered successfully', 'success');
            closeSupplierModal();
            document.getElementById('form-supplier').reset();
            await loadSuppliers();
        }
    } catch (err) {}
}

function openPOModal() {
    document.getElementById('po-modal').style.display = 'flex';
}

function closePOModal() {
    document.getElementById('po-modal').style.display = 'none';
}

async function handleCreatePO(e) {
    e.preventDefault();
    const supplierId = document.getElementById('po-supplier-select').value;
    const medicineId = document.getElementById('po-med-select').value;
    const batchNumber = document.getElementById('po-batch-number').value;
    const manufacturingDate = document.getElementById('po-mfg-date').value;
    const expiryDate = document.getElementById('po-exp-date').value;
    const quantity = parseInt(document.getElementById('po-quantity').value, 10);
    const unitCost = parseFloat(document.getElementById('po-unit-cost').value);
    const notes = document.getElementById('po-notes')?.value;

    const payload = {
        supplierId: parseInt(supplierId, 10),
        notes,
        items: [{
            medicineId: parseInt(medicineId, 10),
            batchNumber,
            manufacturingDate,
            expiryDate,
            quantity,
            unitCost
        }]
    };

    try {
        const res = await api.post('/suppliers/purchase-orders', payload);
        if (res.success) {
            showToast('Purchase Order created. Note: Stock will be added upon delivery receipt.', 'info');
            closePOModal();
            document.getElementById('form-purchase-order').reset();
            await loadPurchaseOrders();
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
