/**
 * LUNETTE GIFTS — STORE & DYNAMIC PERSONALIZATION JAVASCRIPT
 * Handles product catalog, dynamic photo slots based on database rules,
 * pricing calculation, photo uploads, customization references, and cart integration.
 */

let allProducts = [];
let currentProduct = null;
let selectedVariant = null;
let currentQuantity = 1;
let isCustomizationSelected = false;
let uploadedPhotos = []; // Array of { slotIndex, fileUrl, originalFilename }
let customizationReference = null; // { fileUrl, originalFilename }

const productImageFallbacks = {
    PHOTO_FRAMES: './images/realistic-frame.jpg',
    PHOTO_CARDS: './images/realistic-cards.jpg',
    RING_ALBUMS: './images/realistic-album.jpg',
    LED_POLAROIDS: './images/realistic-led.jpg'
};

document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    loadReviews();
    setupCategoryFilters();
});

// 1. FETCH & RENDER PRODUCTS
async function loadProducts() {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    try {
        const res = await fetch(apiUrl('/api/store/products'));
        if (!res.ok) throw new Error('Failed to load products');
        allProducts = await res.json();
        renderProducts(allProducts);
    } catch (err) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px;">
                <p style="color: #C46D7E; font-size: 16px;">Unable to load boutique gifts right now.</p>
                <button onclick="loadProducts()" class="btn-primary" style="margin-top: 14px;">Try Again</button>
            </div>
        `;
    }
}

function renderProducts(products) {
    const grid = document.getElementById('products-grid');
    if (!grid) return;

    if (products.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px;">
                <p style="font-family: 'Playfair Display', serif; font-size: 20px; color: #8C6D54;">Your memories are waiting to become something beautiful.</p>
                <p style="font-size: 13px; color: #786C68; margin-top: 6px;">New personalized handcrafted gifts will be featured here soon.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = products.map(p => {
        const minPrice = p.variants && p.variants.length > 0
            ? Math.min(...p.variants.map(v => v.price))
            : p.basePrice;

        const categoryLabels = {
            'PHOTO_FRAMES': 'Photo Frame',
            'PHOTO_CARDS': 'Photo Card',
            'RING_ALBUMS': 'Ring Album',
            'LED_POLAROIDS': 'LED Polaroid'
        };

        const categoryTag = categoryLabels[p.category] || p.category;

        return `
            <div class="product-card" data-category="${p.category}">
                <div class="product-thumb">
                    <img src="${p.imageUrl || productImageFallbacks[p.category] || './images/realistic-frame.jpg'}" alt="${p.name}" loading="lazy" onerror="this.onerror=null;this.src='${productImageFallbacks[p.category] || './images/realistic-frame.jpg'}';">
                    <span class="product-badge">${categoryTag}</span>
                </div>
                <div class="product-info">
                    <span class="product-category-tag">${categoryTag}</span>
                    <h3 class="product-name">${p.name}</h3>
                    <p class="product-desc">${p.description || ''}</p>
                    <div class="product-footer">
                        <div class="product-price">
                            ₹${minPrice} <small>${p.minQuantity > 1 ? `(Min ${p.minQuantity})` : ''}</small>
                        </div>
                        <button class="btn-customize" onclick="openProductModal(${p.id})">Customize & Buy</button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function setupCategoryFilters() {
    const filterBtns = document.querySelectorAll('[data-filter]');
    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const cat = btn.getAttribute('data-filter');
            if (cat === 'ALL') {
                renderProducts(allProducts);
            } else {
                const filtered = allProducts.filter(p => p.category === cat);
                renderProducts(filtered);
            }
        });
    });
}

// 2. INTERACTIVE PRODUCT CUSTOMIZATION MODAL
function openProductModal(productId) {
    currentProduct = allProducts.find(p => p.id === productId);
    if (!currentProduct) return;

    // Reset state
    currentQuantity = currentProduct.minQuantity || 1;
    isCustomizationSelected = false;
    uploadedPhotos = [];
    customizationReference = null;

    // Select default variant (first variant)
    selectedVariant = (currentProduct.variants && currentProduct.variants.length > 0)
        ? currentProduct.variants[0]
        : null;

    renderModalContent();

    const backdrop = document.getElementById('product-modal-backdrop');
    if (backdrop) backdrop.classList.add('active');
}

function closeProductModal() {
    const backdrop = document.getElementById('product-modal-backdrop');
    if (backdrop) backdrop.classList.remove('active');
}

