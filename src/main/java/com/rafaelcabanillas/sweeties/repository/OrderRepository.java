package com.rafaelcabanillas.sweeties.repository;

import com.rafaelcabanillas.sweeties.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.*;
import java.time.OffsetDateTime;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

}
