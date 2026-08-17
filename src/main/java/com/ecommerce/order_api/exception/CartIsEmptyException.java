package com.ecommerce.order_api.exception;

public class CartIsEmptyException extends RuntimeException {
    public CartIsEmptyException(String message) { super(message); }
}
