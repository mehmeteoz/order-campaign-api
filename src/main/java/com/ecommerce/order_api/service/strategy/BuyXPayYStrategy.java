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
public class BuyXPayYStrategy implements CampaignStrategy {

    private final ObjectMapper objectMapper;

    public BuyXPayYStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CampaignType getCampaignType() {
        return CampaignType.BUY_X_PAY_X;
    }

    @Override
    public BigDecimal calculateDiscount(List<OrderItem> items, Campaign campaign) {
        try {
            Boolean isActive = campaign.getIsActive();
            if (!isActive) {
                return BigDecimal.ZERO;
            }

            JsonNode rules = objectMapper.readTree(campaign.getRuleDetails());
            long targetItemId = rules.get("itemId").asLong();
            int buyAmount = rules.get("buy").asInt();
            int payAmount = rules.get("pay").asInt();

            int freeItemCountPerGroup = buyAmount - payAmount;

            return items.stream()
                    .filter(item -> item.getProduct().getId() == targetItemId)
                    .findFirst()
                    .map(item -> {
                        int quantity = item.getQuantity();

                        if (quantity < buyAmount || freeItemCountPerGroup <= 0) {
                            return BigDecimal.ZERO;
                        }

                        int campaignCount = quantity / buyAmount;

                        int totalFreeItems = campaignCount * freeItemCountPerGroup;

                        BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : item.getProduct().getPrice();

                        return unitPrice.multiply(BigDecimal.valueOf(totalFreeItems));

                    })
                    .orElse(BigDecimal.ZERO);

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }



}
