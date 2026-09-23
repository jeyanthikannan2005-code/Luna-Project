/**
 * LUNETTE GIFTS — CART & CHECKOUT JAVASCRIPT
 * Handles cart item management, single delivery fee per order (Madurai ₹70 / Outside ₹100),
 * PIN code verification, coupon application, UPI QR code display & payment reference submission.
 */

let deliveryCharge = 70; // Default Madurai
let deliveryZone = "MADURAI";
let appliedCoupon = null;
let paymentProofUrl = null;

document.addEventListener('DOMContentLoaded', () => {
    renderCart();
});

function renderCart() {
    const container = document.getElementById('cart-items-container');
    const emptyNotice = document.getElementById('cart-empty-notice');
    const checkoutForm = document.getElementById('checkout-form-container');

    const cart = getCart();

    if (cart.length === 0) {
        if (container) container.innerHTML = '';
        if (emptyNotice) emptyNotice.style.display = 'block';
        if (checkoutForm) checkoutForm.style.display = 'none';
        updateSummary(0);
        return;
    }

    if (emptyNotice) emptyNotice.style.display = 'none';
    if (checkoutForm) checkoutForm.style.display = 'block';

    let subtotal = 0;

    if (container) {
        container.innerHTML = cart.map((item, index) => {
            subtotal += item.itemSubtotal;
            return `
                <div class="cart-item">
                    <img src="${item.imageUrl || './images/frame-thumb.svg'}" class="cart-item-img" alt="${item.productName}">
                    <div class="cart-item-info">
                        <h4 class="cart-item-title">${item.productName}</h4>
                        <div class="cart-item-variant">${item.variantName} ${item.variantDimensions ? `(${item.variantDimensions})` : ''}</div>
                        <div style="font-size: 11px; color: #7C9D86; font-weight: 500; margin-top: 2px;">
                            📷 ${item.photoUrls ? item.photoUrls.length : 0} photos uploaded
                            ${item.customizationSelected ? `• Customization: YES (+₹${item.customizationCharge})` : ''}
                        </div>
                        ${item.customizationInstructions ? `<div style="font-size: 11px; color: #786C68; font-style: italic; margin-top: 2px;">Note: "${item.customizationInstructions}"</div>` : ''}
                        <div class="cart-item-price">₹${item.itemSubtotal}</div>
                    </div>
                    <div style="display: flex; flex-direction: column; align-items: flex-end; justify-content: space-between;">
                        <button onclick="removeCartItem(${index})" style="background: none; color: #A85364; font-size: 16px; padding: 4px;" title="Remove Item">✕</button>
                        <div style="font-size: 12px; font-weight: 600; color: #786C68;">Qty: ${item.quantity}</div>
                    </div>
                </div>
            `;
        }).join('');
    }

    updateSummary(subtotal);
}

function removeCartItem(index) {
    const cart = getCart();
    cart.splice(index, 1);
    saveCart(cart);
    renderCart();
    showToast('Item removed from cart');
}

