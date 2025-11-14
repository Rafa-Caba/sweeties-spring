package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.DashboardStatsDTO;
import com.rafaelcabanillas.sweeties.model.Order;
import com.rafaelcabanillas.sweeties.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Override
    public DashboardStatsDTO getStats() {
        Double revenue = orderRepository.sumTotalRevenue();

        return DashboardStatsDTO.builder()
                .userCount(userRepository.count())
                .itemCount(itemRepository.count())
                .pendingOrdersCount(orderRepository.countByStatus(Order.OrderStatus.PENDIENTE))
                .totalRevenue(revenue != null ? revenue : 0.0)
                .build();
    }
}