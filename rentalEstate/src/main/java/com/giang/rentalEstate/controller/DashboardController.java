package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.dto.PopularPropertyDTO;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.service.PropertyService;
import com.giang.rentalEstate.service.RentalRequestService;
import com.giang.rentalEstate.service.SavedPropertyService;
import com.giang.rentalEstate.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller handling dashboard-related operations including retrieval of customer, owner, and admin dashboards
 */ 
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller", description = "APIs for dashboard display")
public class DashboardController {
    private final PropertyService propertyService;
    private final RentalRequestService rentalRequestService;
    private final UserService userService;
    private final SavedPropertyService savedPropertyService;
    /**
     * Get customer dashboard
     * 
     * @param currentUser Current user
     * @return ResponseEntity containing customer dashboard data
    */
    @Operation(
        summary = "Get customer dashboard",
        description = "Get dashboard statistics and saved properties for the current customer"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard data retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"stats\":{\"savedCount\":5,\"registeredCount\":3,\"pendingCount\":2},\"savedProperties\":[{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\",\"description\":\"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\"city\":\"Ho Chi Minh City\",\"district\":\"District 1\",\"ward\":\"Ben Nghe Ward\",\"street\":\"Nguyen Hue Street\",\"address\":\"123 Nguyen Hue Street, Ben Nghe Ward, District 1, HCMC\",\"pricePerMonth\":5000000,\"area\":50.5,\"bedrooms\":2,\"bathrooms\":1,\"floors\":1,\"type\":\"APARTMENT\",\"status\":\"AVAILABLE\",\"furnitureStatus\":\"FULLY_FURNISHED\",\"direction\":\"EAST\",\"moveInTime\":\"IMMEDIATELY\",\"electricityPrice\":\"PROVIDER_RATE\",\"waterPrice\":\"PROVIDER_RATE\",\"internetPrice\":\"PROVIDER_RATE\",\"amenities\":[\"CAMERA\",\"SECURITY_GUARD\",\"FIRE_SUPPRESSION_SYSTEM\"],\"images\":[{\"id\":1,\"imageUrl\":\"https://example.com/image1.jpg\"},{\"id\":2,\"imageUrl\":\"https://example.com/image2.jpg\"}],\"savedCount\":5,\"createdAt\":\"2024-03-20 10:30:00\",\"updatedAt\":\"2024-03-20 10:30:00\"}]}"
                )
            )
        )
    })
    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerDashboard(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();

        // Thay đổi viewedCount thành savedCount
        stats.put("savedCount", savedPropertyService.getSavedPropertiesCount(currentUser));
        stats.put("registeredCount", rentalRequestService.getActiveRequestsCount(currentUser));
        stats.put("pendingCount", rentalRequestService.getPendingRequestsCount(currentUser));
        // Lấy danh sách BĐS đã lưu thay vì đã xem
        List<PropertyDTO> savedProperties = savedPropertyService.getSavedProperties(currentUser);
        response.put("stats", stats);
        response.put("savedProperties", savedProperties);
        return ResponseEntity.ok(response);

    }
    /**
     * Get owner dashboard
     * 
     * @param currentUser Current user
     * @return ResponseEntity containing owner dashboard data
    */
    @Operation(
        summary = "Get owner dashboard",
        description = "Get dashboard statistics for the current property owner including property counts, request counts, and saved property statistics"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard data retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"stats\":{\"publishedCount\":10,\"requestCount\":5,\"savedCount\":25,\"savedStats\":[{\"date\":\"20/03\",\"count\":5},{\"date\":\"19/03\",\"count\":3},{\"date\":\"18/03\",\"count\":4},{\"date\":\"17/03\",\"count\":2},{\"date\":\"16/03\",\"count\":6},{\"date\":\"15/03\",\"count\":3},{\"date\":\"14/03\",\"count\":2}],\"recentRequests\":[{\"id\":1,\"status\":\"PENDING\",\"customer\":{\"id\":2,\"fullName\":\"Tran Van B\"},\"property\":{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\"},\"createdAt\":\"2024-03-20 10:30:00\"},{\"id\":2,\"status\":\"PENDING\",\"customer\":{\"id\":3,\"fullName\":\"Le Van C\"},\"property\":{\"id\":2,\"title\":\"Nhà trọ cho thuê\"},\"createdAt\":\"2024-03-19 15:45:00\"}]}}"
                )
            )
        )
    })
    @GetMapping("/owner")
    public ResponseEntity<?> getOwnerDashboard(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();

        // Các thống kê cho owner
        stats.put("publishedCount", propertyService.getOwnerPropertiesCount(currentUser));
        stats.put("requestCount", rentalRequestService.getOwnerPendingRequestsCount(currentUser));
        stats.put("savedCount", savedPropertyService.getOwnerSavedCount(currentUser));

        // Thống kê lượt quan tâm theo ngày (7 ngày gần nhất)
        stats.put("savedStats", savedPropertyService.getSavedStatsByDayForOwner(currentUser, 7));
        stats.put("recentRequests", rentalRequestService.getRecentRequestsForOwner(currentUser));

        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }
    /**
     * Get admin dashboard
     * 
     * @param user Current user
     * @return ResponseEntity containing admin dashboard data
    */
    @Operation(
        summary = "Get admin dashboard",
        description = "Get dashboard statistics for admin including user counts, property counts, percentages, and popular properties"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard data retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"stats\":{\"userCount\":100,\"approvedPropertyCount\":50,\"pendingPropertyCount\":10,\"userStats\":{\"ownerCount\":30,\"customerCount\":65,\"adminCount\":5},\"rentedPercentage\":60.5,\"acceptedRequestPercentage\":75.2,\"activeUserPercentage\":85.0},\"popularProperties\":[{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\",\"description\":\"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\"city\":\"Ho Chi Minh City\",\"district\":\"District 1\",\"ward\":\"Ben Nghe Ward\",\"street\":\"Nguyen Hue Street\",\"address\":\"123 Nguyen Hue Street, Ben Nghe Ward, District 1, HCMC\",\"pricePerMonth\":5000000,\"area\":50.5,\"bedrooms\":2,\"bathrooms\":1,\"floors\":1,\"type\":\"APARTMENT\",\"status\":\"AVAILABLE\",\"furnitureStatus\":\"FULLY_FURNISHED\",\"direction\":\"EAST\",\"moveInTime\":\"IMMEDIATELY\",\"electricityPrice\":\"PROVIDER_RATE\",\"waterPrice\":\"PROVIDER_RATE\",\"internetPrice\":\"PROVIDER_RATE\",\"amenities\":[\"CAMERA\",\"SECURITY_GUARD\",\"FIRE_SUPPRESSION_SYSTEM\"],\"images\":[{\"id\":1,\"imageUrl\":\"https://example.com/image1.jpg\"},{\"id\":2,\"imageUrl\":\"https://example.com/image2.jpg\"}],\"savedCount\":25,\"createdAt\":\"2024-03-20 10:30:00\",\"updatedAt\":\"2024-03-20 10:30:00\"}]}"
                )
            )
        )
    })
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminDashboard(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();

        // Các thống kê cho admin
        stats.put("userCount", userService.getTotalUserCount());
        stats.put("approvedPropertyCount", propertyService.getApprovedPropertiesCount());
        stats.put("pendingPropertyCount", propertyService.getPendingPropertiesCount());

        // Thống kê user
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("ownerCount", userService.countUsersByRole("OWNER"));
        userStats.put("customerCount", userService.countUsersByRole("CUSTOMER"));
        userStats.put("adminCount", userService.countUsersByRole("ADMIN"));
        stats.put("userStats", userStats);

        // Các tỷ lệ phần trăm
        stats.put("rentedPercentage", propertyService.getRentedPercentage());
        stats.put("acceptedRequestPercentage", rentalRequestService.getAcceptedRequestPercentage());
        stats.put("activeUserPercentage", userService.getActiveUserPercentage());

        // Lấy danh sách bất động sản nổi bật
        List<PopularPropertyDTO> popularProperties = propertyService.getPopularProperties(5);

        response.put("stats", stats);
        response.put("popularProperties", popularProperties);

        return ResponseEntity.ok(response);
    }
}