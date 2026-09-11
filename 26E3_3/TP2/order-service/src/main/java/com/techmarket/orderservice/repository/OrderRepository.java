package com.techmarket.orderservice.repository;

import com.techmarket.orderservice.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    List<Order> findAll();
}
