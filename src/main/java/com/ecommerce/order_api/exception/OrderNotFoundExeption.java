package com.ecommerce.order_api.exception;

public class OrderNotFoundExeption extends RuntimeException {
    public OrderNotFoundExeption(String message) {
        super(message);
    }
}
