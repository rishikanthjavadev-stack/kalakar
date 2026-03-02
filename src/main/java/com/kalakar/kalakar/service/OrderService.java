package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderItem;
import com.kalakar.kalakar.model.OrderStatus;
import com.kalakar.kalakar.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderNotificationService notificationService;

    public Order createOrder(String customerName, String customerEmail,
                             String address, String city, String state,
                             String zip, List<OrderItem> items) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setCustomerEmail(customerEmail);
        order.setShippingAddress(address);
        order.setShippingCity(city);
        order.setShippingState(state);
        order.setShippingZip(zip);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setItems(items);

        BigDecimal total = items.stream()
            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByOrderDateDesc(email);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public Order getOrderByIdAndEmail(Long id, String email) {
        return orderRepository.findByIdAndCustomerEmail(id, email)
            .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateStatus(Long id, String status) {
        Order order = getOrderById(id);
        OrderStatus newStatus = OrderStatus.valueOf(status);
        order.setStatus(newStatus);

        // Set timestamp for each status
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED      -> order.setConfirmedAt(now);
            case PACKED         -> order.setPackedAt(now);
            case SHIPPED        -> order.setShippedAt(now);
            case OUT_FOR_DELIVERY -> order.setOutForDeliveryAt(now);
            case DELIVERED      -> order.setDeliveredAt(now);
            default -> {}
        }
        // Send email notification
        try {
            notificationService.sendStatusUpdateEmail(order, newStatus);
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return orderRepository.save(order);
    }

    public Order updatePhone(Long id, String phone) {
        Order order = getOrderById(id);
        order.setCustomerPhone(phone);
        return orderRepository.save(order);
    }

    public Order updateTrackingInfo(Long id, String trackingNumber,
                                    String courierName) {
        Order order = getOrderById(id);
        order.setTrackingNumber(trackingNumber);
        order.setCourierName(courierName);
        return orderRepository.save(order);
    }
}
