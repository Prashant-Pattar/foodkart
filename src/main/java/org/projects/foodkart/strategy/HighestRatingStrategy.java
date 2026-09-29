package org.projects.foodkart.strategy;

import org.projects.foodkart.dto.request.OrderItemRequest;
import org.projects.foodkart.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class HighestRatingStrategy implements RestaurantSelectionStrategy {

    @Override
    public SelectionStrategyType getStrategyType() {
        return SelectionStrategyType.HIGHEST_RATING;
    }

    @Override
    public Optional<Restaurant> selectRestaurant(List<Restaurant> eligibleRestaurants, List<OrderItemRequest> requestedItems) {
        if (eligibleRestaurants == null || eligibleRestaurants.isEmpty()) {
            return Optional.empty();
        }

        return eligibleRestaurants.stream()
                .max(Comparator.comparingDouble(r -> r.getAverageRating() != null ? r.getAverageRating() : 0.0));
    }
}
