package com.ecommerce.order_api.service.strategy;

import com.ecommerce.order_api.entity.Campaign;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.enums.CampaignType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class TotalAmountDiscountStrategy implements CampaignStrategy {

    private final ObjectMapper objectMapper;

    public TotalAmountDiscountStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CampaignType getCampaignType() {
        return CampaignType.TOTAL_DISCOUNT;
    }

    @Override
    public BigDecimal calculateDiscount(List<OrderItem> items, Campaign campaign) {
        try {
            Boolean isActive = campaign.getIsActive();
            if (!isActive) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalAmount = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            JsonNode rules = objectMapper.readTree(campaign.getRuleDetails());
            BigDecimal minAmount = BigDecimal.valueOf(rules.get("minAmount").asDouble());
            BigDecimal percentage = BigDecimal.valueOf(rules.get("percentage").asDouble());

            if (totalAmount.compareTo(minAmount) >= 0) {
                return totalAmount.multiply(percentage).divide(BigDecimal.valueOf(100),  RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

}
