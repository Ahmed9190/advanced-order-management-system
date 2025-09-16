package com.orderms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orderms.entity.Order;
import com.orderms.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, String> {
  List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

  long countByStatus(OrderStatus status);

  @EntityGraph(attributePaths = "items")
  @Query("SELECT o FROM Order o WHERE o.id = :id")
  Optional<Order> findByIdWithItems(@Param("id") String id);
}
