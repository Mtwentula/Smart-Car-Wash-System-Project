package za.co.int216d.carwash.booking.membership.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for returning membership plan details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlanResponse {

    private Long id;
    private String name;
    private String description;
    private Double monthlyPrice;
    private Integer creditsPerMonth;
    private Integer freeWashes;
    private Boolean isActive;
    private Boolean discountEligible;
    private Double discountPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
