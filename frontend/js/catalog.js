/**
 * PharmaTrack - Customer Medicine Search & Catalog Script
 */

let allMedicines = [];
let selectedMedicine = null;

document.addEventListener('DOMContentLoaded', async () => {
    renderNavbar('home');
    await loadCategories();
    await loadMedicines();

    const searchInput = document.getElementById('search-query');
    const categorySelect = document.getElementById('category-filter');
    const inStockToggle = document.getElementById('instock-filter');

    if (searchInput) {
        let debounceTimer;
        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => filterMedicines(), 300);
        });
    }

    if (categorySelect) categorySelect.addEventListener('change', filterMedicines);
    if (inStockToggle) inStockToggle.addEventListener('change', filterMedicines);
});

async function loadCategories() {
    try {
        const res = await api.get('/categories');
        const select = document.getElementById('category-filter');
        if (!select || !res.data) return;

        select.innerHTML = '<option value="">All Categories</option>' +
            res.data.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    } catch (err) {
        console.error('Failed to load categories', err);
    }
}

async function loadMedicines() {
    const grid = document.getElementById('catalog-grid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: #64748b;">Loading available medications...</div>';

    try {
        const res = await api.get('/medicines');
        if (res.data) {
            allMedicines = res.data;
            renderMedicineGrid(allMedicines);
        }
    } catch (err) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: #ef4444;">Unable to load medicines. Please ensure the backend is running.</div>';
    }
}

function filterMedicines() {
    const query = (document.getElementById('search-query')?.value || '').toLowerCase().trim();
    const categoryId = document.getElementById('category-filter')?.value;
    const inStockOnly = document.getElementById('instock-filter')?.checked;

    const filtered = allMedicines.filter(med => {
        const matchesQuery = !query || 
            med.name.toLowerCase().includes(query) || 
            (med.genericName && med.genericName.toLowerCase().includes(query)) ||
            med.manufacturer.toLowerCase().includes(query);

        const matchesCategory = !categoryId || med.categoryId == categoryId;
        const matchesStock = !inStockOnly || med.available;

        return matchesQuery && matchesCategory && matchesStock;
    });

    renderMedicineGrid(filtered);
}

function renderMedicineGrid(medicines) {
    const grid = document.getElementById('catalog-grid');
    if (!grid) return;

    if (medicines.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 4rem 1rem; background: #ffffff; border-radius: 12px; border: 1px dashed #cbd5e1;">
                <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">🔍</div>
                <h3 style="margin-bottom: 0.25rem;">No Medications Found</h3>
                <p style="color: #64748b;">Try adjusting your search terms or clearing the filters.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = medicines.map(med => {
        let stockBadge = '';
        if (!med.available) {
            stockBadge = `<span class="stock-tag stock-out">Out of Stock</span>`;
        } else if (med.stockStatus === 'LOW_STOCK') {
            stockBadge = `<span class="stock-tag stock-low">Only ${med.totalEligibleStock} left</span>`;
        } else {
            stockBadge = `<span class="stock-tag stock-in">In Stock (${med.totalEligibleStock})</span>`;
        }

        const rxBadge = med.prescriptionRequired ? 
            `<span class="rx-badge">Rx Required</span>` : 
            `<span class="otc-badge">OTC</span>`;

        return `
            <div class="medicine-card">
                <div class="medicine-badge-top">
                    ${rxBadge}
                    ${stockBadge}
                </div>
                <div class="medicine-image">
                    💊
                </div>
                <div class="medicine-body">
                    <div class="medicine-category">${escapeHtml(med.categoryName)}</div>
                    <h3 class="medicine-name">${escapeHtml(med.name)}</h3>
                    <div class="medicine-generic">${escapeHtml(med.genericName || '')}</div>
                    <div class="medicine-mfr">Mfr: ${escapeHtml(med.manufacturer)}</div>
                    <p class="medicine-desc">${escapeHtml(med.description ? med.description.substring(0, 85) + '...' : '')}</p>
                    
                    <div class="medicine-footer">
                        <div>
                            <span class="medicine-price">$${Number(med.unitPrice).toFixed(2)}</span>
                            <span style="font-size: 0.8rem; color: #64748b;"> / ${escapeHtml(med.dosageForm || 'Unit')}</span>
                        </div>
                        <div style="display: flex; gap: 0.5rem;">
                            <button class="btn btn-outline btn-sm" onclick="openMedicineModal(${med.id})">Details</button>
                            <button class="btn btn-primary btn-sm" 
                                ${!med.available ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''} 
                                onclick="addToCart(${med.id})">
                                + Add
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function openMedicineModal(id) {
    const med = allMedicines.find(m => m.id === id);
    if (!med) return;
    selectedMedicine = med;

    document.getElementById('modal-med-name').innerText = med.name;
    document.getElementById('modal-med-category').innerText = med.categoryName;
    document.getElementById('modal-med-generic').innerText = med.genericName || 'None listed';
    document.getElementById('modal-med-sku').innerText = med.skuCode;
    document.getElementById('modal-med-mfr').innerText = med.manufacturer;
    document.getElementById('modal-med-dosage').innerText = med.dosageForm || 'Tablet';
    document.getElementById('modal-med-price').innerText = '$' + Number(med.unitPrice).toFixed(2);
    document.getElementById('modal-med-stock').innerText = med.totalEligibleStock + ' units';
    document.getElementById('modal-med-desc').innerText = med.description || 'No additional clinical information.';

    const rxNotice = document.getElementById('modal-med-rx-notice');
    if (rxNotice) {
        rxNotice.style.display = med.prescriptionRequired ? 'block' : 'none';
    }

    const modalBtn = document.getElementById('modal-add-cart-btn');
    if (modalBtn) {
        modalBtn.disabled = !med.available;
        modalBtn.style.opacity = med.available ? '1' : '0.5';
    }

    document.getElementById('medicine-detail-modal').style.display = 'flex';
}

function closeMedicineModal() {
    document.getElementById('medicine-detail-modal').style.display = 'none';
}

function addToCartFromModal() {
    if (selectedMedicine && selectedMedicine.available) {
        addToCart(selectedMedicine.id);
        closeMedicineModal();
    }
}

// Cart Storage Helper
function addToCart(medicineId) {
    const med = allMedicines.find(m => m.id === medicineId);
    if (!med || !med.available) {
        showToast('Medicine is out of stock', 'warning');
        return;
    }

    let cart = JSON.parse(localStorage.getItem('pharmatrack_cart') || '[]');
    const existing = cart.find(item => item.medicineId === medicineId);

    if (existing) {
        if (existing.quantity >= med.totalEligibleStock) {
            showToast(`Cannot add more. Only ${med.totalEligibleStock} units available.`, 'warning');
            return;
        }
        existing.quantity += 1;
    } else {
        cart.push({
            medicineId: med.id,
            medicineName: med.name,
            unitPrice: med.unitPrice,
            quantity: 1,
            prescriptionRequired: med.prescriptionRequired
        });
    }

    localStorage.setItem('pharmatrack_cart', JSON.stringify(cart));
    showToast(`Added "${med.name}" to your cart`, 'success');

    // Update nav badge count
    const badge = document.getElementById('nav-cart-badge');
    if (badge) {
        const count = cart.reduce((sum, item) => sum + item.quantity, 0);
        badge.innerText = count;
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
