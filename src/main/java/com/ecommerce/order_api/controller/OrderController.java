package com.ecommerce.order_api.controller;

import com.ecommerce.order_api.dto.OrderRequest;
import com.ecommerce.order_api.dto.OrderResponse;
import com.ecommerce.order_api.entity.Order;
import com.ecommerce.order_api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {

        OrderResponse createdOrder = orderService.createOrder(orderRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") Long orderId) {

        OrderResponse orderResponse = orderService.getOrderDetails(orderId);

        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);

    }

}
