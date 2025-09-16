package com.orderms.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.orderms.OrderServiceApplication;
import com.orderms.grpc.generated.CreateOrderRequest;
import com.orderms.grpc.generated.GetOrderRequest;
import com.orderms.grpc.generated.OrderItem;
import com.orderms.grpc.generated.OrderResponse;
import com.orderms.grpc.generated.OrderServiceGrpc;
import com.orderms.grpc.generated.OrderStatus;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@SpringBootTest(classes = OrderServiceApplication.class)
@ActiveProfiles("test")
public class OrderGrpcIntegrationTest {
  private ManagedChannel channel;
  private OrderServiceGrpc.OrderServiceBlockingStub blockingStub;

  @BeforeEach
  void setup() {
    try {
      channel = ManagedChannelBuilder.forAddress("localhost", 9090)
          .usePlaintext().build();
      blockingStub = OrderServiceGrpc.newBlockingStub(channel);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  void tearDown() {
    channel.shutdownNow();
  }

  @Test
  void createAndGetOrder_happyPath() {
    // Create
    // arrange
    CreateOrderRequest createReq = CreateOrderRequest.newBuilder()
        .setCustomerId("CUST-123")
        .setDeliveryAddress("42 Teststreet, Berlin")
        .setPaymentMethod("CREDIT_CARD")
        .addAllItems(List.of(
            OrderItem.newBuilder()
                .setProductId("SKU-1")
                .setProductName("Widget")
                .setQuantity(2)
                .setUnitPrice(10.0)
                .build()))
        .build();

    // act
    OrderResponse created = blockingStub.createOrder(createReq);

    // assert
    assertThat(created.getOrderId()).isNotEmpty();
    assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(created.getTotalAmount()).isEqualTo(20.0);

    // Get
    // arrange
    GetOrderRequest getReq = GetOrderRequest.newBuilder()
        .setOrderId(created.getOrderId())
        .build();

    // act
    OrderResponse fetched = blockingStub.getOrder(getReq);

    // assert
    assertThat(fetched.getOrderId()).isEqualTo(created.getOrderId());
    assertThat(fetched.getItemsCount()).isEqualTo(1);
    assertThat(fetched.getTotalAmount()).isEqualTo(20.0);
  }
}
