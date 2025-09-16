package com.orderms.mapper;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.orderms.entity.Order;
import com.orderms.entity.OrderItem;
import com.orderms.entity.OrderStatus;
import com.orderms.grpc.generated.CreateOrderRequest;
import com.orderms.grpc.generated.OrderResponse;

@Component
public class OrderMapper {
  public OrderResponse toProto(Order order) {
    List<OrderItem> items = order.getItems();
    List<com.orderms.grpc.generated.OrderItem> protoItems = items == null ? List.of()
        : items.stream().map(this::toProtoItem).toList();

    return OrderResponse.newBuilder()
        .setOrderId(order.getId())
        .setCustomerId(order.getCustomerId())
        .addAllItems(protoItems)
        .setDeliveryAddress(order.getDeliveryAddress())
        .setPaymentMethod(order.getPaymentMethod())
        .setStatus(toProtoStatus(order.getStatus()))
        .setCreatedAt(order.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
        .setUpdatedAt(order.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
        .setTotalAmount(order.getTotalAmount())
        .build();
  }

  public Order toEntity(CreateOrderRequest req) {
    Order order = new Order(
        req.getCustomerId(),
        req.getDeliveryAddress(),
        req.getPaymentMethod());

    List<OrderItem> items = req.getItemsList().stream().map(pi -> {
      OrderItem oi = new OrderItem(
          pi.getProductId(),
          pi.getProductName(),
          pi.getQuantity(),
          pi.getUnitPrice());
      oi.setOrder(order);
      return oi;
    }).toList();

    order.setItems(items);
    return order;
  }

  // Helpers
  private com.orderms.grpc.generated.OrderItem toProtoItem(OrderItem item) {
    return com.orderms.grpc.generated.OrderItem.newBuilder()
        .setProductId(item.getProductId())
        .setProductName(item.getProductName())
        .setQuantity(item.getQuantity())
        .setUnitPrice(item.getUnitPrice())
        .setTotalPrice(item.getQuantity() * item.getUnitPrice())
        .build();
  }

  public OrderStatus toEntityStatus(com.orderms.grpc.generated.OrderStatus status) {
    return OrderStatus.valueOf(status.name());
  }

  public com.orderms.grpc.generated.OrderStatus toProtoStatus(OrderStatus status) {
    return com.orderms.grpc.generated.OrderStatus.valueOf(status.name());
  }
}