function renderModalContent() {
    const modalBody = document.getElementById('modal-dynamic-body');
    const modalTitle = document.getElementById('modal-product-title');
    if (!modalBody || !currentProduct) return;

    modalTitle.textContent = currentProduct.name;

    // 1. Variant Selector (Crucial for Frames: 5x5, 4x6, 5x7, A4)
    let variantsHtml = '';
    if (currentProduct.variants && currentProduct.variants.length > 1) {
        variantsHtml = `
            <div class="options-group">
                <label class="options-label">Select Frame Size / Variant</label>
                <div class="variant-chips">
                    ${currentProduct.variants.map(v => `
                        <div class="variant-chip ${selectedVariant && selectedVariant.id === v.id ? 'selected' : ''}"
                             onclick="selectVariant(${v.id})">
                            <span class="variant-name">${v.name}</span>
                            <span class="variant-meta">₹${v.price} • ${v.requiredPhotos} ${v.requiredPhotos === 1 ? 'photo' : 'photos'} required</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    // 2. Quantity Selector
    const isSingleItemFrame = currentProduct.category === 'PHOTO_FRAMES';
    let qtyHtml = `
        <div class="options-group">
            <label class="options-label">Quantity ${currentProduct.minQuantity > 1 ? `<small style="text-transform: none; color: #C46D7E;">(Minimum: ${currentProduct.minQuantity})</small>` : ''}</label>
            <div class="qty-controller">
                <button class="qty-btn" onclick="adjustQuantity(-1)">−</button>
                <input type="text" class="qty-input" id="modal-qty-input" value="${currentQuantity}" readonly>
                <button class="qty-btn" onclick="adjustQuantity(1)">+</button>
            </div>
        </div>
    `;

    // 3. Customization Section
    const custPrice = currentProduct.customizationPrice || 0;
    let customizationHtml = `
        <div class="customization-card">
            <label class="customization-toggle">
                <input type="checkbox" id="customization-checkbox" ${isCustomizationSelected ? 'checked' : ''} onchange="toggleCustomization(this.checked)">
                <div>
                    <strong style="font-size: 14px;">Add Personalized Customization (+₹${custPrice})</strong>
                    <p style="font-size: 12px; color: #786C68; margin-top: 2px;">Include personal names, dates, quotes, or reference design layouts.</p>
                </div>
            </label>
            <div class="customization-fields ${isCustomizationSelected ? 'open' : ''}" id="customization-fields-container">
                <label style="font-size: 12px; font-weight: 600; color: #2C2523;">Customization Instructions / Text</label>
                <textarea class="input-field" id="customization-instructions" rows="2" placeholder="e.g. Please print 'Together Since 2020' at bottom center with gold font"></textarea>

                <label style="font-size: 12px; font-weight: 600; color: #2C2523; margin-top: 8px; display: block;">Optional Reference Image</label>
                <div style="display: flex; align-items: center; gap: 12px; margin-top: 6px;">
                    <label class="btn-secondary" style="font-size: 12px; padding: 6px 14px; cursor: pointer;">
                        Upload Reference Image
                        <input type="file" accept="image/*" style="display: none;" onchange="uploadCustomizationRef(this)">
                    </label>
                    <span id="ref-image-name" style="font-size: 12px; color: #786C68;">No file chosen</span>
                </div>
            </div>
        </div>
    `;

    // 4. Dynamic Photo Upload Slots
    const totalRequiredPhotos = calculateRequiredPhotos();
    let photoUploadHtml = `
        <div class="photo-upload-section">
            <div class="upload-header-row">
                <div>
                    <strong style="font-size: 15px; color: #2C2523;">Upload Your Memories</strong>
                    <p style="font-size: 12px; color: #786C68;">Please upload high-resolution photos for optimal printing.</p>
                </div>
                <div class="upload-status-counter" id="upload-counter-badge">
                    ${uploadedPhotos.length} / ${totalRequiredPhotos} photos uploaded
                </div>
            </div>
            <div class="photo-slots-grid" id="photo-slots-grid">
                <!-- Dynamic slots will be populated below -->
            </div>
        </div>
    `;

    modalBody.innerHTML = variantsHtml + qtyHtml + customizationHtml + photoUploadHtml;

    renderPhotoSlots();
    updateModalPriceSummary();
}

function selectVariant(variantId) {
    selectedVariant = currentProduct.variants.find(v => v.id === variantId);
    uploadedPhotos = []; // Reset uploaded photos to match new requirement
    renderModalContent();
}

function adjustQuantity(delta) {
    const min = currentProduct.minQuantity || 1;
    let step = 1;
    // For Photo Cards, Albums, Polaroids min is 10, often ordered in multiples of 5 or 10
    if (currentProduct.category === 'PHOTO_CARDS' || currentProduct.category === 'RING_ALBUMS' || currentProduct.category === 'LED_POLAROIDS') {
        step = 5;
    }

    const newQty = currentQuantity + (delta * step);
    if (newQty >= min) {
        currentQuantity = newQty;
        // Re-calculate slots
        renderModalContent();
    }
}

function toggleCustomization(checked) {
    isCustomizationSelected = checked;
    const container = document.getElementById('customization-fields-container');
    if (container) {
        container.classList.toggle('open', checked);
    }
    updateModalPriceSummary();
}

function calculateRequiredPhotos() {
    if (!currentProduct) return 1;

    if (currentProduct.category === 'PHOTO_FRAMES') {
        // Controlled by database variant entity!
        return selectedVariant ? selectedVariant.requiredPhotos : 1;
    } else {
        // 1 photo per card
        return currentQuantity;
    }
}

// 3. DYNAMIC PHOTO SLOTS RENDERING & UPLOAD
function renderPhotoSlots() {
    const grid = document.getElementById('photo-slots-grid');
    if (!grid) return;

    const totalRequired = calculateRequiredPhotos();
    let slotsHtml = '';

    for (let i = 1; i <= totalRequired; i++) {
        const uploaded = uploadedPhotos.find(p => p.slotIndex === i);
        if (uploaded) {
            slotsHtml += `
                <div class="photo-slot filled" id="slot-${i}">
                    <img src="${uploaded.fileUrl}" class="slot-preview" alt="Slot ${i}">
                    <div class="slot-actions">
                        <label class="slot-action-btn" title="Replace Photo">
                            ✎
                            <input type="file" accept="image/jpeg,image/png,image/webp" style="display: none;" onchange="handleSlotUpload(${i}, this)">
                        </label>
                        <button class="slot-action-btn" title="Remove Photo" onclick="removeSlotPhoto(${i})">✕</button>
                    </div>
                </div>
            `;
        } else {
            slotsHtml += `
                <label class="photo-slot" id="slot-${i}">
                    <input type="file" accept="image/jpeg,image/png,image/webp" style="display: none;" onchange="handleSlotUpload(${i}, this)">
                    <div class="slot-placeholder">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>
                        </svg>
                        <span>Photo ${i}</span>
                    </div>
                </label>
            `;
        }
    }

    grid.innerHTML = slotsHtml;

    // Update counter
    const counterBadge = document.getElementById('upload-counter-badge');
    if (counterBadge) {
        counterBadge.textContent = `${uploadedPhotos.length} / ${totalRequired} photos uploaded`;
    }

    checkAddToCartEligibility();
}

async function handleSlotUpload(slotIndex, input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    const slotElem = document.getElementById(`slot-${slotIndex}`);
    if (slotElem) {
        slotElem.innerHTML = `<span style="font-size: 11px; color: #C46D7E;">Uploading...</span>`;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'customer_photos');

    try {
        const res = await fetch(apiUrl('/api/store/upload'), {
            method: 'POST',
            body: formData
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.error || 'Upload failed');
        }

        const data = await res.json();

        // Update or insert slot in uploadedPhotos
        const existingIdx = uploadedPhotos.findIndex(p => p.slotIndex === slotIndex);
        const photoObj = {
            slotIndex: slotIndex,
            fileUrl: data.fileUrl,
            originalFilename: data.originalFilename
        };

        if (existingIdx >= 0) {
            uploadedPhotos[existingIdx] = photoObj;
        } else {
            uploadedPhotos.push(photoObj);
        }

        renderPhotoSlots();
    } catch (err) {
        showToast('Error uploading photo: ' + err.message, 'error');
        renderPhotoSlots();
    }
}

function removeSlotPhoto(slotIndex) {
    uploadedPhotos = uploadedPhotos.filter(p => p.slotIndex !== slotIndex);
    renderPhotoSlots();
}

async function uploadCustomizationRef(input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    const nameLabel = document.getElementById('ref-image-name');
    if (nameLabel) nameLabel.textContent = 'Uploading...';

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'customization_references');

    try {
        const res = await fetch(apiUrl('/api/store/upload'), {
            method: 'POST',
            body: formData
        });
        if (!res.ok) throw new Error('Failed to upload reference');
        const data = await res.json();
        customizationReference = {
            fileUrl: data.fileUrl,
            originalFilename: data.originalFilename
        };
        if (nameLabel) nameLabel.textContent = data.originalFilename;
        showToast('Reference image attached');
    } catch (err) {
        if (nameLabel) nameLabel.textContent = 'Upload failed';
        showToast(err.message, 'error');
    }
}

