package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.model.Order;
import com.rafaelcabanillas.sweeties.model.OrderItem;
import com.rafaelcabanillas.sweeties.repository.OrderRepository;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate; // Make sure this is imported
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification; // Make sure this is imported
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.ArrayList; // Make sure this is imported
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
                        .collect(Collectors.toList())) // This is still correct!
                .total(dto.getTotal())
                .status(Order.OrderStatus.PENDIENTE) // <-- FIX 1: Use the Enum
                .build();
        orderRepository.save(order);

        OrderDTO savedOrder = toOrderDTO(order);

        try {
            // Updated method signatures (no 'to' param needed)
            emailService.sendOrderConfirmationToGuest(savedOrder);
            emailService.sendOrderConfirmationToAdmin(savedOrder);
        } catch (Exception ex) { // Catch the general Exception
            // Log but do not fail the order creation!
            // We can now use the Slf4j logger
            log.error("Failed to send order confirmation emails for order ID {}", savedOrder.getId(), ex);
        }

        return savedOrder;
    }

    @Override
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El pedido no existe"));

        Order.OrderStatus newStatus;
        try {
            // Convert string "ENVIADO" to Enum OrderStatus.ENVIADO
            newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // This catches any invalid string (like "foo" or "pendiente ")
            throw new IllegalArgumentException("Estado inválido: " + status);
        }

        order.setStatus(newStatus);

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
            try {
                Order.OrderStatus queryStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(queryStatus, pageable);
            } catch (IllegalArgumentException e) {
                // Invalid status string, return empty
                return new ArrayList<>();
            }
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

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isBlank()) {
                try {
                    Order.OrderStatus specStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), specStatus));
                } catch (IllegalArgumentException e) {
                    // Invalid status, add a predicate that returns nothing
                    predicates.add(cb.disjunction()); // or just ignore it
                }
            }

            if (phone != null && !phone.isBlank()) {
                predicates.add(cb.like(root.get("phone"), "%" + phone + "%"));
            }
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(root.get("email"), "%" + email + "%"));
            }
            if (minTotal != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("total"), minTotal));
            }
            if (maxTotal != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("total"), maxTotal));
            }

            if (from != null && to != null) {
                predicates.add(cb.between(root.get("createdAt"), from, to));
            } else if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            } else if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orders.stream().map(this::toOrderDTO).toList();
    }

    public void exportOrdersAsCsv(Writer writer) throws IOException {
        List<Order> orders = orderRepository.findAll(Sort.by("createdAt").descending());
        List<OrderDTO> orderDtos = orders.stream().map(this::toOrderDTO).toList();

        writer.write("id,name,email,phone,total,status,createdAt,updatedAt\n");
        for (OrderDTO order : orderDtos) {
            writer.write(String.format(
                    "%d,%s,%s,%s,%.2f,%s,%s,%s\n",
                    order.getId(), order.getName(), order.getEmail(), order.getPhone(),
                    order.getTotal(), order.getCreatedAt(), order.getUpdatedAt()
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
                // --- FIX 5: Convert Enum back to string for the DTO ---
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                // --- End of Fix 5 ---
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().toString() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null)
                .build();
    }
}