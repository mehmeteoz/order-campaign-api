package com.ecommerce.order_api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime errorTime,
        Integer statusCode,
        String message,
        List<String> details
) {
}
