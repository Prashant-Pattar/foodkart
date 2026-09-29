package org.projects.foodkart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projects.foodkart.entity.Restaurant;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private Integer maxCapacity;
    private Integer currentActiveOrders;
    private Integer availableCapacity;
    private Double averageRating;
    private Integer ratingCount;
    private Boolean isActive;
    @Builder.Default
    private Set<String> serviceablePinCodes = new HashSet<>();
    @Builder.Default
    private List<MenuItemResponse> menuItems = new ArrayList<>();
    private LocalDateTime createdAt;

    public static RestaurantResponse fromEntity(Restaurant restaurant) {
        if (restaurant == null) return null;
        int available = Math.max(0, restaurant.getMaxCapacity() - (restaurant.getCurrentActiveOrders() != null ? restaurant.getCurrentActiveOrders() : 0));
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .maxCapacity(restaurant.getMaxCapacity())
                .currentActiveOrders(restaurant.getCurrentActiveOrders())
                .availableCapacity(available)
                .averageRating(restaurant.getAverageRating())
                .ratingCount(restaurant.getRatingCount())
                .isActive(restaurant.getIsActive())
                .serviceablePinCodes(restaurant.getServiceablePinCodes())
                .menuItems(restaurant.getMenuItems() != null ?
                        restaurant.getMenuItems().stream().map(MenuItemResponse::fromEntity).collect(Collectors.toList()) :
                        new ArrayList<>())
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}
