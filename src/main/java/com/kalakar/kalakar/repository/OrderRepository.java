package com.kalakar.kalakar.repository;

import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmailOrderByOrderDateDesc(String email);
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByStatus(OrderStatus status);
    Optional<Order> findByIdAndCustomerEmail(Long id, String email);
}
