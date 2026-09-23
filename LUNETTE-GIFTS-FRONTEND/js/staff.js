/**
 * LUNETTE GIFTS — STAFF / WORKER ORDER-ENTRY & MANAGEMENT JAVASCRIPT
 * Handles walk-in/phone order creation, customer lookup, permission checks,
 * status updates, inventory checks, and photo ZIP downloads.
 */

let staffToken = null;
let staffUser = null;
let staffCustomer = null;
let staffAllProducts = [];
let staffSelectedProduct = null;
let staffSelectedVariant = null;
let staffQuantity = 1;
let staffUploadedPhotos = []; // { slotIndex, fileUrl, originalFilename }
let staffCustomizationRef = null;

document.addEventListener('DOMContentLoaded', () => {
    checkStaffAuth();
    loadStaffProducts();
    loadStaffOrders();
    loadStaffInventory();
});

function checkStaffAuth() {
    staffToken = localStorage.getItem('lunette_token');
    const role = localStorage.getItem('lunette_role');

    if (!staffToken || (role !== 'ROLE_WORKER' && role !== 'ROLE_ADMIN')) {
        window.location.href = '/login.html?redirect=staff';
        return;
    }

    const username = localStorage.getItem('lunette_username') || 'Staff';
    const nameElem = document.getElementById('staff-username-display');
    if (nameElem) nameElem.textContent = username;
}

function staffHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${staffToken}`
    };
}

// 1. CUSTOMER LOOKUP OR QUICK CREATE
async function lookupCustomer() {
    const query = document.getElementById('staff-cust-search').value.trim();
    const resultDiv = document.getElementById('staff-cust-result');
    if (!query) return;

    resultDiv.innerHTML = '<span style="color: #786C68; font-size: 13px;">Searching customer...</span>';

    try {
        const res = await fetch(`/api/worker/customers/lookup?query=${encodeURIComponent(query)}`, {
            headers: staffHeaders()
        });

        if (res.ok) {
            staffCustomer = await res.json();
            resultDiv.innerHTML = `
                <div style="background: #EBF3ED; border: 1px solid #A3C2AC; padding: 12px 16px; border-radius: 8px; margin-top: 10px;">
                    <strong style="color: #5B7B66;">✓ Customer Found:</strong>
                    <div style="font-size: 13px; color: #2C2523; margin-top: 4px;">
                        <strong>${staffCustomer.fullName}</strong> (${staffCustomer.phone}) • ${staffCustomer.email || 'No email'}
                    </div>
                </div>
            `;
            // Fill shipping fields if available
            document.getElementById('staff-order-name').value = staffCustomer.fullName;
            document.getElementById('staff-order-phone').value = staffCustomer.phone;
            if (staffCustomer.email) document.getElementById('staff-order-email').value = staffCustomer.email;
        } else {
            resultDiv.innerHTML = `
                <div style="background: #FFF5F7; border: 1px solid #F7CCD6; padding: 12px 16px; border-radius: 8px; margin-top: 10px;">
                    <span style="color: #C46D7E; font-size: 13px;">Customer not found. Please enter details below to auto-register.</span>
                </div>
            `;
        }
    } catch (e) {
        resultDiv.innerHTML = '<span style="color: #A85364; font-size: 13px;">Lookup error.</span>';
    }
}

// 2. PRODUCT & VARIANT CONFIGURATION
async function loadStaffProducts() {
    try {
        const res = await fetch(apiUrl('/api/store/products'));
        staffAllProducts = await res.json();
        const select = document.getElementById('staff-product-select');
        if (!select) return;

        select.innerHTML = '<option value="">-- Choose Product --</option>' +
            staffAllProducts.map(p => `<option value="${p.id}">${p.name} (${p.category})</option>`).join('');
    } catch (e) {}
}

function onStaffProductChange(productId) {
    staffSelectedProduct = staffAllProducts.find(p => p.id == productId);
    const variantSelect = document.getElementById('staff-variant-select');
    const variantGroup = document.getElementById('staff-variant-group');

    staffUploadedPhotos = [];
    staffSelectedVariant = null;

    if (!staffSelectedProduct) {
        if (variantGroup) variantGroup.style.display = 'none';
        return;
    }

    staffQuantity = staffSelectedProduct.minQuantity || 1;
    document.getElementById('staff-qty-input').value = staffQuantity;

    if (staffSelectedProduct.variants && staffSelectedProduct.variants.length > 0) {
        if (variantGroup) variantGroup.style.display = 'block';
        variantSelect.innerHTML = staffSelectedProduct.variants.map(v =>
            `<option value="${v.id}">${v.name} — ₹${v.price} (${v.requiredPhotos} photos required)</option>`
        ).join('');
        staffSelectedVariant = staffSelectedProduct.variants[0];
    } else {
        if (variantGroup) variantGroup.style.display = 'none';
    }

    renderStaffPhotoSlots();
    calcStaffPrice();
}

function onStaffVariantChange(variantId) {
    if (!staffSelectedProduct) return;
    staffSelectedVariant = staffSelectedProduct.variants.find(v => v.id == variantId);
    staffUploadedPhotos = [];
    renderStaffPhotoSlots();
    calcStaffPrice();
}

function adjustStaffQty(delta) {
    if (!staffSelectedProduct) return;
    const min = staffSelectedProduct.minQuantity || 1;
    let step = (staffSelectedProduct.category !== 'PHOTO_FRAMES') ? 5 : 1;
    const newQty = staffQuantity + (delta * step);
    if (newQty >= min) {
        staffQuantity = newQty;
        document.getElementById('staff-qty-input').value = staffQuantity;
        renderStaffPhotoSlots();
        calcStaffPrice();
    }
}

function getStaffRequiredPhotos() {
    if (!staffSelectedProduct) return 1;
    if (staffSelectedProduct.category === 'PHOTO_FRAMES') {
        return staffSelectedVariant ? staffSelectedVariant.requiredPhotos : 1;
    }
    return staffQuantity;
}

function renderStaffPhotoSlots() {
    const grid = document.getElementById('staff-photo-slots');
    if (!grid || !staffSelectedProduct) return;

    const totalRequired = getStaffRequiredPhotos();
    let html = '';

    for (let i = 1; i <= totalRequired; i++) {
        const uploaded = staffUploadedPhotos.find(p => p.slotIndex === i);
        if (uploaded) {
            html += `
                <div class="photo-slot filled" style="height: 90px; width: 90px;">
                    <img src="${uploaded.fileUrl}" class="slot-preview" alt="Slot ${i}">
                    <div class="slot-actions">
                        <button class="slot-action-btn" onclick="removeStaffSlot(${i})">✕</button>
                    </div>
                </div>
            `;
        } else {
            html += `
                <label class="photo-slot" style="height: 90px; width: 90px;" id="staff-slot-${i}">
                    <input type="file" accept="image/*" style="display: none;" onchange="uploadStaffPhoto(${i}, this)">
                    <div class="slot-placeholder">
                        <span style="font-size: 16px;">📷</span>
                        <span>Photo ${i}</span>
                    </div>
                </label>
            `;
        }
    }

    grid.innerHTML = html;
    document.getElementById('staff-upload-counter').textContent = `${staffUploadedPhotos.length} / ${totalRequired} photos uploaded`;
}

async function uploadStaffPhoto(slotIndex, input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'staff_orders');

    try {
        const res = await fetch(apiUrl('/api/worker/upload'), {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${staffToken}` },
            body: formData
        });
        if (!res.ok) throw new Error('Upload failed');
        const data = await res.json();

        staffUploadedPhotos = staffUploadedPhotos.filter(p => p.slotIndex !== slotIndex);
        staffUploadedPhotos.push({
            slotIndex: slotIndex,
            fileUrl: data.fileUrl,
            originalFilename: data.originalFilename
        });

        renderStaffPhotoSlots();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function removeStaffSlot(slotIndex) {
    staffUploadedPhotos = staffUploadedPhotos.filter(p => p.slotIndex !== slotIndex);
    renderStaffPhotoSlots();
}

