// package: com.rafaelcabanillas.sweeties.service

package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;

import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrderDTO dto);
    OrderDTO getOrderById(Long id);
    OrderDTO updateOrderStatus(Long id, String status);
    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByStatus(String status, int page, int size);
    void exportOrdersAsCsv(Writer writer) throws IOException;
    void deleteOrder(Long id);
    List<OrderDTO> filterOrders(
        String status, OffsetDateTime from, OffsetDateTime to, Double minTotal, Double maxTotal,
        String phone, String email, int page, int size
    );
}
