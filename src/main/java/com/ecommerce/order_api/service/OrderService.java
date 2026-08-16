package com.ecommerce.order_api.service;

import com.ecommerce.order_api.dto.*;
import com.ecommerce.order_api.entity.Order;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.Product;
import com.ecommerce.order_api.exception.InsufficientStockException;
import com.ecommerce.order_api.exception.OrderNotFoundExeption;
import com.ecommerce.order_api.exception.ProductNotFoundExeption;
import com.ecommerce.order_api.repository.OrderRepository;
import com.ecommerce.order_api.repository.ProductRepository;
import lombok.Locked;
import org.apache.coyote.Request;
import org.hibernate.ReadOnlyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CampaignEngine campaignEngine;

    private static final BigDecimal FREE_SHIPPING_LIMIT = new BigDecimal("50.00");
    private static final BigDecimal SHIPPING_COST = new BigDecimal("10.00");

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository,CampaignEngine campaignEngine) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.campaignEngine = campaignEngine;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;


        for (var itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundExeption("Product not found. ID: " + itemRequest));
            if(product.getStock() < itemRequest.quantity()) {
                throw new InsufficientStockException("Insufficient stock! Item: " + itemRequest + " quantity: " + product.getStock());
            }

            product.setStock(product.getStock() - itemRequest.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        BestCampaignResult campaignResult = campaignEngine.evaluateBestCampaign(orderItems);
        order.setDiscountAmount(campaignResult.discountAmount());

        if (campaignResult.appliedCampaign() != null){
            order.setAppliedCampaignId(campaignResult.appliedCampaign().getId());
        }

        BigDecimal amountAfterDiscount = totalAmount.subtract(campaignResult.discountAmount());


        if (amountAfterDiscount.compareTo(FREE_SHIPPING_LIMIT) < 0) {
            order.setShippingAmount(SHIPPING_COST);
        }  else {
            order.setShippingAmount(BigDecimal.ZERO);
        }

        order.setFinalAmount(amountAfterDiscount.add(order.getShippingAmount()));

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long id) {

        // Standart findById yerine yazdığımız özel sorguyu çağırıyoruz
        Order order = orderRepository.findOrderWithDetailsById(id)
                .orElseThrow(() -> new OrderNotFoundExeption("Order not found. ID: " + id));

        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                orderItems,
                order.getTotalAmount(),
                order.getAppliedCampaignId(),
                order.getDiscountAmount(),
                order.getShippingAmount(),
                order.getFinalAmount(),
                order.getCreatedAt()
        );
    }

}
