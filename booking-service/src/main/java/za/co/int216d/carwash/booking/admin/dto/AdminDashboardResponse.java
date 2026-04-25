package za.co.int216d.carwash.booking.admin.dto;

import lombok.*;

/**
 * Dashboard statistics overview
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private Long totalActiveMemberships;
    private Long totalSuspendedMemberships;
    private Long totalExpiredMemberships;
    private Long totalCancelledMemberships;
    private Long totalMembers;
    private Double totalMonthlyRevenue;
    private Double averagePlanPrice;
    private Integer totalPlans;
    private Integer activePlans;
}
