package com.ecommerce.order_api.service.strategy;

import com.ecommerce.order_api.entity.Campaign;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.enums.CampaignType;

import java.math.BigDecimal;
import java.util.List;

public interface CampaignStrategy {

    CampaignType getCampaignType();

    BigDecimal calculateDiscount(List<OrderItem> orderItems, Campaign campaign);
}
