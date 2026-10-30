package com.pharmatrack.controller;

import com.pharmatrack.dto.ApiResponse;
import com.pharmatrack.dto.CheckoutRequest;
import com.pharmatrack.dto.OrderDto;
import com.pharmatrack.dto.OrderStatusUpdateRequest;
import com.pharmatrack.model.OrderStatus;
import com.pharmatrack.model.User;
import com.pharmatrack.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal User user) {
        OrderDto order = orderService.createOrder(request, user);
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully", order));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getMyOrders(@AuthenticationPrincipal User user) {
        List<OrderDto> orders = orderService.getCustomerOrders(user);
        return ResponseEntity.ok(ApiResponse.ok("Customer orders retrieved", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        OrderDto order = orderService.getOrderDetails(id, user);
        return ResponseEntity.ok(ApiResponse.ok("Order details retrieved", order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String reason = body != null ? body.get("reason") : "Customer requested cancellation";
        OrderDto order = orderService.cancelOrder(id, reason, user);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled and reserved stock released", order));
    }

    // Staff & Admin endpoints
    @GetMapping("/staff/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrdersForStaff(
            @RequestParam(required = false) String status) {
        OrderStatus orderStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        List<OrderDto> orders = orderService.getAllOrdersForStaff(orderStatus);
        return ResponseEntity.ok(ApiResponse.ok("All orders retrieved", orders));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @AuthenticationPrincipal User user) {
        OrderDto order = orderService.updateOrderStatus(id, request, user);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", order));
    }
}
