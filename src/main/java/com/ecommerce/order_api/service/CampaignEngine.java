package com.ecommerce.order_api.service;

import com.ecommerce.order_api.dto.BestCampaignResult;
import com.ecommerce.order_api.entity.Campaign;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.enums.CampaignType;
import com.ecommerce.order_api.repository.CampaignRepository;
import com.ecommerce.order_api.repository.CategoryRepository;
import com.ecommerce.order_api.repository.ProductRepository;
import com.ecommerce.order_api.service.strategy.CampaignStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CampaignEngine {

    private final Map<CampaignType, CampaignStrategy> strategyMap;
    private final CampaignRepository campaignRepository;

    public CampaignEngine(List<CampaignStrategy> strategies, CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;

        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(CampaignStrategy::getCampaignType, Function.identity()));
    }

    public BestCampaignResult evaluateBestCampaign(List<OrderItem> items) {
        List<Campaign> activeCampaigns = campaignRepository.findByIsActiveTrue();

        Campaign bestCampaign = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (Campaign campaign : activeCampaigns) {
            CampaignStrategy strategy = strategyMap.get(campaign.getType());

            if (strategy != null) {
                BigDecimal discount = strategy.calculateDiscount(items, campaign);

                if (discount.compareTo(maxDiscount) > 0) {
                    maxDiscount = discount;
                    bestCampaign = campaign;
                }

            }
        }

        return new BestCampaignResult(bestCampaign, maxDiscount);
    }


}
