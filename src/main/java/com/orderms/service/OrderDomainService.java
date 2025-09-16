package com.orderms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.orderms.entity.Order;
import com.orderms.entity.OrderStatus;
import com.orderms.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderDomainService {
  private final OrderRepository orderRepo;

  public OrderDomainService(OrderRepository orderRepo) {
    this.orderRepo = orderRepo;
  }

  @Transactional
  public Order createOrder(Order order) {
    return orderRepo.save(order);
  }

  @Transactional(readOnly = true)
  public Order getById(String id) {
    return orderRepo.findByIdWithItems(id)
        .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
    
  }

  @Transactional(readOnly = true)
  public Order getByIdWithItems(String id) {
    return orderRepo.findByIdWithItems(id)
        .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
  }

  @Transactional
  public Order updateStatus(String id, OrderStatus newStatus) {
    Order order = getById(id);
    if (!order.getStatus().canTransitionTo(newStatus)) {
      throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
    }
    order.updateStatus(newStatus);
    return order;
  }

  public List<Order> listByCustomer(String customerId, int page, int limit) {
    // Simple for now paging later
    return orderRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);
  }
}
