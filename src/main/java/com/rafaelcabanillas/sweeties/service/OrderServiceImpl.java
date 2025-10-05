// package: com.rafaelcabanillas.sweeties.service

package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.model.Order;
import com.rafaelcabanillas.sweeties.model.OrderItem;
import com.rafaelcabanillas.sweeties.repository.OrderRepository;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Override
    public OrderDTO createOrder(CreateOrderDTO dto) {
        Order order = Order.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .note(dto.getNote())
                .items(dto.getItems().stream()
                        .map(i -> OrderItem.builder()
                                .productId(i.getProductId())
                                .name(i.getName())
                                .price(i.getPrice())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .total(dto.getTotal())
                .status("pendiente")
                .build();
        orderRepository.save(order);

        OrderDTO savedOrder = toOrderDTO(order);

        try {
            emailService.sendOrderConfirmationToGuest(savedOrder.getEmail(), savedOrder);
            emailService.sendOrderConfirmationToAdmin("admin@sweeties.com", savedOrder); // Replace as needed
        } catch (Exception ex) {
            // Log but do not fail the order creation!
        }
        return savedOrder;
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));
        // Only allow allowed status values
        List<String> allowed = List.of("pendiente", "enviado", "entregado");
        if (!allowed.contains(status))
            throw new IllegalArgumentException("Estado inválido.");
        order.setStatus(status);
        orderRepository.save(order);
        return toOrderDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));
        return toOrderDTO(order);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.stream().map(this::toOrderDTO).toList();
    }

    @Override
    public List<OrderDTO> filterOrders(
            String status, OffsetDateTime from, OffsetDateTime to, Double minTotal, Double maxTotal,
            String phone, String email, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository
                .findByStatusAndCreatedAtBetweenAndTotalBetweenAndPhoneContainingAndEmailContaining(
                        status, from, to, minTotal, maxTotal, phone, email, pageable
                );
        return orders.stream().map(this::toOrderDTO).toList();
    }


    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toOrderDTO).toList();
    }

    public void exportOrdersAsCsv(Writer writer) throws IOException {
        List<OrderDTO> orders = getAllOrders();
        writer.write("id,name,email,phone,total,status,createdAt,updatedAt\n");
        for (OrderDTO order : orders) {
            writer.write(String.format(
                    "%d,%s,%s,%s,%.2f,%s,%s,%s\n",
                    order.getId(), order.getName(), order.getEmail(), order.getPhone(),
                    order.getTotal(), order.getStatus(), order.getCreatedAt(), order.getUpdatedAt()
            ));
        }
        writer.flush();
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("El pedido no existe");
        }
        orderRepository.deleteById(id);
    }

    private OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .name(order.getName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .note(order.getNote())
                .items(order.getItems() != null ?
                        order.getItems().stream().map(i ->
                                OrderItemDTO.builder()
                                        .productId(i.getProductId())
                                        .name(i.getName())
                                        .price(i.getPrice())
                                        .quantity(i.getQuantity())
                                        .build()
                        ).collect(Collectors.toList())
                        : null)
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null)
                .build();
    }
}
