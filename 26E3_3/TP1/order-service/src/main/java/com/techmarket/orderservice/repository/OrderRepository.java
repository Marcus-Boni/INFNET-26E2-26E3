package com.techmarket.orderservice.repository;

import com.techmarket.orderservice.domain.Order;
import com.techmarket.orderservice.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status);
}
