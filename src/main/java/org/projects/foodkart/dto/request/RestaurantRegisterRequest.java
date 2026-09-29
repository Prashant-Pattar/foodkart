package org.projects.foodkart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRegisterRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String address;

    private String city;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;

    @NotEmpty(message = "At least one serviceable pin code is required")
    @Builder.Default
    private Set<String> serviceablePinCodes = new HashSet<>();

    @Valid
    @Builder.Default
    private List<MenuItemRequest> menuItems = new ArrayList<>();
}
