package com.rafaelcabanillas.sweeties.repository;

import com.rafaelcabanillas.sweeties.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // You can add custom queries if needed (e.g., findByIsVisibleTrueOrderByCreatedAtDesc)
}
