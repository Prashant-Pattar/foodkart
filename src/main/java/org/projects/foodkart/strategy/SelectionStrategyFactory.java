package org.projects.foodkart.strategy;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SelectionStrategyFactory {

    private final Map<SelectionStrategyType, RestaurantSelectionStrategy> strategyMap = new EnumMap<>(SelectionStrategyType.class);

    public SelectionStrategyFactory(List<RestaurantSelectionStrategy> strategies) {
        for (RestaurantSelectionStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyType(), strategy);
        }
    }

    public RestaurantSelectionStrategy getStrategy(SelectionStrategyType type) {
        RestaurantSelectionStrategy strategy = strategyMap.get(type != null ? type : SelectionStrategyType.LOWEST_PRICE);
        if (strategy == null) {
            return strategyMap.get(SelectionStrategyType.LOWEST_PRICE);
        }
        return strategy;
    }
}
