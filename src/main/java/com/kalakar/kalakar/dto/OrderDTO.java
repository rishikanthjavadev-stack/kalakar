package com.kalakar.kalakar.dto;

import com.kalakar.kalakar.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String trackingNumber;
    private String courierName;
    private LocalDateTime confirmedAt;
    private LocalDateTime packedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemDTO> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String n) { this.customerName = n; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String e) { this.customerEmail = e; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String p) { this.customerPhone = p; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus s) { this.status = s; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal t) { this.totalAmount = t; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime d) { this.orderDate = d; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String a) { this.shippingAddress = a; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String c) { this.shippingCity = c; }
    public String getShippingState() { return shippingState; }
    public void setShippingState(String s) { this.shippingState = s; }
    public String getShippingZip() { return shippingZip; }
    public void setShippingZip(String z) { this.shippingZip = z; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String t) { this.trackingNumber = t; }
    public String getCourierName() { return courierName; }
    public void setCourierName(String c) { this.courierName = c; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime t) { this.confirmedAt = t; }
    public LocalDateTime getPackedAt() { return packedAt; }
    public void setPackedAt(LocalDateTime t) { this.packedAt = t; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime t) { this.shippedAt = t; }
    public LocalDateTime getOutForDeliveryAt() { return outForDeliveryAt; }
    public void setOutForDeliveryAt(LocalDateTime t) { this.outForDeliveryAt = t; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime t) { this.deliveredAt = t; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
