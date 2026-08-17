package com.ecommerce.order_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CartRequest(
        @NotEmpty @Valid List<CartItemRequest> items) {

}

