/**
 * PharmaTrack - Admin Medicine & Category Management Script
 */

let allMedicines = [];
let allCategories = [];

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('medicines');
    if (!requireAuth(['ROLE_ADMIN', 'ROLE_STAFF'])) return;

    await loadCategories();
    await loadMedicines();

    const searchInput = document.getElementById('med-search');
    if (searchInput) searchInput.addEventListener('input', filterMedicines);

    const formMedicine = document.getElementById('form-medicine');
    if (formMedicine) formMedicine.addEventListener('submit', handleSaveMedicine);

    const formCategory = document.getElementById('form-category');
    if (formCategory) formCategory.addEventListener('submit', handleSaveCategory);
});

async function loadCategories() {
    try {
        const res = await api.get('/categories/all');
        if (res.data) {
            allCategories = res.data;
            populateCategoryDropdown(allCategories);
            renderCategoryList(allCategories);
        }
    } catch (err) {}
}

async function loadMedicines() {
    const tbody = document.getElementById('medicines-table-body');
    if (!tbody) return;

    try {
        const res = await api.get('/medicines/admin/all');
        if (res.data) {
            allMedicines = res.data;
            renderMedicinesTable(allMedicines);
        }
    } catch (err) {}
}

function filterMedicines() {
    const query = (document.getElementById('med-search')?.value || '').toLowerCase().trim();
    const filtered = allMedicines.filter(m =>
        !query ||
        m.name.toLowerCase().includes(query) ||
        m.skuCode.toLowerCase().includes(query) ||
        (m.genericName && m.genericName.toLowerCase().includes(query)) ||
        m.manufacturer.toLowerCase().includes(query)
    );
    renderMedicinesTable(filtered);
}

