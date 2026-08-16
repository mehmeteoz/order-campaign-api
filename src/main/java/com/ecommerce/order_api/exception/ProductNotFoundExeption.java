package com.ecommerce.order_api.exception;

public class ProductNotFoundExeption extends RuntimeException{
    public ProductNotFoundExeption(String message) {
        super(message);
    }
}
