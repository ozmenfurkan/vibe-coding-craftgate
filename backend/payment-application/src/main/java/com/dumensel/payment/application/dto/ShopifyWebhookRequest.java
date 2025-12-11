package com.dumensel.payment.application.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Shopify Order Webhook Request DTO
 * Shopify'dan gelen "orders/create" webhook payload'ını temsil eder
 */
public class ShopifyWebhookRequest {
    
    private Long id; // Shopify order ID
    private String email;
    private String orderNumber;
    private BigDecimal totalPrice;
    private String currency;
    private String financialStatus; // pending, paid, refunded, etc.
    private String fulfillmentStatus; // fulfilled, partial, null
    private ShopifyCustomer customer;
    private List<ShopifyLineItem> lineItems;
    private ShopifyPaymentDetails paymentDetails;

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getFinancialStatus() {
        return financialStatus;
    }

    public void setFinancialStatus(String financialStatus) {
        this.financialStatus = financialStatus;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public ShopifyCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(ShopifyCustomer customer) {
        this.customer = customer;
    }

    public List<ShopifyLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<ShopifyLineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public ShopifyPaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(ShopifyPaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    /**
     * Shopify müşteri bilgileri
     */
    public static class ShopifyCustomer {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;

        // Getters & Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    /**
     * Shopify sipariş kalemi bilgileri
     */
    public static class ShopifyLineItem {
        private Long id;
        private String name;
        private Integer quantity;
        private BigDecimal price;
        private String sku;

        // Getters & Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }
    }

    /**
     * Shopify ödeme detayları (kart bilgileri)
     * Shopify webhook'unda payment_details içinde gelir
     */
    public static class ShopifyPaymentDetails {
        private String creditCardBin; // First 6 digits
        private String creditCardCompany; // Visa, Mastercard, etc.
        private String creditCardNumber; // Last 4 digits (e.g., "•••• •••• •••• 4242")
        private Integer creditCardExpMonth;
        private Integer creditCardExpYear;
        private String creditCardName; // Cardholder name

        // Getters & Setters
        public String getCreditCardBin() {
            return creditCardBin;
        }

        public void setCreditCardBin(String creditCardBin) {
            this.creditCardBin = creditCardBin;
        }

        public String getCreditCardCompany() {
            return creditCardCompany;
        }

        public void setCreditCardCompany(String creditCardCompany) {
            this.creditCardCompany = creditCardCompany;
        }

        public String getCreditCardNumber() {
            return creditCardNumber;
        }

        public void setCreditCardNumber(String creditCardNumber) {
            this.creditCardNumber = creditCardNumber;
        }

        public Integer getCreditCardExpMonth() {
            return creditCardExpMonth;
        }

        public void setCreditCardExpMonth(Integer creditCardExpMonth) {
            this.creditCardExpMonth = creditCardExpMonth;
        }

        public Integer getCreditCardExpYear() {
            return creditCardExpYear;
        }

        public void setCreditCardExpYear(Integer creditCardExpYear) {
            this.creditCardExpYear = creditCardExpYear;
        }

        public String getCreditCardName() {
            return creditCardName;
        }

        public void setCreditCardName(String creditCardName) {
            this.creditCardName = creditCardName;
        }
    }
}
