package org.projects.foodkart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuRequest {

    @NotEmpty(message = "Menu items list cannot be empty")
    @Valid
    @Builder.Default
    private List<MenuItemRequest> items = new ArrayList<>();
}
