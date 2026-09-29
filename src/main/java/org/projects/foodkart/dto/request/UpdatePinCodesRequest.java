package org.projects.foodkart.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePinCodesRequest {

    @NotEmpty(message = "Serviceable pin codes cannot be empty")
    @Builder.Default
    private Set<String> serviceablePinCodes = new HashSet<>();
}
