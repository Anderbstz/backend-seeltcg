package com.pikacards.cart.repository;

import com.pikacards.auth.model.User;
import com.pikacards.cart.model.CartItem;
import com.pikacards.catalog.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndCard(User user, Card card);
    void deleteByUser(User user);
}