// 2. PIN CODE VERIFICATION & SINGLE DELIVERY CHARGE PER ORDER
async function verifyPinCode() {
    const pinInput = document.getElementById('shipping-pincode');
    const statusDiv = document.getElementById('pincode-status-message');
    if (!pinInput) return;

    const pin = pinInput.value.trim();
    if (pin.length < 3) {
        if (statusDiv) statusDiv.innerHTML = '<span style="color: #A85364; font-size: 12px;">Please enter a valid 6-digit PIN code</span>';
        return;
    }

    if (statusDiv) statusDiv.innerHTML = '<span style="color: #786C68; font-size: 12px;">Checking delivery zone...</span>';

    try {
        const res = await fetch(`/api/delivery/check-pincode?pincode=${encodeURIComponent(pin)}`);
        if (!res.ok) throw new Error('Could not verify PIN code');
        const data = await res.json();

        deliveryZone = data.deliveryZone;
        deliveryCharge = data.deliveryCharge;

        // Auto-fill city/state if available
        const cityInput = document.getElementById('shipping-city');
        const stateInput = document.getElementById('shipping-state');
        if (cityInput && data.city) cityInput.value = data.city;
        if (stateInput && data.state) stateInput.value = data.state;

        if (statusDiv) {
            if (data.isMadurai) {
                statusDiv.innerHTML = `<span style="color: #5B7B66; font-size: 12px; font-weight: 600;">✓ Madurai Delivery Zone — Flat ₹${deliveryCharge} (charged once per order)</span>`;
            } else {
                statusDiv.innerHTML = `<span style="color: #5B7B66; font-size: 12px; font-weight: 600;">✓ Regional Shipping Zone — Flat ₹${deliveryCharge} (charged once per order)</span>`;
            }
        }

        const cart = getCart();
        const subtotal = cart.reduce((sum, item) => sum + item.itemSubtotal, 0);
        updateSummary(subtotal);
    } catch (e) {
        if (statusDiv) statusDiv.innerHTML = '<span style="color: #A85364; font-size: 12px;">Error checking PIN code. Defaulting to standard shipping.</span>';
    }
}

// 3. COUPON VALIDATION
async function applyCoupon() {
    const couponInput = document.getElementById('coupon-code-input');
    const msgElem = document.getElementById('coupon-message');
    if (!couponInput) return;

    const code = couponInput.value.trim();
    if (!code) return;

    const cart = getCart();
    const subtotal = cart.reduce((sum, item) => sum + item.itemSubtotal, 0);

    try {
        const res = await fetch(apiUrl('/api/store/coupon/validate'), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code: code, subtotal: subtotal })
        });

        const data = await res.json();
        if (data.valid) {
            appliedCoupon = data;
            if (msgElem) {
                msgElem.style.color = '#5B7B66';
                msgElem.textContent = `✓ Coupon "${data.code}" applied! Discount: ₹${data.discount}`;
            }
            updateSummary(subtotal);
            showToast(`Coupon applied: Save ₹${data.discount}!`);
        } else {
            appliedCoupon = null;
            if (msgElem) {
                msgElem.style.color = '#A85364';
                msgElem.textContent = data.error || 'Invalid coupon code';
            }
            updateSummary(subtotal);
        }
    } catch (err) {
        if (msgElem) {
            msgElem.style.color = '#A85364';
            msgElem.textContent = 'Error applying coupon';
        }
    }
}

// 4. SUMMARY CALCULATION
function updateSummary(subtotal) {
    let discount = 0;
    if (appliedCoupon && appliedCoupon.discount) {
        discount = appliedCoupon.discount;
    }

    const total = Math.max(0, subtotal - discount + (subtotal > 0 ? deliveryCharge : 0));

    const subtotalElem = document.getElementById('summary-subtotal');
    const deliveryElem = document.getElementById('summary-delivery');
    const discountRow = document.getElementById('summary-discount-row');
    const discountElem = document.getElementById('summary-discount');
    const totalElem = document.getElementById('summary-total');

    if (subtotalElem) subtotalElem.textContent = `₹${subtotal}`;
    if (deliveryElem) deliveryElem.textContent = subtotal > 0 ? `₹${deliveryCharge}` : '₹0';

    if (discountRow && discountElem) {
        if (discount > 0) {
            discountRow.style.display = 'flex';
            discountElem.textContent = `-₹${discount}`;
        } else {
            discountRow.style.display = 'none';
        }
    }

    if (totalElem) totalElem.textContent = `₹${total}`;
}

// 5. UPI PAYMENT HELPERS
function copyUpiId() {
    const upiId = "haring478-1@okicici";
    navigator.clipboard.writeText(upiId).then(() => {
        showToast("UPI ID copied: " + upiId);
    }).catch(() => {
        showToast("UPI ID: " + upiId);
    });
}

