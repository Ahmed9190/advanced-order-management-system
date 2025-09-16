package com.orderms.entity;

public enum OrderStatus {
  PENDING("Order received, awaiting confirmation"),
  CONFIRMED("Order confirmed, preparing for processing"),
  PROCESSING("Order is being processed"),
  SHIPPED("Order has been shipped"),
  DELIVERED("Order has been delivered"),
  CANCELLED("Order has been cancelled");

  private final String description;

  OrderStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  // Business logic for valid status transitions
  public boolean canTransitionTo(OrderStatus newStatus) {
    return switch (this) {
      case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
      case CONFIRMED -> newStatus == PROCESSING || newStatus == CANCELLED;
      case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
      case SHIPPED -> newStatus == DELIVERED;
      case DELIVERED, CANCELLED -> false;
    };
  }
}