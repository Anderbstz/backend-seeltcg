package com.pikacards.order.repository;

import com.pikacards.auth.model.User;
import com.pikacards.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
