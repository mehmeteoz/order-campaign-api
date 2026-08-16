package com.ecommerce.order_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse (
        Long id,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        Long appliedCampaignId,
        BigDecimal discountAmount,
        BigDecimal shippingCost,
        BigDecimal finalAmount,
        LocalDateTime createdAt) {
}
