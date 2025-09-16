package com.orderms.grpc;

import com.orderms.grpc.generated.CreateOrderRequest;
import com.orderms.grpc.generated.GetOrderRequest;
import com.orderms.grpc.generated.ListOrdersRequest;
import com.orderms.grpc.generated.ListOrdersResponse;
import com.orderms.grpc.generated.OrderResponse;
import com.orderms.grpc.generated.OrderServiceGrpc;
import com.orderms.grpc.generated.UpdateOrderStatusRequest;
import com.orderms.mapper.OrderMapper;
import com.orderms.service.OrderDomainService;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class OrderGrpcEndpoint extends OrderServiceGrpc.OrderServiceImplBase {
  private final OrderDomainService domain;
  private final OrderMapper mapper;

  public OrderGrpcEndpoint(OrderDomainService domain, OrderMapper mapper) {
    this.domain = domain;
    this.mapper = mapper;
  }

  // RPCs
  @Override
  public void createOrder(CreateOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
    var entity = mapper.toEntity(request);
    var saved = domain.createOrder(entity);
    var proto = mapper.toProto(saved);

    responseObserver.onNext(proto);
    responseObserver.onCompleted();
  }

  @Override
  public void getOrder(GetOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
    var entity = domain.getById(request.getOrderId());
    var proto = mapper.toProto(entity);

    responseObserver.onNext(proto);
    responseObserver.onCompleted();
  }

  @Override
  public void updateOrderStatus(UpdateOrderStatusRequest request, StreamObserver<OrderResponse> responseObserver) {
    var saved = domain.updateStatus(request.getOrderId(), mapper.toEntityStatus(request.getStatus()));
    var proto = mapper.toProto(saved);

    responseObserver.onNext(proto);
    responseObserver.onCompleted();
  }

  @Override
  public void listCustomerOrders(ListOrdersRequest request,
      StreamObserver<ListOrdersResponse> responseObserver) {
    var list = domain.listByCustomer(request.getCustomerId(), request.getPage(), request.getLimit());

    var protoList = list.stream().map(mapper::toProto).toList();

    var resp = ListOrdersResponse.newBuilder()
        .addAllOrders(protoList)
        .setTotalCount(protoList.size())
        .setPage(request.getPage())
        .setHasNext(false) // pagination later
        .build();

    responseObserver.onNext(resp);
    responseObserver.onCompleted();
  }
}
