package com.ecommerce.order_api.dto;

import com.ecommerce.order_api.entity.Campaign;

import java.math.BigDecimal;

public record BestCampaignResult(
        Campaign appliedCampaign,
        BigDecimal discountAmount) {

}
