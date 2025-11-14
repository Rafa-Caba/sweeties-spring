package com.rafaelcabanillas.sweeties.repository;

import com.rafaelcabanillas.sweeties.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT SUM(o.total) FROM Order o")
    Double sumTotalRevenue();

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

}
