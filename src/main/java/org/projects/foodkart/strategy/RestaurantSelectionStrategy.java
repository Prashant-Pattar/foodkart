package org.projects.foodkart.strategy;

import org.projects.foodkart.dto.request.OrderItemRequest;
import org.projects.foodkart.entity.Restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantSelectionStrategy {
    SelectionStrategyType getStrategyType();

    Optional<Restaurant> selectRestaurant(List<Restaurant> eligibleRestaurants, List<OrderItemRequest> requestedItems);
}
