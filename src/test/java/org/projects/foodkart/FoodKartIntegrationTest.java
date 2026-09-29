package org.projects.foodkart;

import org.junit.jupiter.api.Test;
import org.projects.foodkart.dto.request.*;
import org.projects.foodkart.dto.response.*;
import org.projects.foodkart.entity.Gender;
import org.projects.foodkart.entity.OrderStatus;
import org.projects.foodkart.service.OrderService;
import org.projects.foodkart.service.RestaurantService;
import org.projects.foodkart.service.ReviewService;
import org.projects.foodkart.service.UserService;
import org.projects.foodkart.strategy.SelectionStrategyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FoodKartIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @Test
    public void testCompleteFoodKartFlow() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 100000);

        String testPinCode = "5" + (10000 + (int)(Math.random() * 89999));

        // 1. Register User
        UserRegisterRequest userRequest = UserRegisterRequest.builder()
                .name("Prashant")
                .gender(Gender.MALE)
                .phoneNumber("99" + uniqueSuffix)
                .pinCode(testPinCode)
                .build();
        UserResponse user = userService.registerUser(userRequest);
        assertNotNull(user.getId());
        assertEquals("Prashant", user.getName());

        // 2. Register Restaurant 1 (Food Court)
        RestaurantRegisterRequest r1Req = RestaurantRegisterRequest.builder()
                .name("Food Court " + uniqueSuffix)
                .address("MG Road")
                .city("Bangalore")
                .maxCapacity(2)
                .serviceablePinCodes(Set.of(testPinCode, "560002"))
                .menuItems(List.of(
                        MenuItemRequest.builder().name("Burger").price(120.0).quantity(10).category("Fast Food").build(),
                        MenuItemRequest.builder().name("Pizza").price(250.0).quantity(5).category("Fast Food").build()
                ))
                .build();
        RestaurantResponse r1 = restaurantService.registerRestaurant(r1Req);
        assertNotNull(r1.getId());
        assertEquals(2, r1.getMaxCapacity());
        assertEquals(0, r1.getCurrentActiveOrders());

        // 3. Register Restaurant 2 (Spicy Kitchen - cheaper pizza, higher price burger)
        RestaurantRegisterRequest r2Req = RestaurantRegisterRequest.builder()
                .name("Spicy Kitchen " + uniqueSuffix)
                .address("Indiranagar")
                .city("Bangalore")
                .maxCapacity(3)
                .serviceablePinCodes(Set.of(testPinCode))
                .menuItems(List.of(
                        MenuItemRequest.builder().name("Burger").price(150.0).quantity(10).category("Fast Food").build(),
                        MenuItemRequest.builder().name("Pizza").price(200.0).quantity(5).category("Fast Food").build()
                ))
                .build();
        RestaurantResponse r2 = restaurantService.registerRestaurant(r2Req);
        assertNotNull(r2.getId());

        // 4. Update Menu for Restaurant 1 (add fries)
        UpdateMenuRequest menuUpdate = UpdateMenuRequest.builder()
                .items(List.of(MenuItemRequest.builder().name("Fries").price(80.0).quantity(20).category("Sides").build()))
                .build();
        RestaurantResponse updatedR1 = restaurantService.updateMenu(r1.getId(), menuUpdate);
        assertTrue(updatedR1.getMenuItems().stream().anyMatch(i -> i.getName().equalsIgnoreCase("Fries")));

        // 5. Update Serviceable Pin Codes
        UpdatePinCodesRequest pinUpdate = UpdatePinCodesRequest.builder()
                .serviceablePinCodes(Set.of("560038"))
                .build();
        RestaurantResponse r1PinUpdated = restaurantService.updatePinCodes(r1.getId(), pinUpdate);
        assertTrue(r1PinUpdated.getServiceablePinCodes().contains("560038"));

        // 6. Test Restaurant Search
        List<RestaurantResponse> searchResults = restaurantService.searchRestaurants(testPinCode, SelectionStrategyType.HIGHEST_RATING, "Burger");
        assertFalse(searchResults.isEmpty());

        // 7. Place Order using LOWEST_PRICE Strategy
        PlaceOrderRequest orderReq = PlaceOrderRequest.builder()
                .userId(user.getId())
                .deliveryPinCode(testPinCode)
                .selectionStrategy(SelectionStrategyType.LOWEST_PRICE)
                .items(List.of(
                        OrderItemRequest.builder().itemName("Burger").quantity(2).build()
                ))
                .build();
        OrderResponse order = orderService.placeOrder(orderReq);
        assertNotNull(order.getId());
        assertEquals(r1.getId(), order.getRestaurantId()); // Food Court has cheaper Burger (120 vs 150)
        assertEquals(240.0, order.getTotalAmount());
        assertEquals(OrderStatus.PLACED, order.getStatus());

        // Verify Restaurant 1 active order incremented
        RestaurantResponse r1AfterOrder = restaurantService.getRestaurantById(r1.getId());
        assertEquals(1, r1AfterOrder.getCurrentActiveOrders());

        // 8. Add Review for Restaurant 1
        ReviewRequest reviewReq = ReviewRequest.builder()
                .userId(user.getId())
                .restaurantId(r1.getId())
                .rating(5)
                .comment("Superb taste and fast delivery!")
                .build();
        ReviewResponse review = reviewService.addReview(reviewReq);
        assertEquals(5, review.getRating());

        RestaurantResponse r1AfterReview = restaurantService.getRestaurantById(r1.getId());
        assertEquals(5.0, r1AfterReview.getAverageRating());
        assertEquals(1, r1AfterReview.getRatingCount());

        // 9. Update Order Status to DELIVERED and verify capacity freed
        OrderResponse deliveredOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());

        RestaurantResponse r1AfterDelivery = restaurantService.getRestaurantById(r1.getId());
        assertEquals(0, r1AfterDelivery.getCurrentActiveOrders()); // Capacity freed
    }
}
