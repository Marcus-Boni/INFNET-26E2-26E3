package com.infnet.tp3.domain.repository;

import com.infnet.tp3.domain.model.Order;
import com.infnet.tp3.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllWithItems();

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    @EntityGraph(attributePaths = {"items"})
    List<Order> findByStatus(OrderStatus status);

    Optional<Order> findByTrackingNumber(String trackingNumber);
}
