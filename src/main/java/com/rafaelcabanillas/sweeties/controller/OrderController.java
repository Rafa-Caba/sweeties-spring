package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.CreateOrderDTO;
import com.rafaelcabanillas.sweeties.dto.OrderDTO;
import com.rafaelcabanillas.sweeties.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ------- PUBLIC CHECKOUT -------
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        OrderDTO saved = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("orderId", saved.getId(), "message", "Pedido recibido"));
    }

    // ------- ADMIN MANAGEMENT -------

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest req
    ) {
        // Optionally validate req.status against an enum/whitelist here
        OrderDTO updated = orderService.updateOrderStatus(id, req.getStatus());
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderDTO>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, // <-- ADDED default
            @RequestParam(defaultValue = "10") int size   // <-- ADDED default
    ) {
        // This existing service call already handles a null status
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/export", produces = "text/csv")
    public void exportOrders(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");
        orderService.exportOrdersAsCsv(response.getWriter());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderDTO>> searchOrders(
             @RequestParam(required = false) String status,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
             @RequestParam(required = false) Double minTotal,
             @RequestParam(required = false) Double maxTotal,
             @RequestParam(required = false) String phone,
             @RequestParam(required = false) String email,
             @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "10") int size
    ) {
        List<OrderDTO> result = orderService.filterOrders(
                status, from, to, minTotal, maxTotal, phone, email, page, size
        );
        return ResponseEntity.ok(result);
    }

    @Data
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        private String status;
    }
}