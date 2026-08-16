package com.ecommerce.order_api.entity;


import com.ecommerce.order_api.entity.enums.CampaignType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String campaignName;

    @Enumerated(EnumType.STRING)
    private CampaignType type;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String ruleDetails;

    private Boolean isActive;


}
