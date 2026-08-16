package com.ecommerce.order_api.repository;

import com.ecommerce.order_api.entity.Campaign;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Cacheable("campaigns")
    List<Campaign> findAll();

    @Cacheable("campaigns")
    List<Campaign> findByIsActiveTrue();


}
