package com.rafaelcabanillas.sweeties.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long userCount;
    private long itemCount;
    private long pendingOrdersCount;
    private Double totalRevenue;
}