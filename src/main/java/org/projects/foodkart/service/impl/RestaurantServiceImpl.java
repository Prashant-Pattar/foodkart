package org.projects.foodkart.service.impl;

import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.MenuItemRequest;
import org.projects.foodkart.dto.request.OrderItemRequest;
import org.projects.foodkart.dto.request.RestaurantRegisterRequest;
import org.projects.foodkart.dto.request.UpdateMenuRequest;
import org.projects.foodkart.dto.request.UpdatePinCodesRequest;
import org.projects.foodkart.dto.response.RestaurantResponse;
import org.projects.foodkart.entity.MenuItem;
import org.projects.foodkart.entity.Restaurant;
import org.projects.foodkart.exception.ResourceNotFoundException;
import org.projects.foodkart.repository.MenuItemRepository;
import org.projects.foodkart.repository.RestaurantRepository;
import org.projects.foodkart.service.RestaurantService;
import org.projects.foodkart.strategy.SelectionStrategyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    @Transactional
    public RestaurantResponse registerRestaurant(RestaurantRegisterRequest request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .maxCapacity(request.getMaxCapacity())
                .currentActiveOrders(0)
                .averageRating(0.0)
                .ratingCount(0)
                .isActive(true)
                .serviceablePinCodes(new HashSet<>(request.getServiceablePinCodes()))
                .build();

        if (request.getMenuItems() != null) {
            for (MenuItemRequest itemReq : request.getMenuItems()) {
                MenuItem item = MenuItem.builder()
                        .name(itemReq.getName())
                        .description(itemReq.getDescription())
                        .price(itemReq.getPrice())
                        .quantity(itemReq.getQuantity())
                        .category(itemReq.getCategory())
                        .isAvailable(itemReq.getQuantity() > 0)
                        .build();
                restaurant.addMenuItem(item);
            }
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        return RestaurantResponse.fromEntity(findRestaurantEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Restaurant findRestaurantEntityById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantResponse updateMenu(Long restaurantId, UpdateMenuRequest request) {
        Restaurant restaurant = findRestaurantEntityById(restaurantId);

        Map<String, MenuItem> existingMenu = restaurant.getMenuItems().stream()
                .collect(Collectors.toMap(i -> i.getName().toLowerCase().trim(), i -> i, (a, b) -> a));

        for (MenuItemRequest itemReq : request.getItems()) {
            String key = itemReq.getName().toLowerCase().trim();
            if (existingMenu.containsKey(key)) {
                MenuItem existing = existingMenu.get(key);
                existing.setPrice(itemReq.getPrice());
                existing.setQuantity(existing.getQuantity() + itemReq.getQuantity());
                if (itemReq.getDescription() != null) existing.setDescription(itemReq.getDescription());
                if (itemReq.getCategory() != null) existing.setCategory(itemReq.getCategory());
                existing.setIsAvailable(existing.getQuantity() > 0);
            } else {
                MenuItem newItem = MenuItem.builder()
                        .name(itemReq.getName())
                        .description(itemReq.getDescription())
                        .price(itemReq.getPrice())
                        .quantity(itemReq.getQuantity())
                        .category(itemReq.getCategory())
                        .isAvailable(itemReq.getQuantity() > 0)
                        .build();
                restaurant.addMenuItem(newItem);
            }
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public RestaurantResponse updatePinCodes(Long restaurantId, UpdatePinCodesRequest request) {
        Restaurant restaurant = findRestaurantEntityById(restaurantId);
        restaurant.getServiceablePinCodes().addAll(request.getServiceablePinCodes());
        Restaurant updated = restaurantRepository.save(restaurant);
        return RestaurantResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurants(String pinCode, SelectionStrategyType strategyType, String itemName) {
        List<Restaurant> restaurants = restaurantRepository.findByServiceablePinCode(pinCode);

        // Filter active restaurants with available capacity
        List<Restaurant> filtered = restaurants.stream()
                .filter(Restaurant::canAcceptOrder)
                .collect(Collectors.toList());

        // Filter by item name if specified
        if (itemName != null && !itemName.isBlank()) {
            final String targetItem = itemName.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(r -> r.getMenuItems().stream()
                            .anyMatch(i -> i.getName().equalsIgnoreCase(targetItem) && i.getQuantity() > 0 && Boolean.TRUE.equals(i.getIsAvailable())))
                    .collect(Collectors.toList());
        }

        // Apply sorting based on selection strategy
        SelectionStrategyType strategy = strategyType != null ? strategyType : SelectionStrategyType.HIGHEST_RATING;
        if (strategy == SelectionStrategyType.HIGHEST_RATING) {
            filtered.sort((r1, r2) -> Double.compare(
                    r2.getAverageRating() != null ? r2.getAverageRating() : 0.0,
                    r1.getAverageRating() != null ? r1.getAverageRating() : 0.0
            ));
        } else if (strategy == SelectionStrategyType.LOWEST_PRICE) {
            filtered.sort(Comparator.comparingDouble(r -> {
                if (itemName != null && !itemName.isBlank()) {
                    return r.getMenuItems().stream()
                            .filter(i -> i.getName().equalsIgnoreCase(itemName.trim()))
                            .mapToDouble(MenuItem::getPrice)
                            .findFirst()
                            .orElse(Double.MAX_VALUE);
                }
                return r.getMenuItems().stream()
                        .mapToDouble(MenuItem::getPrice)
                        .min()
                        .orElse(Double.MAX_VALUE);
            }));
        }

        return filtered.stream().map(RestaurantResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Restaurant> findEligibleRestaurants(String pinCode, List<OrderItemRequest> requestedItems) {
        List<Restaurant> serviceable = restaurantRepository.findByServiceablePinCode(pinCode);

        return serviceable.stream()
                .filter(Restaurant::canAcceptOrder)
                .filter(restaurant -> canFulfillAllItems(restaurant, requestedItems))
                .collect(Collectors.toList());
    }

    private boolean canFulfillAllItems(Restaurant restaurant, List<OrderItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            return true;
        }

        Map<String, MenuItem> menuMap = restaurant.getMenuItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getName().toLowerCase().trim(),
                        item -> item,
                        (i1, i2) -> i1
                ));

        for (OrderItemRequest req : requestedItems) {
            MenuItem item = menuMap.get(req.getItemName().toLowerCase().trim());
            if (item == null || item.getQuantity() < req.getQuantity() || !Boolean.TRUE.equals(item.getIsAvailable())) {
                return false;
            }
        }
        return true;
    }
}
