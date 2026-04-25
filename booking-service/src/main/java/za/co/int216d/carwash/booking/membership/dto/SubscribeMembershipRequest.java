package za.co.int216d.carwash.booking.membership.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO for client to subscribe to a membership plan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeMembershipRequest {

    @NotNull(message = "Plan ID is required")
    @Positive(message = "Plan ID must be positive")
    private Long planId;

    @NotNull(message = "Auto-renewal preference is required")
    private Boolean autoRenew;
}
