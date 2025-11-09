package com.rafaelcabanillas.sweeties.repository;

import com.rafaelcabanillas.sweeties.model.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
    // We only need the standard JpaRepository methods.
    // We will find the singleton using findById(1L).
}