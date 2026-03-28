package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor  // Needed for Jackson/JSON
@AllArgsConstructor // Allows: new CreateWalletRequest(uuid)
@Data
@Schema(description = "Request to create a new wallet for a user")
public class CreateWalletRequest {
    @NotNull
    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;
}