package org.projects.foodkart.service;

import org.projects.foodkart.dto.request.OrderItemRequest;
import org.projects.foodkart.dto.request.RestaurantRegisterRequest;
import org.projects.foodkart.dto.request.UpdateMenuRequest;
import org.projects.foodkart.dto.request.UpdatePinCodesRequest;
import org.projects.foodkart.dto.response.RestaurantResponse;
import org.projects.foodkart.entity.Restaurant;
import org.projects.foodkart.strategy.SelectionStrategyType;

import java.util.List;

public interface RestaurantService {
    RestaurantResponse registerRestaurant(RestaurantRegisterRequest request);
    RestaurantResponse getRestaurantById(Long id);
    Restaurant findRestaurantEntityById(Long id);
    List<RestaurantResponse> getAllRestaurants();
    RestaurantResponse updateMenu(Long restaurantId, UpdateMenuRequest request);
    RestaurantResponse updatePinCodes(Long restaurantId, UpdatePinCodesRequest request);
    List<RestaurantResponse> searchRestaurants(String pinCode, SelectionStrategyType strategyType, String itemName);
    List<Restaurant> findEligibleRestaurants(String pinCode, List<OrderItemRequest> requestedItems);
}
