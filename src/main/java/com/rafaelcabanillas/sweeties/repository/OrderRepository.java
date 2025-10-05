// package: com.rafaelcabanillas.sweeties.repository

package com.rafaelcabanillas.sweeties.repository;

import com.rafaelcabanillas.sweeties.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import java.time.OffsetDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findByStatusAndCreatedAtBetweenAndTotalBetweenAndPhoneContainingAndEmailContaining(
            String status, OffsetDateTime from, OffsetDateTime to, Double minTotal, Double maxTotal, String phone, String email, Pageable pageable
    );
}