function renderMedicinesTable(medicines) {
    const tbody = document.getElementById('medicines-table-body');
    if (!tbody) return;

    if (medicines.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" style="text-align: center; color: #64748b; padding: 2rem;">No medicines found.</td></tr>';
        return;
    }

    tbody.innerHTML = medicines.map(m => {
        let stockColor = '#10b981';
        if (m.totalEligibleStock <= 0) stockColor = '#ef4444';
        else if (m.totalEligibleStock <= m.minStockThreshold) stockColor = '#f59e0b';

        return `
            <tr>
                <td><code>${escapeHtml(m.skuCode)}</code></td>
                <td>
                    <strong>${escapeHtml(m.name)}</strong>
                    <div style="font-size: 0.8rem; color: #64748b;">${escapeHtml(m.genericName || '')}</div>
                </td>
                <td>${escapeHtml(m.categoryName)}</td>
                <td>${escapeHtml(m.manufacturer)}</td>
                <td><strong>$${Number(m.unitPrice).toFixed(2)}</strong></td>
                <td>${m.minStockThreshold} units</td>
                <td style="color: ${stockColor}; font-weight: 700;">
                    ${m.totalEligibleStock} units
                </td>
                <td>
                    ${m.prescriptionRequired ? '<span class="rx-badge" style="font-size: 0.7rem;">Rx</span>' : '<span class="otc-badge" style="font-size: 0.7rem;">OTC</span>'}
                </td>
                <td>
                    <span class="status-badge ${m.active ? 'status-completed' : 'status-cancelled'}">
                        ${m.active ? 'Active' : 'Inactive'}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 0.35rem;">
                        <button class="btn btn-outline btn-sm" onclick="editMedicine(${m.id})">Edit</button>
                        <button class="btn btn-outline btn-sm" style="color: ${m.active ? '#ef4444' : '#10b981'};" onclick="toggleStatus(${m.id})">
                            ${m.active ? 'Deactivate' : 'Activate'}
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function populateCategoryDropdown(categories) {
    const select = document.getElementById('med-form-category');
    if (!select) return;

    select.innerHTML = '<option value="">Select Category...</option>' +
        categories.filter(c => c.active).map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

function renderCategoryList(categories) {
    const list = document.getElementById('category-list-container');
    if (!list) return;

    list.innerHTML = categories.map(c => `
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0; border-bottom: 1px solid #e2e8f0; font-size: 0.85rem;">
            <div>
                <strong>${escapeHtml(c.name)}</strong>
                <div style="color: #64748b;">${escapeHtml(c.description || '')}</div>
            </div>
            <span class="status-badge ${c.active ? 'status-completed' : 'status-cancelled'}">${c.active ? 'Active' : 'Inactive'}</span>
        </div>
    `).join('');
}

// Modal handling
function openMedicineModal() {
    document.getElementById('edit-med-id').value = '';
    document.getElementById('med-modal-title').innerText = 'Add New Medicine';
    document.getElementById('form-medicine').reset();
    document.getElementById('medicine-form-modal').style.display = 'flex';
}

function editMedicine(id) {
    const m = allMedicines.find(item => item.id === id);
    if (!m) return;

    document.getElementById('edit-med-id').value = m.id;
    document.getElementById('med-modal-title').innerText = 'Edit Medicine: ' + m.name;
    document.getElementById('med-form-name').value = m.name;
    document.getElementById('med-form-generic').value = m.genericName || '';
    document.getElementById('med-form-sku').value = m.skuCode;
    document.getElementById('med-form-category').value = m.categoryId;
    document.getElementById('med-form-mfr').value = m.manufacturer;
    document.getElementById('med-form-dosage').value = m.dosageForm || 'Tablet';
    document.getElementById('med-form-price').value = m.unitPrice;
    document.getElementById('med-form-threshold').value = m.minStockThreshold;
    document.getElementById('med-form-desc').value = m.description || '';
    document.getElementById('med-form-rx').checked = m.prescriptionRequired;

    document.getElementById('medicine-form-modal').style.display = 'flex';
}

function closeMedicineFormModal() {
    document.getElementById('medicine-form-modal').style.display = 'none';
}

function openCategoryModal() {
    document.getElementById('category-modal').style.display = 'flex';
}

function closeCategoryModal() {
    document.getElementById('category-modal').style.display = 'none';
}

async function handleSaveMedicine(e) {
    e.preventDefault();
    const editId = document.getElementById('edit-med-id').value;

    const payload = {
        name: document.getElementById('med-form-name').value,
        genericName: document.getElementById('med-form-generic').value,
        skuCode: document.getElementById('med-form-sku').value,
        categoryId: parseInt(document.getElementById('med-form-category').value, 10),
        manufacturer: document.getElementById('med-form-mfr').value,
        dosageForm: document.getElementById('med-form-dosage').value,
        unitPrice: parseFloat(document.getElementById('med-form-price').value),
        minStockThreshold: parseInt(document.getElementById('med-form-threshold').value, 10),
        description: document.getElementById('med-form-desc').value,
        prescriptionRequired: document.getElementById('med-form-rx').checked,
        active: true
    };

    try {
        let res;
        if (editId) {
            res = await api.put(`/medicines/${editId}`, payload);
            showToast('Medicine updated successfully', 'success');
        } else {
            res = await api.post('/medicines', payload);
            showToast('Medicine created successfully', 'success');
        }

        if (res.success) {
            closeMedicineFormModal();
            await loadMedicines();
        }
    } catch (err) {}
}

async function handleSaveCategory(e) {
    e.preventDefault();
    const name = document.getElementById('cat-name').value;
    const description = document.getElementById('cat-desc').value;

    try {
        const res = await api.post('/categories', { name, description, active: true });
        if (res.success) {
            showToast('Category created', 'success');
            document.getElementById('form-category').reset();
            await loadCategories();
        }
    } catch (err) {}
}

async function toggleStatus(id) {
    try {
        const res = await api.patch(`/medicines/${id}/toggle-status`);
        if (res.success) {
            showToast('Status toggled', 'info');
            await loadMedicines();
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
