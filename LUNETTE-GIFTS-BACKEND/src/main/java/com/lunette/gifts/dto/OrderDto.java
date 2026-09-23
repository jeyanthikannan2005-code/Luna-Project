package com.lunette.gifts.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderDto {

    public static class OrderItemRequest {
        private Long productId;
        private Long variantId;
        private String variantName;
        private int quantity;
        private boolean customizationSelected;
        private String customizationInstructions;
        // List of uploaded file keys/paths for this item
        private List<String> photoUrls;
        private List<String> originalFilenames;
        private String customizationReferenceUrl;
        private String customizationReferenceFilename;

        public OrderItemRequest() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public boolean isCustomizationSelected() { return customizationSelected; }
        public void setCustomizationSelected(boolean customizationSelected) { this.customizationSelected = customizationSelected; }
        public String getCustomizationInstructions() { return customizationInstructions; }
        public void setCustomizationInstructions(String customizationInstructions) { this.customizationInstructions = customizationInstructions; }
        public List<String> getPhotoUrls() { return photoUrls; }
        public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
        public List<String> getOriginalFilenames() { return originalFilenames; }
        public void setOriginalFilenames(List<String> originalFilenames) { this.originalFilenames = originalFilenames; }
        public String getCustomizationReferenceUrl() { return customizationReferenceUrl; }
        public void setCustomizationReferenceUrl(String customizationReferenceUrl) { this.customizationReferenceUrl = customizationReferenceUrl; }
        public String getCustomizationReferenceFilename() { return customizationReferenceFilename; }
        public void setCustomizationReferenceFilename(String customizationReferenceFilename) { this.customizationReferenceFilename = customizationReferenceFilename; }
    }

    public static class OrderCreateRequest {
        private String customerName;
        private String customerPhone;
        private String customerEmail;
        private String shippingAddress;
        private String pinCode;
        private String city;
        private String district;
        private String state;
        private String deliveryZone; // MADURAI, OUTSIDE_MADURAI
        private String couponCode;
        private String orderNotes;
        private String paymentReference;
        private String paymentProofUrl;
        private List<OrderItemRequest> items;

        public OrderCreateRequest() {}

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getPinCode() { return pinCode; }
        public void setPinCode(String pinCode) { this.pinCode = pinCode; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getDeliveryZone() { return deliveryZone; }
        public void setDeliveryZone(String deliveryZone) { this.deliveryZone = deliveryZone; }
        public String getCouponCode() { return couponCode; }
        public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
        public String getOrderNotes() { return orderNotes; }
        public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }
        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
        public String getPaymentProofUrl() { return paymentProofUrl; }
        public void setPaymentProofUrl(String paymentProofUrl) { this.paymentProofUrl = paymentProofUrl; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class OrderStatusUpdateRequest {
        private String status;
        private String notes;

        public OrderStatusUpdateRequest() {}
        public OrderStatusUpdateRequest(String status, String notes) {
            this.status = status;
            this.notes = notes;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class PaymentVerificationRequest {
        private String paymentStatus; // CONFIRMED or REJECTED
        private String notes;

        public PaymentVerificationRequest() {}

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class PincodeCheckResponse {
        private String pinCode;
        private String city;
        private String district;
        private String state;
        private String deliveryZone;
        private BigDecimal deliveryCharge;
        private boolean isMadurai;

        public PincodeCheckResponse() {}
        public PincodeCheckResponse(String pinCode, String city, String district, String state, String deliveryZone, BigDecimal deliveryCharge, boolean isMadurai) {
            this.pinCode = pinCode;
            this.city = city;
            this.district = district;
            this.state = state;
            this.deliveryZone = deliveryZone;
            this.deliveryCharge = deliveryCharge;
            this.isMadurai = isMadurai;
        }

        public String getPinCode() { return pinCode; }
        public void setPinCode(String pinCode) { this.pinCode = pinCode; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getDeliveryZone() { return deliveryZone; }
        public void setDeliveryZone(String deliveryZone) { this.deliveryZone = deliveryZone; }
        public BigDecimal getDeliveryCharge() { return deliveryCharge; }
        public void setDeliveryCharge(BigDecimal deliveryCharge) { this.deliveryCharge = deliveryCharge; }
        public boolean isMadurai() { return isMadurai; }
        public void setMadurai(boolean isMadurai) { this.isMadurai = isMadurai; }
    }
}
