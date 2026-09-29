package org.projects.foodkart.service.impl;

import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.OrderItemRequest;
import org.projects.foodkart.dto.request.PlaceOrderRequest;
import org.projects.foodkart.dto.response.OrderResponse;
import org.projects.foodkart.entity.*;
import org.projects.foodkart.exception.BadRequestException;
import org.projects.foodkart.exception.CapacityExceededException;
import org.projects.foodkart.exception.InsufficientQuantityException;
import org.projects.foodkart.exception.ResourceNotFoundException;
import org.projects.foodkart.repository.MenuItemRepository;
import org.projects.foodkart.repository.OrderRepository;
import org.projects.foodkart.repository.RestaurantRepository;
import org.projects.foodkart.service.OrderService;
import org.projects.foodkart.service.RestaurantService;
import org.projects.foodkart.service.UserService;
import org.projects.foodkart.strategy.RestaurantSelectionStrategy;
import org.projects.foodkart.strategy.SelectionStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final SelectionStrategyFactory selectionStrategyFactory;

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = userService.findUserEntityById(request.getUserId());

        String deliveryPinCode = (request.getDeliveryPinCode() != null && !request.getDeliveryPinCode().isBlank())
                ? request.getDeliveryPinCode().trim()
                : user.getPinCode();

        Restaurant targetRestaurant;

        if (request.getRestaurantId() != null) {
            targetRestaurant = restaurantService.findRestaurantEntityById(request.getRestaurantId());

            if (!targetRestaurant.getServiceablePinCodes().contains(deliveryPinCode)) {
                throw new BadRequestException("Restaurant '" + targetRestaurant.getName() +
                        "' does not deliver to pin code: " + deliveryPinCode);
            }

            if (!targetRestaurant.canAcceptOrder()) {
                throw new CapacityExceededException("Restaurant '" + targetRestaurant.getName() +
                        "' has reached its maximum processing capacity (" + targetRestaurant.getMaxCapacity() + ")");
            }

            validateItemsAvailability(targetRestaurant, request.getItems());
        } else {
            List<Restaurant> eligibleRestaurants = restaurantService.findEligibleRestaurants(deliveryPinCode, request.getItems());

            if (eligibleRestaurants.isEmpty()) {
                throw new BadRequestException("No restaurant currently has available capacity and stock to fulfill this order for pin code: " + deliveryPinCode);
            }

            RestaurantSelectionStrategy strategy = selectionStrategyFactory.getStrategy(request.getSelectionStrategy());
            targetRestaurant = strategy.selectRestaurant(eligibleRestaurants, request.getItems())
                    .orElseThrow(() -> new BadRequestException("Could not select an eligible restaurant using strategy: " + request.getSelectionStrategy()));
        }

        // Deduct inventory and build OrderItems
        Map<String, MenuItem> menuMap = targetRestaurant.getMenuItems().stream()
                .collect(Collectors.toMap(i -> i.getName().toLowerCase().trim(), i -> i, (a, b) -> a));

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuMap.get(itemReq.getItemName().toLowerCase().trim());
            if (menuItem == null || menuItem.getQuantity() < itemReq.getQuantity()) {
                throw new InsufficientQuantityException("Insufficient quantity for item: " + itemReq.getItemName());
            }

            menuItem.setQuantity(menuItem.getQuantity() - itemReq.getQuantity());
            if (menuItem.getQuantity() == 0) {
                menuItem.setIsAvailable(false);
            }
            menuItemRepository.save(menuItem);

            double subTotal = menuItem.getPrice() * itemReq.getQuantity();
            totalAmount += subTotal;

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .itemName(menuItem.getName())
                    .price(menuItem.getPrice())
                    .quantity(itemReq.getQuantity())
                    .subTotal(subTotal)
                    .build();

            orderItems.add(orderItem);
        }

        // Increment active orders for the restaurant
        targetRestaurant.setCurrentActiveOrders(targetRestaurant.getCurrentActiveOrders() + 1);
        restaurantRepository.save(targetRestaurant);

        // Build Order
        String trackingNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = Order.builder()
                .orderTrackingNumber(trackingNumber)
                .user(user)
                .restaurant(targetRestaurant)
                .deliveryPinCode(deliveryPinCode)
                .status(OrderStatus.PLACED)
                .totalAmount(totalAmount)
                .build();

        for (OrderItem oi : orderItems) {
            order.addOrderItem(oi);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromEntity(savedOrder);
    }

    private void validateItemsAvailability(Restaurant restaurant, List<OrderItemRequest> requestedItems) {
        Map<String, MenuItem> menuMap = restaurant.getMenuItems().stream()
                .collect(Collectors.toMap(i -> i.getName().toLowerCase().trim(), i -> i, (a, b) -> a));

        for (OrderItemRequest req : requestedItems) {
            MenuItem item = menuMap.get(req.getItemName().toLowerCase().trim());
            if (item == null) {
                throw new BadRequestException("Item '" + req.getItemName() + "' is not on the menu of restaurant '" + restaurant.getName() + "'");
            }
            if (item.getQuantity() < req.getQuantity() || !Boolean.TRUE.equals(item.getIsAvailable())) {
                throw new InsufficientQuantityException("Item '" + req.getItemName() + "' does not have sufficient quantity. Requested: "
                        + req.getQuantity() + ", Available: " + item.getQuantity());
            }
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus currentStatus = order.getStatus();

        // Release restaurant capacity if transitioning to DELIVERED or CANCELLED from an active status
        boolean wasActive = (currentStatus != OrderStatus.DELIVERED && currentStatus != OrderStatus.CANCELLED);
        boolean isNowTerminal = (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED);

        if (wasActive && isNowTerminal) {
            Restaurant restaurant = order.getRestaurant();
            restaurant.setCurrentActiveOrders(Math.max(0, restaurant.getCurrentActiveOrders() - 1));
            restaurantRepository.save(restaurant);
        }

        // Restock items if cancelled
        if (wasActive && newStatus == OrderStatus.CANCELLED) {
            Restaurant restaurant = order.getRestaurant();
            Map<Long, MenuItem> menuMap = restaurant.getMenuItems().stream()
                    .collect(Collectors.toMap(MenuItem::getId, i -> i, (a, b) -> a));

            for (OrderItem oi : order.getOrderItems()) {
                MenuItem item = menuMap.get(oi.getMenuItemId());
                if (item != null) {
                    item.setQuantity(item.getQuantity() + oi.getQuantity());
                    item.setIsAvailable(true);
                    menuItemRepository.save(item);
                }
            }
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return OrderResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByTrackingNumber(String trackingNumber) {
        Order order = orderRepository.findByOrderTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with tracking number: " + trackingNumber));
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
