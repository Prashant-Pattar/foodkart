package org.projects.foodkart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.PlaceOrderRequest;
import org.projects.foodkart.dto.request.UpdateOrderStatusRequest;
import org.projects.foodkart.dto.response.ApiResponse;
import org.projects.foodkart.dto.response.OrderResponse;
import org.projects.foodkart.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully", response));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", response));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByTrackingNumber(@PathVariable String trackingNumber) {
        OrderResponse response = orderService.getOrderByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponse> responses = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok("User orders retrieved successfully", responses));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByRestaurantId(@PathVariable Long restaurantId) {
        List<OrderResponse> responses = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(ApiResponse.ok("Restaurant orders retrieved successfully", responses));
    }
}
