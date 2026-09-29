package org.projects.foodkart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projects.foodkart.entity.OrderItem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private Double price;
    private Integer quantity;
    private Double subTotal;

    public static OrderItemResponse fromEntity(OrderItem item) {
        if (item == null) return null;
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subTotal(item.getSubTotal())
                .build();
    }
}
