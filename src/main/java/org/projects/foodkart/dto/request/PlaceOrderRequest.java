package org.projects.foodkart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.projects.foodkart.strategy.SelectionStrategyType;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long restaurantId;

    @Builder.Default
    private SelectionStrategyType selectionStrategy = SelectionStrategyType.LOWEST_PRICE;

    private String deliveryPinCode;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Builder.Default
    private List<OrderItemRequest> items = new ArrayList<>();
}
