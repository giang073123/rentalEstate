package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.dto.RentalRequestDTO;
import com.giang.rentalEstate.enums.RentalRequestStatus;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.service.RentalRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controller handling rental request-related operations including sending, retrieving, and updating rental requests
 */ 
@RestController
@RequiredArgsConstructor
@Tag(name = "Rental Request Controller", description = "APIs for rental request management")
public class RentalRequestController {
    private final RentalRequestService rentalRequestService;

    /**
     * Send a rental request for a property
     * 
     * @param propertyId Property ID
     * @param user Current user
     * @return ResponseEntity containing the created rental request
     */
    @Operation(
        summary = "Send a rental request for a property",
        description = "Send a rental request for a property"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Send rental request successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequest.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"status\": \"PENDING\",\n" +
                        "  \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "  \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"BĐS không tồn tại\",\n" +
                        "  \"path\": \"/api/rental-request/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )    
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Rental request exceeds the limit",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Rental Request Limit Exceeded Error\",\n" +
                        "  \"message\": \"BĐS này đã đạt giới hạn yêu cầu thuê tối đa\",\n" +
                        "  \"path\": \"/api/rental-request/1\",\n" +
                        "  \"status\": 409\n" +
                        "}"
                )    
            )
        )
    })
    @PostMapping("/api/rental-request/{propertyId}")
    public ResponseEntity<?> sendRentalRequest(
            @Schema(description = "Property id", example = "1") @PathVariable Long propertyId,
            @AuthenticationPrincipal User user) {
        RentalRequest request = rentalRequestService.createRentalRequest(propertyId, user);
        return ResponseEntity.ok(request);
    }
    /**
     * Get rental requests for a property
     * 
     * @param propertyId Property ID
     * @return ResponseEntity containing the list of rental requests
     */
    @Operation(
        summary = "Retrieve rental requests for a property",
        description = "Retrieve rental requests for a property"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Retrieve rental requests successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequest.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"status\": \"PENDING\",\n" +
                        "    \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 2,\n" +
                        "    \"status\": \"SELECTED\",\n" +
                        "    \"createdAt\": \"2024-03-19 15:45:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 09:15:00\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"BĐS không tồn tại\",\n" +
                        "  \"path\": \"/api/rental-request/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )    
            )
        )
    })
    @GetMapping("/api/rental-request/{propertyId}")
    public ResponseEntity<?> getRentalRequest(
            @Schema(description = "Property id", example="1") @PathVariable Long propertyId
    ) {
        List<RentalRequestDTO> requests = rentalRequestService.getRentalRequest(propertyId);
        return ResponseEntity.ok(requests);

    }
    /**
     * Check the rental request status of a customer for a property
     * 
     * @param propertyId Property ID
     * @param user Current user
     * @return ResponseEntity containing the rental request status
     */
    @Operation(
        summary = "Get the rental request status of a customer for a property",
        description = "Get the rental request status of a customer for a property"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get rental request status successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequestStatus.class),
                examples = @ExampleObject(
                    value = "\"PENDING\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Property not found",
                        value = "{\n" +
                            "  \"error\": \"Resource Not Found Error\",\n" +
                            "  \"message\": \"BĐS không tồn tại\",\n" +
                            "  \"path\": \"/api/rental-request/1/customers\",\n" +
                            "  \"status\": 404\n" +
                            "}"
                    ),
                    @ExampleObject(
                        name = "Rental request not found",
                        value = "{\n" +
                            "  \"error\": \"Resource Not Found Error\",\n" +
                            "  \"message\": \"Không có yêu cầu thuê cho BĐS này\",\n" +
                            "  \"path\": \"/api/rental-request/1/customers\",\n" +
                            "  \"status\": 404\n" +
                            "}"
                    )
            }
            )
        )
    })
    @GetMapping("/api/rental-request/{propertyId}/customers")
    public ResponseEntity<?> checkRentalRequestStatusOfCustomer(
            @Schema(description = "Property id", example = "1") @PathVariable Long propertyId,
            @AuthenticationPrincipal User user
    ) {
        RentalRequestStatus status = rentalRequestService.checkRentalRequestStatusOfCustomer(propertyId, user);
        return ResponseEntity.ok(status);
    }

    /**
     * Cancel a rental request for a property
     * 
     * @param propertyId Property ID
     * @param user Current user
     * @return ResponseEntity containing the result message
     */ 
    @Operation(
        summary = "Cancel a rental request for a property",
        description = "Cancel a rental request for a property"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cancel rental request successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Hủy yêu cầu thành công\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Property not found",
                        value = "{\n" +
                            "  \"error\": \"Resource Not Found Error\",\n" +
                            "  \"message\": \"BĐS không tồn tại\",\n" +
                            "  \"path\": \"/api/rental-request/1\",\n" +
                            "  \"status\": 404\n" +
                            "}"
                    ),
                    @ExampleObject(
                        name = "Rental request not found",
                        value = "{\n" +
                            "  \"error\": \"Resource Not Found Error\",\n" +
                            "  \"message\": \"Yêu cầu thuê không tồn tại\",\n" +
                            "  \"path\": \"/api/rental-request/1\",\n" +
                            "  \"status\": 404\n" +
                            "}"
                    )
            }
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to cancel rental request",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"error\": \"Rental Request Not Authorized Error\",\n" +
                            "  \"message\": \"Bạn không có quyền hủy yêu cầu này\",\n" +
                            "  \"path\": \"/api/rental-request/1\",\n" +
                            "  \"status\": 403\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot change rental request status",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"error\": \"Rental Request Invalid Status Error\",\n" +
                            "  \"message\": \"Không thể hủy yêu cầu khi ở trạng thái hiện tại\",\n" +
                            "  \"path\": \"/api/rental-request/1\",\n" +
                            "  \"status\": 403\n" +
                            "}"
                )
            )
        )
    })
    @DeleteMapping("/api/rental-request/{propertyId}")
    public ResponseEntity<?> cancelRentalRequest(
            @Schema(description = "Property id", example = "1") @PathVariable Long propertyId,
            @AuthenticationPrincipal User user) {
        rentalRequestService.cancelRentalRequest(propertyId, user);
        return ResponseEntity.ok("Hủy yêu cầu thành công");

    }
    /**
     * Update the status of a rental request
     * 
     * @param requestId Request ID
     * @param status New status
     * @param user Current user
     * @return ResponseEntity containing the updated rental request
     */ 
    @Operation(
        summary = "Update the status of a rental request",
        description = "Update the status of a rental request"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Update rental request status successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequest.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"status\": \"SELECTED\",\n" +
                        "  \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "  \"updatedAt\": \"2024-03-20 11:15:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        name = "Rental request not found",
                        value = "{\n" +
                            "  \"error\": \"Resource Not Found Error\",\n" +
                            "  \"message\": \"Yêu cầu thuê không tồn tại\",\n" +
                            "  \"path\": \"/api/rental-request/1/status\",\n" +
                            "  \"status\": 404\n" +
                            "}"
                    )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Not authorized to update rental request status",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"error\": \"Rental Request Not Authorized Error\",\n" +
                            "  \"message\": \"Bạn không có quyền cập nhật trạng thái của yêu cầu thuê này\",\n" +
                            "  \"path\": \"/api/rental-request/1/status\",\n" +
                            "  \"status\": 403\n" +
                            "}"
                )
            )
        )
    })
    @PutMapping("/api/rental-request/{requestId}/status")
    public ResponseEntity<?> updateRentalRequestStatus(
            @Schema(description = "Rental request id", example = "1") @PathVariable Long requestId,
            @Schema(description = "Rental request status", example = "SELECTED") @RequestParam RentalRequestStatus status,
            @AuthenticationPrincipal User user) {
        RentalRequest request = rentalRequestService.updateRentalRequestStatus(requestId, status, user);
        return ResponseEntity.ok(request);

    }

    /**
     * Get current customer properties
     *
     * @param user Current user
     * @return ResponseEntity containing the list of customer properties
     */
    @Operation(
        summary = "Get properties to which current customers send requests",
        description = "Get properties to which current customers send requests"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Retrieve properties successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequestDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"status\": \"PENDING\",\n" +
                        "    \"property\": {\n" +
                        "      \"id\": 1,\n" +
                        "      \"title\": \"Căn hộ cho thuê\",\n" +
                        "      \"address\": \"123 Đường ABC, Quận 1, TP.HCM\",\n" +
                        "      \"price\": 5000000,\n" +
                        "      \"area\": 50,\n" +
                        "      \"bedrooms\": 2,\n" +
                        "      \"bathrooms\": 1,\n" +
                        "      \"status\": \"AVAILABLE\"\n" +
                        "    },\n" +
                        "    \"customer\": {\n" +
                        "      \"id\": 1,\n" +
                        "      \"fullName\": \"Nguyen Van A\",\n" +
                        "      \"email\": \"nguyenvana@example.com\",\n" +
                        "      \"phone\": \"0123456789\"\n" +
                        "    },\n" +
                        "    \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 2,\n" +
                        "    \"status\": \"SELECTED\",\n" +
                        "    \"property\": {\n" +
                        "      \"id\": 2,\n" +
                        "      \"title\": \"Nhà trọ cho thuê\",\n" +
                        "      \"address\": \"456 Đường XYZ, Quận 2, TP.HCM\",\n" +
                        "      \"price\": 3000000,\n" +
                        "      \"area\": 30,\n" +
                        "      \"bedrooms\": 1,\n" +
                        "      \"bathrooms\": 1,\n" +
                        "      \"status\": \"RENTED\"\n" +
                        "    },\n" +
                        "    \"customer\": {\n" +
                        "      \"id\": 2,\n" +
                        "      \"fullName\": \"Tran Van B\",\n" +
                        "      \"email\": \"tranvanb@example.com\",\n" +
                        "      \"phone\": \"0987654321\"\n" +
                        "    },\n" +
                        "    \"createdAt\": \"2024-03-19 15:45:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 09:15:00\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        )
    }
    )
    @GetMapping("api/rental-request/customer/properties")
    public ResponseEntity<?> getCurrentCustomerProperties(@AuthenticationPrincipal User user) {
        List<RentalRequestDTO> properties = rentalRequestService.getCustomerRequest(user);
        return ResponseEntity.ok(properties);



    }
    /**
     * Get customer properties
     * @param customerId customer id
     * @return ResponseEntity containing the list of properties to which customers send requests
     */
    @Operation(
        summary = "Get properties of current owner to which customers send requests",
        description = "Get properties of current owner to which customers send requests"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Retrieve properties successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RentalRequestDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"status\": \"PENDING\",\n" +
                        "    \"property\": {\n" +
                        "      \"id\": 1,\n" +
                        "      \"title\": \"Căn hộ cho thuê\",\n" +
                        "      \"address\": \"123 Đường ABC, Quận 1, TP.HCM\",\n" +
                        "      \"price\": 5000000,\n" +
                        "      \"area\": 50,\n" +
                        "      \"bedrooms\": 2,\n" +
                        "      \"bathrooms\": 1,\n" +
                        "      \"status\": \"AVAILABLE\"\n" +
                        "    },\n" +
                        "    \"customer\": {\n" +
                        "      \"id\": 1,\n" +
                        "      \"fullName\": \"Nguyen Van A\",\n" +
                        "      \"email\": \"nguyenvana@example.com\",\n" +
                        "      \"phone\": \"0123456789\"\n" +
                        "    },\n" +
                        "    \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 2,\n" +
                        "    \"status\": \"SELECTED\",\n" +
                        "    \"property\": {\n" +
                        "      \"id\": 2,\n" +
                        "      \"title\": \"Nhà trọ cho thuê\",\n" +
                        "      \"address\": \"456 Đường XYZ, Quận 2, TP.HCM\",\n" +
                        "      \"price\": 3000000,\n" +
                        "      \"area\": 30,\n" +
                        "      \"bedrooms\": 1,\n" +
                        "      \"bathrooms\": 1,\n" +
                        "      \"status\": \"RENTED\"\n" +
                        "    },\n" +
                        "    \"customer\": {\n" +
                        "      \"id\": 2,\n" +
                        "      \"fullName\": \"Tran Van B\",\n" +
                        "      \"email\": \"tranvanb@example.com\",\n" +
                        "      \"phone\": \"0987654321\"\n" +
                        "    },\n" +
                        "    \"createdAt\": \"2024-03-19 15:45:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 09:15:00\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Rental request not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                        value = "{\n" +
                            "  \"error\": \"Resource Not Found Error\",\n" +
                            "  \"message\": \"Yêu cầu thuê không tồn tại\",\n" +
                            "  \"path\": \"/api/rental-request/1/status\",\n" +
                            "  \"status\": 404\n" +
                            "}"
                    )
            )
        )
    })
    @GetMapping("api/rental-request/customer/{customerId}/properties")
    public ResponseEntity<?> getCustomerProperties(@PathVariable Long customerId, @AuthenticationPrincipal User user) {
        List<RentalRequestDTO> requestDTOS = rentalRequestService.getCustomerRequestOfOwner(customerId, user);
        return ResponseEntity.ok(requestDTOS);

    }

}