function calcStaffPrice() {
    if (!staffSelectedProduct) return;
    let base = staffSelectedVariant ? staffSelectedVariant.price : staffSelectedProduct.basePrice;

    let subtotal = 0;
    if (staffSelectedProduct.category === 'PHOTO_CARDS') {
        subtotal = base * staffQuantity;
    } else if (staffSelectedProduct.category === 'RING_ALBUMS') {
        subtotal = (staffSelectedProduct.ringPrice || 20) + (base * staffQuantity);
    } else if (staffSelectedProduct.category === 'LED_POLAROIDS') {
        subtotal = (staffSelectedProduct.lightPrice || 150) + (base * staffQuantity);
    } else {
        subtotal = base * staffQuantity;
    }

    const isCustom = document.getElementById('staff-customization-check').checked;
    const custPrice = isCustom ? (staffSelectedProduct.customizationPrice || 0) : 0;
    subtotal += custPrice;

    const isMadurai = document.getElementById('staff-zone-select').value === 'MADURAI';
    const delivery = isMadurai ? 70 : 100;
    const total = subtotal + delivery;

    document.getElementById('staff-order-subtotal').textContent = `₹${subtotal}`;
    document.getElementById('staff-order-delivery').textContent = `₹${delivery}`;
    document.getElementById('staff-order-total').textContent = `₹${total}`;
}