// 4. PRICE CALCULATION & ADD TO CART
function updateModalPriceSummary() {
    if (!currentProduct) return;

    let base = currentProduct.basePrice;
    if (selectedVariant) {
        base = selectedVariant.price;
    }

    let itemTotal = 0;
    if (currentProduct.category === 'PHOTO_CARDS') {
        // ₹8 per card
        itemTotal = base * currentQuantity;
    } else if (currentProduct.category === 'RING_ALBUMS') {
        // Ring ₹20 + (quantity * ₹8)
        itemTotal = (currentProduct.ringPrice || 20) + (base * currentQuantity);
    } else if (currentProduct.category === 'LED_POLAROIDS') {
        // Light ₹150 + (quantity * ₹7)
        itemTotal = (currentProduct.lightPrice || 150) + (base * currentQuantity);
    } else {
        // Photo frames
        itemTotal = base * currentQuantity;
    }

    if (isCustomizationSelected) {
        itemTotal += (currentProduct.customizationPrice || 0);
    }

    const totalDisplay = document.getElementById('modal-total-price');
    if (totalDisplay) {
        totalDisplay.textContent = `₹${itemTotal}`;
    }

    checkAddToCartEligibility();
}

function checkAddToCartEligibility() {
    const addBtn = document.getElementById('modal-add-to-cart-btn');
    if (!addBtn) return;

    const totalRequired = calculateRequiredPhotos();
    const isComplete = uploadedPhotos.length === totalRequired;

    addBtn.disabled = !isComplete;
    if (!isComplete) {
        addBtn.style.opacity = '0.6';
        addBtn.style.cursor = 'not-allowed';
        addBtn.textContent = `Upload ${totalRequired - uploadedPhotos.length} More Photos`;
    } else {
        addBtn.style.opacity = '1';
        addBtn.style.cursor = 'pointer';
        addBtn.textContent = 'Add To Cart';
    }
}

