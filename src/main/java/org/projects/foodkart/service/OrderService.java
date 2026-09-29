package org.projects.foodkart.service;

import org.projects.foodkart.dto.request.PlaceOrderRequest;
import org.projects.foodkart.dto.response.OrderResponse;
import org.projects.foodkart.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest request);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByTrackingNumber(String trackingNumber);
    List<OrderResponse> getOrdersByUserId(Long userId);
    List<OrderResponse> getOrdersByRestaurantId(Long restaurantId);
}
