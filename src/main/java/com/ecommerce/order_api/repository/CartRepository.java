package com.ecommerce.order_api.repository;

import com.ecommerce.order_api.entity.Cart;
import com.ecommerce.order_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c " +
            "LEFT JOIN FETCH c.cartItems ci " +
            "LEFT JOIN FETCH ci.product " +
            "WHERE c.id = :id")
    Optional<Order> findOrderWithDetailsById(@Param("id") Long id);

}