function addCurrentProductToCart() {
    const totalRequired = calculateRequiredPhotos();
    if (uploadedPhotos.length < totalRequired) {
        showToast(`Please upload all ${totalRequired} photos before adding to cart`, 'error');
        return;
    }

    const instructionsElem = document.getElementById('customization-instructions');
    const instructions = instructionsElem ? instructionsElem.value.trim() : '';

    let base = currentProduct.basePrice;
    if (selectedVariant) base = selectedVariant.price;

    let subtotal = 0;
    if (currentProduct.category === 'PHOTO_CARDS') {
        subtotal = base * currentQuantity;
    } else if (currentProduct.category === 'RING_ALBUMS') {
        subtotal = (currentProduct.ringPrice || 20) + (base * currentQuantity);
    } else if (currentProduct.category === 'LED_POLAROIDS') {
        subtotal = (currentProduct.lightPrice || 150) + (base * currentQuantity);
    } else {
        subtotal = base * currentQuantity;
    }

    const custCharge = isCustomizationSelected ? (currentProduct.customizationPrice || 0) : 0;
    subtotal += custCharge;

    const cartItem = {
        productId: currentProduct.id,
        productName: currentProduct.name,
        variantId: selectedVariant ? selectedVariant.id : null,
        variantName: selectedVariant ? selectedVariant.name : 'Standard',
        variantDimensions: selectedVariant ? selectedVariant.dimensions : '',
        quantity: currentQuantity,
        unitPrice: base,
        customizationSelected: isCustomizationSelected,
        customizationCharge: custCharge,
        customizationInstructions: instructions,
        photoUrls: uploadedPhotos.sort((a,b) => a.slotIndex - b.slotIndex).map(p => p.fileUrl),
        originalFilenames: uploadedPhotos.sort((a,b) => a.slotIndex - b.slotIndex).map(p => p.originalFilename),
        customizationReferenceUrl: customizationReference ? customizationReference.fileUrl : null,
        customizationReferenceFilename: customizationReference ? customizationReference.originalFilename : null,
        itemSubtotal: subtotal,
        imageUrl: currentProduct.imageUrl
    };

    const cart = getCart();
    cart.push(cartItem);
    saveCart(cart);

    closeProductModal();
    showToast(`Added ${currentProduct.name} to your cart! 🌸`);
}

// 5. REVIEWS LOADER
async function loadReviews() {
    const container = document.getElementById('reviews-container');
    if (!container) return;

    try {
        const res = await fetch(apiUrl('/api/store/reviews'));
        if (!res.ok) return;
        const reviews = await res.json();

        if (reviews.length === 0) return;

        container.innerHTML = reviews.map(r => `
            <div class="category-card" style="text-align: left; padding: 22px;">
                <div style="color: #E892A2; font-size: 16px; margin-bottom: 8px;">
                    ${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}
                </div>
                <p style="font-style: italic; font-size: 13px; color: #5C524F; margin-bottom: 12px;">"${r.comment}"</p>
                <div style="display: flex; align-items: center; justify-content: space-between;">
                    <strong style="font-size: 13px; color: #2C2523;">${r.customerName}</strong>
                    <span style="font-size: 11px; color: #7C9D86; font-weight: 600;">Verified Buyer</span>
                </div>
            </div>
        `).join('');
    } catch (e) {}
}
