package com.ecommerce.order_api.exception;

public class CartNotFoundException extends RuntimeException{
    public CartNotFoundException(String message) { super(message); }
}
