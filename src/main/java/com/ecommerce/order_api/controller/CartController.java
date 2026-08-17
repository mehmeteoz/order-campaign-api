package com.ecommerce.order_api.controller;

import com.ecommerce.order_api.dto.CartRequest;
import com.ecommerce.order_api.dto.CartResponse;
import com.ecommerce.order_api.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

//    @PostMapping
//    public ResponseEntity<CartResponse> createCart(@RequestBody CartRequest cartRequest) {
//        CartResponse createdCart = cartService.createCart(cartRequest);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdCart);
//    }

    @PostMapping("/add-items")
    public ResponseEntity<CartResponse> addToCart(@RequestBody CartRequest cartRequest) {
        CartResponse response = cartService.addToCart(cartRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
