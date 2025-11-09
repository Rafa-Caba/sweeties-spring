package com.rafaelcabanillas.sweeties.repository;

import com.rafaelcabanillas.sweeties.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByIsVisibleTrueOrderByIdDesc();
    List<Item> findByIsFeaturedTrueAndIsVisibleTrueOrderByIdDesc();
    List<Item> findTop8ByIsVisibleTrueOrderByIdDesc();
}