async function handlePaymentProofUpload(input) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    const statusText = document.getElementById('payment-proof-status');
    if (statusText) statusText.textContent = 'Uploading screenshot...';

    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', 'payments');

    try {
        const res = await fetch(apiUrl('/api/store/upload'), {
            method: 'POST',
            body: formData
        });
        if (!res.ok) throw new Error('Proof upload failed');
        const data = await res.json();
        paymentProofUrl = data.fileUrl;
        if (statusText) statusText.textContent = `✓ Proof uploaded: ${data.originalFilename}`;
        showToast('Payment proof attached!');
    } catch (e) {
        if (statusText) statusText.textContent = 'Upload failed';
        showToast(e.message, 'error');
    }
}

// 6. PLACE ORDER
async function placeCustomerOrder(event) {
    if (event) event.preventDefault();

    const cart = getCart();
    if (cart.length === 0) {
        showToast('Your cart is empty', 'error');
        return;
    }

    const name = document.getElementById('shipping-name').value.trim();
    const phone = document.getElementById('shipping-phone').value.trim();
    const email = document.getElementById('shipping-email').value.trim();
    const address = document.getElementById('shipping-address').value.trim();
    const pin = document.getElementById('shipping-pincode').value.trim();
    const city = document.getElementById('shipping-city').value.trim();
    const state = document.getElementById('shipping-state').value.trim();
    const notes = document.getElementById('order-notes') ? document.getElementById('order-notes').value.trim() : '';
    const paymentRef = document.getElementById('payment-reference') ? document.getElementById('payment-reference').value.trim() : '';

    if (!name || !phone || !address || !pin) {
        showToast('Please fill in your name, phone, address, and PIN code', 'error');
        return;
    }

    const payload = {
        customerName: name,
        customerPhone: phone,
        customerEmail: email,
        shippingAddress: address,
        pinCode: pin,
        city: city || (deliveryZone === 'MADURAI' ? 'Madurai' : 'Tamil Nadu'),
        district: deliveryZone === 'MADURAI' ? 'Madurai District' : '',
        state: state || 'Tamil Nadu',
        deliveryZone: deliveryZone,
        couponCode: appliedCoupon ? appliedCoupon.code : null,
        orderNotes: notes,
        paymentReference: paymentRef,
        paymentProofUrl: paymentProofUrl,
        items: cart.map(item => ({
            productId: item.productId,
            variantId: item.variantId,
            variantName: item.variantName,
            quantity: item.quantity,
            customizationSelected: item.customizationSelected,
            customizationInstructions: item.customizationInstructions,
            photoUrls: item.photoUrls,
            originalFilenames: item.originalFilenames,
            customizationReferenceUrl: item.customizationReferenceUrl,
            customizationReferenceFilename: item.customizationReferenceFilename
        }))
    };

    const submitBtn = document.getElementById('place-order-submit-btn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Processing Your Order...';
    }

    try {
        const token = localStorage.getItem('lunette_token');
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const res = await fetch(apiUrl('/api/store/order'), {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.error || 'Failed to place order');
        }

        const orderResult = await res.json();

        // Clear Cart
        localStorage.removeItem('lunette_cart');
        updateCartCountBadge();

        // Show Confirmation
        showOrderConfirmationModal(orderResult.orderNumber, orderResult.totalAmount);
    } catch (err) {
        showToast('Error placing order: ' + err.message, 'error');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Complete Order & Submit Payment';
        }
    }
}

function showOrderConfirmationModal(orderNumber, totalAmount) {
    const modal = document.getElementById('order-confirmation-modal');
    const orderNumElem = document.getElementById('confirm-order-number');
    const trackLink = document.getElementById('confirm-track-link');

    if (orderNumElem) orderNumElem.textContent = orderNumber;
    if (trackLink) trackLink.href = `/track.html?order=${orderNumber}`;

    if (modal) modal.classList.add('active');
}
