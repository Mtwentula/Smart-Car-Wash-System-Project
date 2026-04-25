package za.co.int216d.carwash.booking.admin.dto;

import lombok.*;

/**
 * Membership plan analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanAnalyticsResponse {
    private Long planId;
    private String planName;
    private Double monthlyPrice;
    private Long activeSubscriptions;
    private Long totalSubscriptions;
    private Double totalMonthlyRevenue;
    private Double conversionRate;
}
