/**
 * LUNETTE GIFTS — OWNER / ADMIN DASHBOARD JAVASCRIPT
 * Beginner-friendly dashboard, order management with ZIP photo download,
 * frame variant photo count configuration, inventory audit trail,
 * append-only worker monitoring, expenses & profit calculation,
 * smart suggestions, and report CSV exports.
 */

let adminToken = null;
let currentPeriod = 'today';
let dashboardData = null;

document.addEventListener('DOMContentLoaded', () => {
    checkAdminAuth();
    loadDashboardSummary();
    loadOrders();
    loadProductsAndVariants();
    loadInventory();
    loadWorkers();
    loadWorkerActivities('TODAY');
    loadWorkerPerformance();
    loadExpenses();
    loadCustomers();
    loadReviews();
    loadCoupons();
    loadInsights();
    loadSettings();
    setupTabNavigation();
});

function checkAdminAuth() {
    adminToken = localStorage.getItem('lunette_token');
    const role = localStorage.getItem('lunette_role');

    if (!adminToken || role !== 'ROLE_ADMIN') {
        window.location.href = '/login.html?redirect=admin';
        return;
    }

    const username = localStorage.getItem('lunette_username') || 'Owner';
    const tag = document.getElementById('admin-user-tag');

    if (tag) {
        tag.textContent = username + ' (Owner)';
    }
}

function adminHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminToken}`
    };
}


/* =========================================================
   ORDER STATUS BADGE CLASS
   ========================================================= */

function getStatusBadgeClass(status) {
    switch (status) {
        case 'ORDER_PLACED':
            return 'status-placed';

        case 'PAYMENT_CONFIRMED':
            return 'status-confirmed';

        case 'IN_PRODUCTION':
            return 'status-production';

        case 'READY_FOR_DELIVERY':
            return 'status-ready';

        case 'OUT_FOR_DELIVERY':
            return 'status-delivery';

        case 'DELIVERED':
            return 'status-delivered';

        case 'CANCELLED':
            return 'status-cancelled';

        default:
            return '';
    }
}


// =========================================================
// 1. TAB NAVIGATION
// =========================================================

function setupTabNavigation() {
    const links = document.querySelectorAll('.sidebar-link[data-tab]');

    links.forEach(link => {
        link.addEventListener('click', () => {

            links.forEach(l => l.classList.remove('active'));

            link.classList.add('active');

            const tabId = link.getAttribute('data-tab');

            document.querySelectorAll('.tab-pane').forEach(pane => {
                pane.style.display = pane.id === tabId ? 'block' : 'none';
            });
        });
    });
}

function switchTab(tabId) {
    const link = document.querySelector(
        `.sidebar-link[data-tab="${tabId}"]`
    );

    if (link) {
        link.click();
    }
}


// =========================================================
// 2. DASHBOARD SUMMARY & METRICS
// =========================================================

async function loadDashboardSummary() {
    try {
        const res = await fetch(
            apiUrl('/api/admin/dashboard'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) return;

        dashboardData = await res.json();

        renderAttentionBanner(dashboardData.attentionNeeded);
        renderPeriodMetrics(currentPeriod);

    } catch (e) {
        console.error('Dashboard summary error:', e);
    }
}

function renderAttentionBanner(items) {
    const container = document.getElementById(
        'attention-items-container'
    );

    if (!container || !items) return;

    container.innerHTML = items.map(item => `
        <div class="attention-chip"
             onclick="handleAttentionClick('${item.type}')">

            <span>${item.message}</span>

            <span class="attention-count">
                ${item.count}
            </span>

        </div>
    `).join('');
}

function handleAttentionClick(type) {

    if (
        type === 'PAYMENT' ||
        type === 'DESIGN' ||
        type === 'DELIVERY'
    ) {
        switchTab('tab-orders');

    } else if (type === 'INVENTORY') {
        switchTab('tab-inventory');
    }
}

function switchPeriod(period) {

    currentPeriod = period;

    document.querySelectorAll('.period-tab').forEach(b => {
        b.classList.toggle(
            'active',
            b.getAttribute('data-period') === period
        );
    });

    renderPeriodMetrics(period);
}

function renderPeriodMetrics(period) {

    if (!dashboardData) return;

    const snap =
        dashboardData[period] ||
        dashboardData.today;

    document.getElementById('metric-sales').textContent =
        `₹${snap.sales}`;

    document.getElementById('metric-orders').textContent =
        snap.orders;

    document.getElementById('metric-expenses').textContent =
        `₹${snap.expenses}`;

    document.getElementById('metric-profit').textContent =
        `₹${snap.estimatedProfit}`;
}


// =========================================================
// 3. ORDER MANAGEMENT & ZIP DOWNLOAD
// =========================================================

async function loadOrders() {

    const tbody = document.getElementById(
        'admin-orders-tbody'
    );

    const searchInput =
        document.getElementById('admin-order-search');

    const search = searchInput
        ? searchInput.value.trim()
        : '';

    if (!tbody) return;

    try {

        const url = search
            ? apiUrl(
                `/api/admin/orders?search=${encodeURIComponent(search)}`
            )
            : apiUrl('/api/admin/orders');

        const res = await fetch(
            url,
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load orders');
        }

        const orders = await res.json();

        if (!Array.isArray(orders) || orders.length === 0) {

            tbody.innerHTML = `
                <tr>
                    <td colspan="8"
                        style="text-align: center; padding: 20px;">
                        No orders found.
                    </td>
                </tr>
            `;

            return;
        }

        tbody.innerHTML = orders.map(o => `
            <tr>

                <td>
                    <strong>${o.orderNumber}</strong>
                    <br>
                    <small style="color: #786C68;">
                        ${o.createdAt
                            ? o.createdAt.substring(0, 10)
                            : ''}
                    </small>
                </td>

                <td>
                    ${o.customerName}
                    <br>
                    <small>${o.customerPhone}</small>
                </td>

                <td>
                    ${o.deliveryZone}
                    <br>
                    <small style="color: #5B7B66;">
                        ₹${o.deliveryCharge}
                    </small>
                </td>

                <td>
                    <strong>
                        ₹${o.totalAmount}
                    </strong>
                </td>

                <td>
                    <span class="status-badge ${getStatusBadgeClass(o.orderStatus)}">
                        ${o.orderStatus}
                    </span>
                </td>

                <td>
                    ${
                        o.paymentStatus === 'CONFIRMED'

                        ? `
                            <span style="color: #155724; font-weight: 600;">
                                ✓ Confirmed
                            </span>
                          `

                        : `
                            <button
                                class="btn-action"
                                style="color: #856404; background: #FFF3CD;"
                                onclick="openPaymentVerifyModal(
                                    ${o.id},
                                    '${String(o.orderNumber || '').replace(/'/g, "\\'")}',
                                    '${String(o.paymentReference || '').replace(/'/g, "\\'")}',
                                    '${String(o.paymentProofUrl || '').replace(/'/g, "\\'")}'
                                ">

                                Verify

                            </button>
                          `
                    }

                </td>

                <td>

                    <div style="display: flex; gap: 6px;">

                        <button
                            class="btn-action"
                            onclick="openAdminOrderDetails(${o.id})">

                            Details

                        </button>

                        <button
                            class="btn-action btn-zip"
                            title="Download ZIP bundle"
                            onclick="downloadAdminFile(
                                '/api/admin/orders/' + ${o.id} + '/download-zip',
                                'ORDER_' + '${String(o.orderNumber || '').replace(/'/g, "\\'")}' + '.zip'
                            )">

                            ZIP

                        </button>

                    </div>

                </td>

            </tr>
        `).join('');

    } catch (e) {

        console.error('Orders error:', e);

        tbody.innerHTML = `
            <tr>
                <td colspan="8"
                    style="text-align: center; color: #A85364; padding: 20px;">

                    Failed to load orders.

                </td>
            </tr>
        `;
    }
}


async function openAdminOrderDetails(orderId) {

    try {

        const res = await fetch(
            apiUrl(`/api/admin/orders/${orderId}`),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Order not found');
        }

        const o = await res.json();

        const modal =
            document.getElementById(
                'admin-order-details-modal'
            );

        const titleElement = document.getElementById(
            'modal-order-number-title'
        );

        if (titleElement) {
            titleElement.textContent = o.orderNumber;
        }

        const customerInfoElement = document.getElementById(
            'modal-order-customer-info'
        );

        if (customerInfoElement) {
            customerInfoElement.innerHTML = `
                <strong>${o.customerName}</strong>
                (${o.customerPhone})
                <br>
                ${o.shippingAddress}
                <br>
                PIN: ${o.pinCode}
                (${o.deliveryZone})
            `;
        }

        const statusSelect = document.getElementById(
            'modal-order-status-select'
        );

        if (statusSelect) {
            statusSelect.value = o.orderStatus;
        }

        const orderIdHidden = document.getElementById(
            'modal-order-id-hidden'
        );

        if (orderIdHidden) {
            orderIdHidden.value = o.id;
        }


        // Render Items and Photos

        const itemsContainer =
            document.getElementById(
                'modal-order-items-list'
            );

        if (itemsContainer) {

            const items = Array.isArray(o.items)
                ? o.items
                : [];

            itemsContainer.innerHTML =
                items.map(item => `

                    <div
                        style="
                            background: #FAF7F2;
                            border: 1px solid #EADBCE;
                            border-radius: 8px;
                            padding: 14px;
                            margin-bottom: 12px;
                        "
                    >

                        <div
                            style="
                                display: flex;
                                justify-content: space-between;
                                align-items: center;
                                margin-bottom: 8px;
                            "
                        >

                            <strong
                                style="
                                    font-size: 14px;
                                    color: #2C2523;
                                "
                            >

                                ${item.productName}
                                —
                                ${item.variantName}
                                (Qty: ${item.quantity})

                            </strong>

                            <span style="font-weight: 700;">
                                ₹${item.itemSubtotal}
                            </span>

                        </div>


                        ${
                            item.customizationSelected

                            ? `
                                <div
                                    style="
                                        font-size: 12px;
                                        color: #C46D7E;
                                        margin-bottom: 6px;
                                    "
                                >

                                    🌸 Customization Instructions:
                                    "${item.customizationInstructions || 'None'}"

                                </div>
                              `

                            : ''
                        }


                        <div
                            style="
                                font-size: 12px;
                                font-weight: 600;
                                color: #786C68;
                                margin-bottom: 6px;
                            "
                        >

                            Customer Photos:

                        </div>


                        <div
                            style="
                                display: flex;
                                gap: 8px;
                                flex-wrap: wrap;
                            "
                        >

                            ${
                                item.photos &&
                                item.photos.length > 0

                                ? item.photos.map(p => `

                                    <div
                                        style="
                                            width: 75px;
                                            height: 75px;
                                            border-radius: 6px;
                                            overflow: hidden;
                                            border: 1px solid #EFE4D8;
                                            position: relative;
                                        "
                                    >

                                        <img
                                            src="${p.fileUrl}"
                                            style="
                                                width: 100%;
                                                height: 100%;
                                                object-fit: cover;
                                            "
                                            alt="${p.originalFilename || 'Customer photo'}"
                                        >

                                        <a
                                            href="${p.fileUrl}"
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            style="
                                                position: absolute;
                                                bottom: 2px;
                                                right: 2px;
                                                background: rgba(0,0,0,0.6);
                                                color: #fff;
                                                font-size: 10px;
                                                padding: 2px 4px;
                                                border-radius: 4px;
                                            "
                                        >
                                            View
                                        </a>

                                    </div>

                                `).join('')

                                : `
                                    <span
                                        style="
                                            font-size: 12px;
                                            color: #786C68;
                                        "
                                    >
                                        No photos attached
                                    </span>
                                  `
                            }

                        </div>

                    </div>

                `).join('');
        }


        if (modal) {
            modal.classList.add('active');
        }

    } catch (e) {

        console.error('Order details error:', e);

        showToast(
            e.message,
            'error'
        );
    }
}


async function saveAdminOrderStatus() {

    const orderId =
        document.getElementById(
            'modal-order-id-hidden'
        ).value;

    const status =
        document.getElementById(
            'modal-order-status-select'
        ).value;

    const notes =
        document.getElementById(
            'modal-order-status-notes'
        ).value.trim();

    try {

        const res = await fetch(
            apiUrl(`/api/admin/orders/${orderId}/status`),
            {
                method: 'PATCH',
                headers: adminHeaders(),

                body: JSON.stringify({
                    status: status,
                    notes: notes
                })
            }
        );

        if (!res.ok) {
            throw new Error(
                'Could not update status'
            );
        }

        showToast(
            'Order status updated!'
        );

        document
            .getElementById(
                'admin-order-details-modal'
            )
            .classList.remove('active');

        loadOrders();
        loadDashboardSummary();

    } catch (e) {

        showToast(
            e.message,
            'error'
        );
    }
}


function openPaymentVerifyModal(
    orderId,
    orderNum,
    ref,
    proofUrl
) {

    const modal =
        document.getElementById(
            'payment-verify-modal'
        );

    const orderIdHidden = document.getElementById(
        'verify-order-id-hidden'
    );

    if (orderIdHidden) {
        orderIdHidden.value = orderId;
    }

    const orderNumElement = document.getElementById(
        'verify-order-num'
    );

    if (orderNumElement) {
        orderNumElement.textContent = orderNum;
    }

    const refElement = document.getElementById(
        'verify-order-ref'
    );

    if (refElement) {
        refElement.textContent =
            ref || 'None provided';
    }


    const proofContainer =
        document.getElementById(
            'verify-proof-container'
        );

    if (proofContainer) {

        if (proofUrl) {

            proofContainer.innerHTML = `
                <a
                    href="${proofUrl}"
                    target="_blank"
                    rel="noopener noreferrer"
                >

                    <img
                        src="${proofUrl}"
                        style="
                            max-height: 180px;
                            border-radius: 8px;
                            border: 1px solid #EFE4D8;
                        "
                    >

                </a>
            `;

        } else {

            proofContainer.innerHTML = `
                <span
                    style="
                        color: #786C68;
                        font-size: 12px;
                    "
                >
                    No screenshot uploaded.
                </span>
            `;
        }
    }

    if (modal) {
        modal.classList.add('active');
    }
}


async function submitPaymentVerification(status) {

    const orderId =
        document.getElementById(
            'verify-order-id-hidden'
        ).value;

    const notes =
        document.getElementById(
            'verify-notes'
        ).value.trim();

    try {

        const res = await fetch(
            apiUrl(
                `/api/admin/orders/${orderId}/verify-payment`
            ),
            {
                method: 'POST',
                headers: adminHeaders(),

                body: JSON.stringify({
                    paymentStatus: status,
                    notes: notes
                })
            }
        );

        if (!res.ok) {
            throw new Error(
                'Verification failed'
            );
        }

        showToast(
            `Payment marked as ${status}!`
        );

        document
            .getElementById(
                'payment-verify-modal'
            )
            .classList.remove('active');

        loadOrders();
        loadDashboardSummary();

    } catch (e) {

        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// 4. PRODUCTS & FRAME PHOTO COUNT CONFIGURATION
// =========================================================

async function loadProductsAndVariants() {

    const pTbody =
        document.getElementById(
            'admin-products-tbody'
        );

    const vTbody =
        document.getElementById(
            'admin-variants-tbody'
        );

    if (!pTbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/products'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load products');
        }

        const products = await res.json();


        // Products Table

        pTbody.innerHTML =
            products.map(p => `

                <tr>

                    <td>
                        <strong>${p.name}</strong>
                    </td>

                    <td>${p.category}</td>

                    <td>
                        ₹${p.basePrice}
                    </td>

                    <td>
                        +₹${p.customizationPrice}
                    </td>

                    <td>
                        ${p.minQuantity}
                    </td>

                    <td>
                        <strong
                            style="
                                color:
                                ${
                                    p.stockQuantity <=
                                    p.lowStockThreshold
                                    ? '#A85364'
                                    : '#2C2523'
                                };
                            "
                        >
                            ${p.stockQuantity}
                        </strong>
                    </td>

                    <td>
                        <button
                            class="btn-action"
                            onclick="openEditProductModal(${p.id})"
                        >
                            Edit
                        </button>
                    </td>

                </tr>

            `).join('');


        // Frame Variants Configuration Table

        if (vTbody) {

            const frameProduct =
                products.find(
                    p => p.category === 'PHOTO_FRAMES'
                );

            if (
                frameProduct &&
                frameProduct.variants
            ) {

                vTbody.innerHTML =
                    frameProduct.variants.map(v => `

                        <tr>

                            <td>
                                <strong>
                                    ${v.name}
                                </strong>
                            </td>

                            <td>
                                ${v.dimensions}
                            </td>

                            <td>
                                <strong>
                                    ₹${v.price}
                                </strong>
                            </td>

                            <td>

                                <span
                                    style="
                                        background: #FFF5F7;
                                        border: 1.5px solid #E892A2;
                                        color: #A85364;
                                        font-weight: 700;
                                        padding: 4px 10px;
                                        border-radius: 999px;
                                    "
                                >

                                    ${v.requiredPhotos}

                                    ${
                                        v.requiredPhotos === 1
                                        ? 'Photo'
                                        : 'Photos'
                                    }

                                </span>

                            </td>

                            <td>

                                <button
                                    class="btn-action"
                                    onclick="openEditVariantModal(
                                        ${v.id},
                                        '${String(v.name || '').replace(/'/g, "\\'")}',
                                        '${String(v.dimensions || '').replace(/'/g, "\\'")}',
                                        ${v.price},
                                        ${v.requiredPhotos}
                                    )"
                                >

                                    Configure Photo Count

                                </button>

                            </td>

                        </tr>

                    `).join('');
            }
        }

    } catch (e) {
        console.error('Products error:', e);
    }
}


function openEditVariantModal(
    id,
    name,
    dims,
    price,
    requiredPhotos
) {

    const modal =
        document.getElementById(
            'edit-variant-modal'
        );

    document.getElementById(
        'edit-variant-id'
    ).value = id;

    document.getElementById(
        'edit-variant-name'
    ).value = name;

    document.getElementById(
        'edit-variant-dims'
    ).value = dims;

    document.getElementById(
        'edit-variant-price'
    ).value = price;

    document.getElementById(
        'edit-variant-photos'
    ).value = requiredPhotos;

    if (modal) {
        modal.classList.add('active');
    }
}


async function saveVariantConfiguration() {

    const id =
        document.getElementById(
            'edit-variant-id'
        ).value;

    const name =
        document.getElementById(
            'edit-variant-name'
        ).value;

    const dims =
        document.getElementById(
            'edit-variant-dims'
        ).value;

    const price =
        document.getElementById(
            'edit-variant-price'
        ).value;

    const photos =
        document.getElementById(
            'edit-variant-photos'
        ).value;

    try {

        const res = await fetch(
            apiUrl(`/api/admin/variants/${id}`),
            {
                method: 'PUT',
                headers: adminHeaders(),

                body: JSON.stringify({
                    name: name,
                    dimensions: dims,
                    price: parseFloat(price),
                    requiredPhotos: parseInt(photos)
                })
            }
        );

        if (!res.ok) {
            throw new Error(
                'Could not update frame configuration'
            );
        }

        showToast(
            'Frame required photo count updated in database! 🌸'
        );

        document
            .getElementById(
                'edit-variant-modal'
            )
            .classList.remove('active');

        loadProductsAndVariants();

    } catch (e) {

        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// 5. INVENTORY MANAGEMENT
// =========================================================

async function loadInventory() {

    const tbody =
        document.getElementById(
            'admin-inventory-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/inventory'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load inventory');
        }

        const items = await res.json();

        tbody.innerHTML =
            items.map(p => {

                const isLow =
                    p.stockQuantity <=
                    p.lowStockThreshold;

                return `

                    <tr>

                        <td>
                            <strong>
                                ${p.name}
                            </strong>
                        </td>

                        <td>
                            ${p.category}
                        </td>

                        <td>
                            <strong
                                style="
                                    color:
                                    ${
                                        isLow
                                        ? '#A85364'
                                        : '#2C2523'
                                    };
                                "
                            >
                                ${p.stockQuantity}
                            </strong>
                        </td>

                        <td>
                            ${p.lowStockThreshold}
                        </td>

                        <td>

                            <span
                                class="status-badge
                                ${isLow
                                    ? 'cancelled'
                                    : 'confirmed'}"
                            >

                                ${
                                    isLow
                                    ? 'Low Stock Alert'
                                    : 'Healthy'
                                }

                            </span>

                        </td>

                        <td>

                            <button
                                class="btn-action"
                                onclick="openAdjustStockModal(
                                    ${p.id},
                                    '${String(p.name || '').replace(/'/g, "\\'")}',
                                    ${p.stockQuantity}
                                )"
                            >

                                Adjust Stock

                            </button>

                        </td>

                    </tr>

                `;
            }).join('');

    } catch (e) {
        console.error('Inventory error:', e);
    }
}


function openAdjustStockModal(
    id,
    name,
    currentStock
) {

    const modal =
        document.getElementById(
            'adjust-stock-modal'
        );

    document.getElementById(
        'adjust-stock-product-id'
    ).value = id;

    document.getElementById(
        'adjust-stock-product-name'
    ).textContent =
        `${name} (Current: ${currentStock})`;

    document.getElementById(
        'adjust-stock-qty'
    ).value = '';

    document.getElementById(
        'adjust-stock-reason'
    ).value = '';

    if (modal) {
        modal.classList.add('active');
    }
}


async function submitStockAdjustment() {

    const id =
        document.getElementById(
            'adjust-stock-product-id'
        ).value;

    const change =
        document.getElementById(
            'adjust-stock-qty'
        ).value;

    const reason =
        document.getElementById(
            'adjust-stock-reason'
        ).value.trim();

    if (!change) {

        showToast(
            'Enter quantity change (positive or negative)',
            'error'
        );

        return;
    }

    try {

        const res = await fetch(
            apiUrl(`/api/admin/inventory/${id}/adjust`),
            {
                method: 'POST',
                headers: adminHeaders(),

                body: JSON.stringify({
                    changeQuantity: parseInt(change),
                    reason: reason
                })
            }
        );

        if (!res.ok) {
            throw new Error(
                'Stock adjustment failed'
            );
        }

        showToast(
            'Stock quantity updated!'
        );

        document
            .getElementById(
                'adjust-stock-modal'
            )
            .classList.remove('active');

        loadInventory();
        loadDashboardSummary();

    } catch (e) {

        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// 6. WORKER MANAGEMENT & ACTIVITY MONITORING
// =========================================================

async function loadWorkers() {

    const tbody =
        document.getElementById(
            'admin-workers-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/workers'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load workers');
        }

        const workers = await res.json();

        tbody.innerHTML =
            workers.map(w => `

                <tr>

                    <td>
                        <strong>
                            ${w.username}
                        </strong>
                    </td>

                    <td>
                        ${w.fullName || w.username}
                    </td>

                    <td>
                        ${w.phone || 'N/A'}
                    </td>

                    <td>
                        <small>
                            ${
                                w.permissions
                                ? Array.from(w.permissions).join(', ')
                                : 'None'
                            }
                        </small>
                    </td>

                    <td>

                        ${
                            w.active

                            ? `
                                <span
                                    style="
                                        color: #155724;
                                        font-weight: 600;
                                    "
                                >
                                    Active
                                </span>
                              `

                            : `
                                <span
                                    style="
                                        color: #A85364;
                                    "
                                >
                                    Inactive
                                </span>
                              `
                        }

                    </td>

                </tr>

            `).join('');

    } catch (e) {
        console.error('Workers error:', e);
    }
}


async function loadWorkerActivities(
    filter = 'TODAY'
) {

    const tbody =
        document.getElementById(
            'admin-worker-activity-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl(
                `/api/admin/workers/activities?filter=${filter}`
            ),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load activity logs');
        }

        const logs = await res.json();

        if (!Array.isArray(logs) || logs.length === 0) {

            tbody.innerHTML = `
                <tr>
                    <td colspan="5"
                        style="
                            text-align: center;
                            padding: 20px;
                        "
                    >
                        No worker activity recorded
                        for this period.
                    </td>
                </tr>
            `;

            return;
        }

        tbody.innerHTML =
            logs.map(l => `

                <tr>

                    <td>
                        <small>
                            ${
                                l.timestamp
                                ? l.timestamp
                                    .replace('T', ' ')
                                    .substring(0, 19)
                                : ''
                            }
                        </small>
                    </td>

                    <td>
                        <strong>
                            ${
                                l.workerFullName ||
                                l.workerUsername
                            }
                        </strong>
                    </td>

                    <td>
                        <span class="status-badge placed">
                            ${l.activityType}
                        </span>
                    </td>

                    <td>
                        ${l.orderNumber || '—'}
                    </td>

                    <td>
                        <small style="color: #5C524F;">
                            ${l.details || ''}
                        </small>
                    </td>

                </tr>

            `).join('');

    } catch (e) {

        console.error('Worker activity error:', e);

        tbody.innerHTML = `
            <tr>
                <td colspan="5"
                    style="
                        text-align: center;
                        color: #A85364;
                    "
                >
                    Failed to load activity logs.
                </td>
            </tr>
        `;
    }
}


async function loadWorkerPerformance() {

    const tbody =
        document.getElementById(
            'admin-worker-perf-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/workers/performance'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load worker performance');
        }

        const perf = await res.json();

        tbody.innerHTML =
            perf.map(p => `

                <tr>

                    <td>
                        <strong>
                            ${p.fullName}
                        </strong>
                    </td>

                    <td>

                        ${
                            p.online

                            ? `
                                <span
                                    style="
                                        color: #155724;
                                        font-weight: 600;
                                    "
                                >
                                    ● Online
                                </span>
                              `

                            : `
                                <span
                                    style="
                                        color: #786C68;
                                    "
                                >
                                    ○ Offline
                                </span>
                              `
                        }

                    </td>

                    <td>
                        ${p.ordersCreated}
                    </td>

                    <td>
                        ${p.ordersUpdated}
                    </td>

                    <td>
                        ${p.filesDownloaded}
                    </td>

                    <td>
                        <small>
                            ${p.lastActive}
                        </small>
                    </td>

                </tr>

            `).join('');

    } catch (e) {
        console.error('Worker performance error:', e);
    }
}


// =========================================================
// 7. EXPENSE MANAGEMENT & NET PROFIT
// =========================================================

async function loadExpenses() {

    const tbody =
        document.getElementById(
            'admin-expenses-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/expenses'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load expenses');
        }

        const expenses = await res.json();

        let total = 0;

        tbody.innerHTML =
            expenses.map(e => {

                total += Number(e.amount) || 0;

                return `

                    <tr>

                        <td>
                            ${e.expenseDate}
                        </td>

                        <td>
                            <span class="status-badge pending">
                                ${e.category}
                            </span>
                        </td>

                        <td>
                            <strong>
                                ${e.description}
                            </strong>
                        </td>

                        <td>
                            <strong>
                                ₹${e.amount}
                            </strong>
                        </td>

                        <td>
                            <small>
                                ${e.recordedBy || 'Owner'}
                            </small>
                        </td>

                        <td>

                            <button
                                onclick="deleteExpenseItem(${e.id})"
                                style="
                                    background: none;
                                    color: #A85364;
                                    font-size: 14px;
                                "
                            >
                                ✕
                            </button>

                        </td>

                    </tr>

                `;
            }).join('');

        const totalDisplay = document.getElementById(
            'total-expenses-display'
        );

        if (totalDisplay) {
            totalDisplay.textContent =
                `₹${total}`;
        }

    } catch (e) {
        console.error('Expenses error:', e);
    }
}


async function submitAddExpense(event) {

    if (event) {
        event.preventDefault();
    }

    const category =
        document.getElementById(
            'expense-category'
        ).value;

    const amount =
        document.getElementById(
            'expense-amount'
        ).value;

    const date =
        document.getElementById(
            'expense-date'
        ).value;

    const desc =
        document.getElementById(
            'expense-desc'
        ).value.trim();

    if (!amount || !desc) {

        showToast(
            'Please enter amount and description',
            'error'
        );

        return;
    }

    try {

        const res = await fetch(
            apiUrl('/api/admin/expenses'),
            {
                method: 'POST',
                headers: adminHeaders(),

                body: JSON.stringify({
                    category: category,
                    amount: parseFloat(amount),
                    expenseDate:
                        date ||
                        new Date()
                            .toISOString()
                            .substring(0, 10),
                    description: desc
                })
            }
        );

        if (!res.ok) {
            throw new Error(
                'Failed to record expense'
            );
        }

        showToast(
            'Expense recorded!'
        );

        document
            .getElementById(
                'add-expense-modal'
            )
            .classList.remove('active');

        document
            .getElementById(
                'add-expense-form'
            )
            .reset();

        loadExpenses();
        loadDashboardSummary();

    } catch (e) {

        showToast(
            e.message,
            'error'
        );
    }
}


async function deleteExpenseItem(id) {

    if (
        !confirm(
            'Are you sure you want to delete this expense?'
        )
    ) {
        return;
    }

    try {

        const res = await fetch(
            apiUrl(`/api/admin/expenses/${id}`),
            {
                method: 'DELETE',
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to delete expense');
        }

        showToast(
            'Expense removed'
        );

        loadExpenses();
        loadDashboardSummary();

    } catch (e) {
        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// 8. CUSTOMER MANAGEMENT
// =========================================================

async function loadCustomers() {

    const tbody =
        document.getElementById(
            'admin-customers-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/customers'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load customers');
        }

        const customers = await res.json();

        tbody.innerHTML =
            customers.map(c => `

                <tr>

                    <td>

                        <strong>
                            ${c.fullName}
                        </strong>

                        <br>

                        <small>
                            ${c.email}
                        </small>

                    </td>

                    <td>
                        ${c.phone}
                    </td>

                    <td>
                        ${c.orderCount} order(s)
                    </td>

                    <td>
                        <strong>
                            ₹${c.totalSpent}
                        </strong>
                    </td>

                    <td>
                        ${c.lastOrder || 'None'}
                    </td>

                </tr>

            `).join('');

    } catch (e) {
        console.error('Customers error:', e);
    }
}


// =========================================================
// 9. REVIEWS MODERATION
// =========================================================

async function loadReviews() {

    const tbody =
        document.getElementById(
            'admin-reviews-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/reviews'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load reviews');
        }

        const reviews = await res.json();

        tbody.innerHTML =
            reviews.map(r => `

                <tr>

                    <td>
                        <strong>
                            ${r.customerName}
                        </strong>
                    </td>

                    <td>
                        ${'★'.repeat(Number(r.rating) || 0)}
                    </td>

                    <td>
                        "${r.comment}"
                    </td>

                    <td>

                        <button
                            class="btn-action"
                            onclick="toggleReviewApproval(${r.id})"
                        >

                            ${
                                r.approved
                                ? '✓ Approved (Click to Hide)'
                                : '✕ Hidden (Click to Approve)'
                            }

                        </button>

                    </td>

                </tr>

            `).join('');

    } catch (e) {
        console.error('Reviews error:', e);
    }
}


async function toggleReviewApproval(id) {

    try {

        const res = await fetch(
            apiUrl(
                `/api/admin/reviews/${id}/toggle-approval`
            ),
            {
                method: 'PATCH',
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to update review');
        }

        showToast(
            'Review visibility updated'
        );

        loadReviews();

    } catch (e) {
        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// 10. COUPONS
// =========================================================

async function loadCoupons() {

    const tbody =
        document.getElementById(
            'admin-coupons-tbody'
        );

    if (!tbody) return;

    try {

        const res = await fetch(
            apiUrl('/api/admin/coupons'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load coupons');
        }

        const coupons = await res.json();

        tbody.innerHTML =
            coupons.map(c => `

                <tr>

                    <td>
                        <strong>
                            ${c.code}
                        </strong>
                    </td>

                    <td>

                        ${
                            c.discountType === 'PERCENTAGE'
                            ? `${c.discountValue}%`
                            : `₹${c.discountValue}`
                        }

                    </td>

                    <td>
                        ₹${c.minOrderAmount}
                    </td>

                    <td>
                        ${c.usageCount} / ${c.usageLimit}
                    </td>

                    <td>

                        <button
                            class="btn-action"
                            onclick="toggleCouponActive(${c.id})"
                        >

                            ${
                                c.active
                                ? 'Active'
                                : 'Inactive'
                            }

                        </button>

                    </td>

                </tr>

            `).join('');

    } catch (e) {
        console.error('Coupons error:', e);
    }
}


async function toggleCouponActive(id) {

    try {

        const res = await fetch(
            apiUrl(
                `/api/admin/coupons/${id}/toggle`
            ),
            {
                method: 'PATCH',
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to update coupon');
        }

        showToast(
            'Coupon status updated'
        );

        loadCoupons();

    } catch (e) {
        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// 11. SMART INSIGHTS & PREDICTIONS
// =========================================================

async function loadInsights() {

    const container =
        document.getElementById(
            'admin-insights-container'
        );

    const predContainer =
        document.getElementById(
            'admin-predictions-container'
        );

    try {

        const res = await fetch(
            apiUrl('/api/admin/insights'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load insights');
        }

        const suggestions =
            await res.json();

        if (container) {

            container.innerHTML =
                suggestions.map(s => `

                    <div class="suggestion-card">

                        <div class="suggestion-title">
                            ${s.title}
                        </div>

                        <div class="suggestion-meta">
                            <strong>Why:</strong>
                            ${s.why}
                        </div>

                        <div class="suggestion-meta">
                            <strong>Source Data:</strong>
                            ${s.sourceData}
                        </div>

                        <div class="suggestion-action">
                            <strong>Suggested Action:</strong>
                            ${s.suggestedAction}
                        </div>

                    </div>

                `).join('');
        }


        const predRes = await fetch(
            apiUrl('/api/admin/predictions'),
            {
                headers: adminHeaders()
            }
        );

        if (!predRes.ok) {
            throw new Error('Failed to load predictions');
        }

        const preds =
            await predRes.json();

        if (predContainer) {

            predContainer.innerHTML =
                preds.map(p => `

                    <div
                        style="
                            background: #FFFFFF;
                            border: 1px solid #EFE4D8;
                            border-radius: 8px;
                            padding: 12px 16px;
                            margin-bottom: 8px;
                            font-size: 13px;
                        "
                    >

                        💡 ${p}

                    </div>

                `).join('');
        }

    } catch (e) {
        console.error('Insights error:', e);
    }
}


// =========================================================
// 12. BUSINESS SETTINGS
// =========================================================

async function loadSettings() {

    try {

        const res = await fetch(
            apiUrl('/api/admin/settings'),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error('Failed to load settings');
        }

        const settings =
            await res.json();

        settings.forEach(s => {

            const input =
                document.getElementById(
                    `setting-${s.settingKey}`
                );

            if (input) {
                input.value = s.settingValue;
            }
        });

    } catch (e) {
        console.error('Settings error:', e);
    }
}


async function saveSettings(event) {

    if (event) {
        event.preventDefault();
    }

    const payload = {

        business_name:
            document.getElementById(
                'setting-business_name'
            ).value,

        phone:
            document.getElementById(
                'setting-phone'
            ).value,

        email:
            document.getElementById(
                'setting-email'
            ).value,

        instagram:
            document.getElementById(
                'setting-instagram'
            ).value,

        upi_id:
            document.getElementById(
                'setting-upi_id'
            ).value,

        delivery_madurai:
            document.getElementById(
                'setting-delivery_madurai'
            ).value,

        delivery_outside:
            document.getElementById(
                'setting-delivery_outside'
            ).value
    };


    try {

        const res = await fetch(
            apiUrl('/api/admin/settings'),
            {
                method: 'POST',
                headers: adminHeaders(),
                body: JSON.stringify(payload)
            }
        );

        if (!res.ok) {
            throw new Error(
                'Failed to update settings'
            );
        }

        showToast(
            'Business settings saved successfully! 🌸'
        );

    } catch (e) {

        showToast(
            e.message,
            'error'
        );
    }
}


// =========================================================
// ADMIN LOGOUT
// =========================================================

function adminLogout() {

    fetch(
        apiUrl('/api/auth/logout'),
        {
            method: 'POST',
            headers: adminHeaders()
        }
    ).finally(() => {

        localStorage.removeItem(
            'lunette_token'
        );

        localStorage.removeItem(
            'lunette_role'
        );

        localStorage.removeItem(
            'lunette_username'
        );

        window.location.href =
            '/login.html';
    });
}


// =========================================================
// ADMIN FILE DOWNLOAD
// =========================================================

async function downloadAdminFile(
    endpoint,
    filename
) {

    try {

        const res = await fetch(
            apiUrl(endpoint),
            {
                headers: adminHeaders()
            }
        );

        if (!res.ok) {
            throw new Error(
                'Download failed'
            );
        }

        const blob =
            await res.blob();

        const link =
            document.createElement('a');

        link.href =
            URL.createObjectURL(blob);

        link.download =
            filename;

        document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(
            link.href
        );

    } catch (error) {

        showToast(
            error.message ||
            'Download failed',
            'error'
        );
    }
}
