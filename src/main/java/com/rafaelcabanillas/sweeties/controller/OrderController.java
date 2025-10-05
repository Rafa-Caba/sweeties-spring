package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.*;
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

@RestController
@RequestMapping("/api/orders") // For public checkout POST and admin GET
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // POST /api/orders  (public/guest order)
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        OrderDTO saved = orderService.createOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrderResponse(saved.getId(), "Pedido recibido"));
    }

    // PATCH /api/orders/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest req
    ) {
        OrderDTO updated = orderService.updateOrderStatus(id, req.getStatus());
        return ResponseEntity.ok(updated);
    }

    @Data
    public static class StatusUpdateRequest {
        private String status;
    }

    // GET /api/orders/{id} (admin or future guest, if desired)
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // GET /api/orders?status=pending&page=0&size=10
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, page, size));
    }

    // GET /api/orders (admin)
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportOrders(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=orders.csv");
        orderService.exportOrdersAsCsv(response.getWriter());
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> filterOrders(
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
    private static class OrderResponse {
        private Long orderId;
        private String message;
    }
}
