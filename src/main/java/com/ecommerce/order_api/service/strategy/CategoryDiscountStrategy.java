package com.ecommerce.order_api.service.strategy;

import com.ecommerce.order_api.entity.Campaign;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.enums.CampaignType;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;



@Component
public class CategoryDiscountStrategy implements CampaignStrategy {

    private final ObjectMapper objectMapper;

    public CategoryDiscountStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CampaignType getCampaignType() {
        return CampaignType.CATEGORY_DISCOUNT;
    }

    @Override
    public BigDecimal calculateDiscount(List<OrderItem> items, Campaign campaign) {
        try {
            Boolean isActive = campaign.getIsActive();
            if (!isActive) {
                return BigDecimal.ZERO;
            }

            JsonNode rules = objectMapper.readTree(campaign.getRuleDetails());

            Long targetCategoryId = rules.get("categoryId").asLong();
            BigDecimal percentage = BigDecimal.valueOf(rules.get("percentage").asDouble());

            BigDecimal categoryTotalAmount = items.stream()
                    .filter(item -> item.getProduct().getCategory().getId().equals(targetCategoryId))
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return categoryTotalAmount.multiply(percentage).divide(BigDecimal.valueOf(100));
        } catch (Exception e) {
            // Loglama
        }

        return BigDecimal.ZERO;
    }
}