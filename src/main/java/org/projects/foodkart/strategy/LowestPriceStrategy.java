package org.projects.foodkart.strategy;

import org.projects.foodkart.dto.request.OrderItemRequest;
import org.projects.foodkart.entity.MenuItem;
import org.projects.foodkart.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LowestPriceStrategy implements RestaurantSelectionStrategy {

    @Override
    public SelectionStrategyType getStrategyType() {
        return SelectionStrategyType.LOWEST_PRICE;
    }

    @Override
    public Optional<Restaurant> selectRestaurant(List<Restaurant> eligibleRestaurants, List<OrderItemRequest> requestedItems) {
        if (eligibleRestaurants == null || eligibleRestaurants.isEmpty()) {
            return Optional.empty();
        }

        return eligibleRestaurants.stream()
                .min(Comparator.comparingDouble(r -> computeTotalCost(r, requestedItems)));
    }

    private double computeTotalCost(Restaurant restaurant, List<OrderItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            // If no specific items requested, return average price of menu items
            return restaurant.getMenuItems().stream()
                    .mapToDouble(MenuItem::getPrice)
                    .average()
                    .orElse(Double.MAX_VALUE);
        }

        Map<String, MenuItem> menuMap = restaurant.getMenuItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getName().toLowerCase().trim(),
                        item -> item,
                        (item1, item2) -> item1
                ));

        double total = 0.0;
        for (OrderItemRequest req : requestedItems) {
            MenuItem item = menuMap.get(req.getItemName().toLowerCase().trim());
            if (item != null) {
                total += item.getPrice() * req.getQuantity();
            }
        }
        return total;
    }
}
