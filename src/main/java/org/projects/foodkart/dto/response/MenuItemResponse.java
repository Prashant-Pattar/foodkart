package org.projects.foodkart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projects.foodkart.entity.MenuItem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String category;
    private Boolean isAvailable;

    public static MenuItemResponse fromEntity(MenuItem item) {
        if (item == null) return null;
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .category(item.getCategory())
                .isAvailable(item.getIsAvailable())
                .build();
    }
}
