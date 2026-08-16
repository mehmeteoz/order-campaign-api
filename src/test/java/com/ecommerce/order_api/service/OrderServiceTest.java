package com.ecommerce.order_api.service;

import com.ecommerce.order_api.dto.BestCampaignResult;
import com.ecommerce.order_api.dto.OrderRequest;
import com.ecommerce.order_api.dto.OrderItemRequest;
import com.ecommerce.order_api.dto.OrderResponse;
import com.ecommerce.order_api.entity.Campaign;
import com.ecommerce.order_api.entity.Order;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.Product;
import com.ecommerce.order_api.exception.InsufficientStockException;
import com.ecommerce.order_api.exception.ProductNotFoundExeption;
import com.ecommerce.order_api.repository.OrderRepository;
import com.ecommerce.order_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CampaignEngine campaignEngine;

    @InjectMocks
    private OrderService orderService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setStock(10);
    }

    @Test
    void createOrder_Success() {
        // Arrange
        OrderItemRequest requestItem = new OrderItemRequest(1L, 2);
        OrderRequest request = new OrderRequest(List.of(requestItem));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        
        // 20 TL indirim uygulanacak
        BestCampaignResult campaignResult = new BestCampaignResult(campaign, BigDecimal.valueOf(20.00));
        when(campaignEngine.evaluateBestCampaign(anyList())).thenReturn(campaignResult);

        Order savedOrder = new Order();
        savedOrder.setId(100L);
        savedOrder.setTotalAmount(BigDecimal.valueOf(200.00));
        savedOrder.setDiscountAmount(BigDecimal.valueOf(20.00));
        savedOrder.setFinalAmount(BigDecimal.valueOf(180.00));
        savedOrder.setShippingAmount(BigDecimal.ZERO);
        savedOrder.setAppliedCampaignId(1L);
        
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(100.00));
        savedOrder.setOrderItems(List.of(orderItem));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(BigDecimal.valueOf(200.00), response.totalAmount());
        assertEquals(BigDecimal.valueOf(20.00), response.discountAmount());
        assertEquals(BigDecimal.ZERO, response.shippingCost()); // 180 TL > 50 TL olduğu için kargo ücretsiz
        assertEquals(BigDecimal.valueOf(180.00), response.finalAmount());
        assertEquals(1L, response.appliedCampaignId());

        // Stok düşürüldüğünü doğrula (10 - 2 = 8)
        assertEquals(8, product.getStock());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void createOrder_ThrowsProductNotFoundException() {
        // Arrange
        OrderItemRequest requestItem = new OrderItemRequest(99L, 1);
        OrderRequest request = new OrderRequest(List.of(requestItem));

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundExeption exception = assertThrows(ProductNotFoundExeption.class, () -> {
            orderService.createOrder(request);
        });

        assertTrue(exception.getMessage().contains("Product not found"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_ThrowsInsufficientStockException() {
        // Arrange
        OrderItemRequest requestItem = new OrderItemRequest(1L, 20); // 20 isteniyor ama stokta 10 var
        OrderRequest request = new OrderRequest(List.of(requestItem));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(request);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_AppliesShippingCost_WhenFinalAmountBelowLimit() {
        // Arrange
        product.setPrice(BigDecimal.valueOf(20.00)); // Ürün 20 TL
        OrderItemRequest requestItem = new OrderItemRequest(1L, 1); // 1 adet alınıyor
        OrderRequest request = new OrderRequest(List.of(requestItem));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // İndirim yok
        BestCampaignResult campaignResult = new BestCampaignResult(null, BigDecimal.ZERO);
        when(campaignEngine.evaluateBestCampaign(anyList())).thenReturn(campaignResult);

        Order savedOrder = new Order();
        savedOrder.setId(101L);
        savedOrder.setTotalAmount(BigDecimal.valueOf(20.00));
        savedOrder.setDiscountAmount(BigDecimal.ZERO);
        savedOrder.setShippingAmount(BigDecimal.valueOf(10.00));
        savedOrder.setFinalAmount(BigDecimal.valueOf(30.00)); // 20 + 10 kargo
        
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(BigDecimal.valueOf(20.00));
        savedOrder.setOrderItems(List.of(orderItem));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(10.00), response.shippingCost()); // Kargo ücreti eklenmiş olmalı
        assertEquals(BigDecimal.valueOf(30.00), response.finalAmount());
    }
}
