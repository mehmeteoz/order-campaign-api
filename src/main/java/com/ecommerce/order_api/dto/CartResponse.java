package com.ecommerce.order_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse (
        Long id,
        List<CartItemResponse> items,
        String sessionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt ) {
}
