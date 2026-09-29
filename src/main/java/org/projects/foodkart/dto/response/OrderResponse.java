package org.projects.foodkart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projects.foodkart.entity.Order;
import org.projects.foodkart.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderTrackingNumber;
    private Long userId;
    private String userName;
    private Long restaurantId;
    private String restaurantName;
    private String deliveryPinCode;
    private OrderStatus status;
    private Double totalAmount;
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse fromEntity(Order order) {
        if (order == null) return null;
        return OrderResponse.builder()
                .id(order.getId())
                .orderTrackingNumber(order.getOrderTrackingNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userName(order.getUser() != null ? order.getUser().getName() : null)
                .restaurantId(order.getRestaurant() != null ? order.getRestaurant().getId() : null)
                .restaurantName(order.getRestaurant() != null ? order.getRestaurant().getName() : null)
                .deliveryPinCode(order.getDeliveryPinCode())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(order.getOrderItems() != null ?
                        order.getOrderItems().stream().map(OrderItemResponse::fromEntity).collect(Collectors.toList()) :
                        new ArrayList<>())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
