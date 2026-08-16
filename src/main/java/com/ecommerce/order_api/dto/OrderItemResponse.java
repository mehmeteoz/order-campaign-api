package com.ecommerce.order_api.dto;

import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.Product;

import java.math.BigDecimal;


public record OrderItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice) {

}
