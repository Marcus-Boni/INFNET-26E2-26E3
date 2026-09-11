package com.techmarket.orderservice.repository;

import com.techmarket.orderservice.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public Order save(Order order) {
        if (order.getId() == null || order.getId().isBlank()) {
            order.setId(String.valueOf(idSequence.incrementAndGet()));
        }
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(storage.values());
    }
}
