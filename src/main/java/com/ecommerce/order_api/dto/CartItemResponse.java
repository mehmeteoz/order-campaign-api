package com.ecommerce.order_api.dto;

import java.math.BigDecimal;

public record CartItemResponse (
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {

}
