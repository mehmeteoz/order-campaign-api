package com.ecommerce.order_api.service;

import com.ecommerce.order_api.dto.*;
import com.ecommerce.order_api.entity.Cart;
import com.ecommerce.order_api.entity.CartItem;
import com.ecommerce.order_api.entity.OrderItem;
import com.ecommerce.order_api.entity.Product;
import com.ecommerce.order_api.exception.InsufficientStockException;
import com.ecommerce.order_api.exception.ProductNotFoundExeption;
import com.ecommerce.order_api.repository.CartRepository;
import com.ecommerce.order_api.repository.OrderRepository;
import com.ecommerce.order_api.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CampaignEngine campaignEngine;

    @Value("${shipping.free-limit}")
    private BigDecimal FREE_SHIPPING_LIMIT;

    @Value("${shipping.cost}")
    private BigDecimal SHIPPING_COST;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, CampaignEngine campaignEngine) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.campaignEngine = campaignEngine;
    }


    @Transactional
    public CartResponse addToCart(CartRequest request) {
        Cart cart = cartRepository.findBySessionId(request.sessionId()).orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setSessionId(request.sessionId());
            cartRepository.save(cart);
        }

        // 3. Ürünleri bul, stokları EN AZINDAN sepet miktarı kadar var mı diye kontrol et (Stok düşme!)
        for (CartItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundExeption("Product not found. ID: " + itemRequest.productId()));

            CartItem existingCartItem = cart.getCartItems().stream()
                    .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            int newQuantity = (existingCartItem != null ? existingCartItem.getQuantity() : 0) +
                    itemRequest.quantity(); // <-- quantity is red, bide burayı çok anlamadım

            if (product.getStock() < newQuantity) {
                throw  new InsufficientStockException("Insufficient stock! Item: " + product.getName() + ", quantity: " + newQuantity);
            }

            if (existingCartItem != null) {
                existingCartItem.setQuantity(newQuantity);
            } else {
                CartItem newCartItem = new CartItem();
                newCartItem.setCart(cart);
                newCartItem.setProduct(product);
                newCartItem.setQuantity(itemRequest.quantity()); // <-- also red

                cart.getCartItems().add(newCartItem);
            }
        }

        Cart savedCart = cartRepository.save(cart);

        // 6. Finansal hesaplamaları (CampaignEngine ile indirim, Kargo vb.) yapıp CartResponse DTO'sunu dön
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> tempOrderItems = new ArrayList<>();
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem cartItem : savedCart.getCartItems()) {
            BigDecimal itemTotal = BigDecimal.ZERO;
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            tempOrderItems.add(orderItem);

            itemResponses.add(new CartItemResponse(
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getName(),
                    cartItem.getQuantity(),
                    cartItem.getProduct().getPrice()
            ));
        }

        BestCampaignResult campaignResult = campaignEngine.evaluateBestCampaign(tempOrderItems);
        BigDecimal discountAmount = campaignResult.discountAmount();
        BigDecimal amountAfterDiscount = totalAmount.subtract(discountAmount);
        BigDecimal shippingAmount = BigDecimal.ZERO;

        if (amountAfterDiscount.compareTo(FREE_SHIPPING_LIMIT) > 0) { shippingAmount = SHIPPING_COST; }

        BigDecimal finalAmount = amountAfterDiscount.add(shippingAmount);
        Long appliedCampaignId = campaignResult.appliedCampaign() != null ?
                campaignResult.appliedCampaign().getId() : null;

        return new CartResponse(
                savedCart.getId(),
                itemResponses,
                savedCart.getSessionId(),
                totalAmount,
                appliedCampaignId,
                discountAmount,
                shippingAmount,
                finalAmount,
                savedCart.getCreatedAt(),
                savedCart.getUpdatedAt()
        );
    }

}