// 3. SUBMIT STAFF ORDER
async function submitStaffOrder(event) {
    if (event) event.preventDefault();

    if (!staffSelectedProduct) {
        showToast('Please select a product', 'error');
        return;
    }

    const totalRequired = getStaffRequiredPhotos();
    if (staffUploadedPhotos.length < totalRequired) {
        showToast(`Please upload all ${totalRequired} photos for this order`, 'error');
        return;
    }

    const name = document.getElementById('staff-order-name').value.trim();
    const phone = document.getElementById('staff-order-phone').value.trim();
    const email = document.getElementById('staff-order-email').value.trim();
    const address = document.getElementById('staff-order-address').value.trim();
    const pin = document.getElementById('staff-order-pin').value.trim();
    const zone = document.getElementById('staff-zone-select').value;
    const paymentStatus = document.getElementById('staff-payment-status-select').value;
    const paymentRef = document.getElementById('staff-payment-ref').value.trim();
    const notes = document.getElementById('staff-order-notes').value.trim();

    const isCustom = document.getElementById('staff-customization-check').checked;
    const instructions = document.getElementById('staff-custom-instructions').value.trim();

    if (!name || !phone || !address || !pin) {
        showToast('Please enter customer name, phone, address, and PIN code', 'error');
        return;
    }

    const payload = {
        customerName: name,
        customerPhone: phone,
        customerEmail: email,
        shippingAddress: address,
        pinCode: pin,
        deliveryZone: zone,
        orderNotes: notes,
        paymentReference: paymentRef,
        items: [{
            productId: staffSelectedProduct.id,
            variantId: staffSelectedVariant ? staffSelectedVariant.id : null,
            variantName: staffSelectedVariant ? staffSelectedVariant.name : 'Standard',
            quantity: staffQuantity,
            customizationSelected: isCustom,
            customizationInstructions: instructions,
            photoUrls: staffUploadedPhotos.sort((a,b) => a.slotIndex - b.slotIndex).map(p => p.fileUrl),
            originalFilenames: staffUploadedPhotos.sort((a,b) => a.slotIndex - b.slotIndex).map(p => p.originalFilename)
        }]
    };

    try {
        const res = await fetch(apiUrl('/api/worker/orders'), {
            method: 'POST',
            headers: staffHeaders(),
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || 'Failed to create order');
        }

        const data = await res.json();
        showToast(`Order #${data.orderNumber} created successfully! 🌸`);

        // If payment was marked confirmed, update status
        if (paymentStatus === 'CONFIRMED') {
            await fetch(`/api/worker/orders/${data.orderId}/status`, {
                method: 'PATCH',
                headers: staffHeaders(),
                body: JSON.stringify({ status: 'PAYMENT_CONFIRMED', notes: 'Payment verified by staff at counter' })
            });
        }

        resetStaffForm();
        loadStaffOrders();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function resetStaffForm() {
    staffSelectedProduct = null;
    staffSelectedVariant = null;
    staffUploadedPhotos = [];
    document.getElementById('staff-order-form').reset();
    document.getElementById('staff-variant-group').style.display = 'none';
    document.getElementById('staff-photo-slots').innerHTML = '';
}

// 4. ORDERS LIST & STATUS UPDATES
async function loadStaffOrders() {
    const tableBody = document.getElementById('staff-orders-tbody');
    if (!tableBody) return;

    try {
        const res = await fetch(apiUrl('/api/worker/orders'), { headers: staffHeaders() });
        if (!res.ok) throw new Error('Could not load orders');
        const orders = await res.json();

        if (orders.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 20px;">No orders found.</td></tr>';
            return;
        }

        tableBody.innerHTML = orders.map(o => `
            <tr>
                <td><strong>${o.orderNumber}</strong></td>
                <td>${o.customerName}<br><small style="color: #786C68;">${o.customerPhone}</small></td>
                <td>${o.items ? o.items.length : 0} item(s)</td>
                <td><strong>₹${o.totalAmount}</strong><br><small>${o.deliveryZone}</small></td>
                <td><span class="status-badge ${getStatusBadgeClass(o.orderStatus)}">${o.orderStatus}</span></td>
                <td>${o.paymentStatus === 'CONFIRMED' ? '<span style="color: #155724; font-weight: 600;">Paid</span>' : '<span style="color: #856404; font-weight: 600;">Pending</span>'}</td>
                <td>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn-action" onclick="openStaffStatusModal(${o.id}, '${o.orderStatus}')">Status</button>
                        <button class="btn-action btn-zip" title="Download Photos ZIP" onclick="downloadStaffFile('/api/worker/orders/' + o.id + '/download-zip', 'ORDER_' + o.orderNumber + '.zip')">ZIP</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #A85364; padding: 20px;">Failed to load orders.</td></tr>';
    }
}

function openStaffStatusModal(orderId, currentStatus) {
    const modal = document.getElementById('staff-status-modal');
    document.getElementById('staff-status-order-id').value = orderId;
    document.getElementById('staff-new-status-select').value = currentStatus;
    if (modal) modal.classList.add('active');
}

function closeStaffStatusModal() {
    const modal = document.getElementById('staff-status-modal');
    if (modal) modal.classList.remove('active');
}

async function updateStaffOrderStatus() {
    const orderId = document.getElementById('staff-status-order-id').value;
    const newStatus = document.getElementById('staff-new-status-select').value;
    const notes = document.getElementById('staff-status-notes').value.trim();

    try {
        const res = await fetch(`/api/worker/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: staffHeaders(),
            body: JSON.stringify({ status: newStatus, notes: notes })
        });
        if (!res.ok) throw new Error('Failed to update status');

        showToast('Order status updated!');
        closeStaffStatusModal();
        loadStaffOrders();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

// 5. INVENTORY TABLE
async function loadStaffInventory() {
    const tbody = document.getElementById('staff-inventory-tbody');
    if (!tbody) return;

    try {
        const res = await fetch(apiUrl('/api/worker/inventory'), { headers: staffHeaders() });
        if (!res.ok) return;
        const products = await res.json();

        tbody.innerHTML = products.map(p => {
            const isLow = p.stockQuantity <= p.lowStockThreshold;
            return `
                <tr>
                    <td><strong>${p.name}</strong></td>
                    <td>${p.category}</td>
                    <td>₹${p.basePrice}</td>
                    <td><strong style="color: ${isLow ? '#A85364' : '#2C2523'};">${p.stockQuantity}</strong></td>
                    <td>
                        <span class="status-badge ${isLow ? 'cancelled' : 'confirmed'}">
                            ${isLow ? 'Low Stock' : 'In Stock'}
                        </span>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (e) {}
}

function getStatusBadgeClass(status) {
    switch (status) {
        case 'ORDER_PLACED': return 'placed';
        case 'PAYMENT_PENDING': return 'pending';
        case 'PAYMENT_CONFIRMED': return 'confirmed';
        case 'IN_PRODUCTION': return 'production';
        case 'READY': return 'ready';
        case 'DELIVERED': return 'delivered';
        default: return 'pending';
    }
}

function staffLogout() {
    fetch(apiUrl('/api/auth/logout'), { method: 'POST', headers: staffHeaders() }).finally(() => {
        localStorage.removeItem('lunette_token');
        localStorage.removeItem('lunette_role');
        localStorage.removeItem('lunette_username');
        window.location.href = '/login.html';
    });
}


async function downloadStaffFile(endpoint, filename) {
    try {
        const res = await fetch(apiUrl(endpoint), { headers: staffHeaders() });
        if (!res.ok) throw new Error('Download failed');
        const blob = await res.blob();
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
    } catch (error) {
        showToast(error.message || 'Download failed', 'error');
    }
}
