package com.ecommerce.order_api.exception;

import com.ecommerce.order_api.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExeptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(Exception exception) {

        List<String> errors = List.of(exception.getMessage());

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "An error occurred while processing the request",
                errors);

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    // Stok hatası için (HTTP 400)
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientStockException(Exception exception) {
        List<String> errors = List.of(exception.getMessage());
        ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "Insufficent Stock Error", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductNotFoundExeption.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFoundExeption(Exception exception) {
        List<String> errors = List.of(exception.getMessage());
        ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Product Not Found", errors);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundExeption.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFoundExeption(Exception exception) {
        List<String> errors = List.of(exception.getMessage());
        ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "Order Not Found", errors);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {

        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toCollection(ArrayList::new));

        errors.addFirst(exception.getMessage());

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed!",
                errors
        );

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

}
