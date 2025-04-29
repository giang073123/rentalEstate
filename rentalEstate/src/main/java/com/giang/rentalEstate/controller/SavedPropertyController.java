package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.service.SavedPropertyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

/**
 * This controller handles all operations related to saved properties
 */
@RestController
@RequestMapping("/api/saved-properties")
@RequiredArgsConstructor
@Tag(name = "Saved Property Controller", description = "APIs for saved property management")
public class SavedPropertyController {
    private final SavedPropertyService savedPropertyService;

    @Operation(
        summary = "Save a property",
        description = "Save a property to user's saved list. If the property is already saved, an error will be returned"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Property saved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Đã lưu bất động sản\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\":\"Resource Not Found Error\",\"message\":\"BĐS không tồn tại\",\"path\":\"/api/saved-properties/1\",\"status\":404}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Property already saved",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\":\"Property Already Saved Error\",\"message\":\"BĐS đã được lưu trước đó\",\"path\":\"/api/saved-properties/1\",\"status\":409}"
                )
            )
        )
    })
    @PostMapping("/{propertyId}")
    public ResponseEntity<?> saveProperty(
            @Schema(
                description = "Property ID",
                example = "1"
            ) @PathVariable Long propertyId,
            @AuthenticationPrincipal User currentUser) {
        savedPropertyService.saveProperty(currentUser, propertyId);
        return ResponseEntity.ok("Đã lưu bất động sản");
    }

    @Operation(
        summary = "Unsave a property",
        description = "Remove a property from user's saved list"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Property unsaved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Đã bỏ lưu bất động sản\""
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Property not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\":\"Resource Not Found Error\",\"message\":\"BĐS không tồn tại\",\"path\":\"/api/saved-properties/1\",\"status\":404}"
                )
            )
        )
    })
    @DeleteMapping("/{propertyId}")
    public ResponseEntity<?> unsaveProperty(
            @Schema(
                description = "Property ID",
                example = "1"
            ) @PathVariable Long propertyId,
            @AuthenticationPrincipal User currentUser) {
        savedPropertyService.unsaveProperty(currentUser, propertyId);
        return ResponseEntity.ok("Đã bỏ lưu bất động sản");
    }

    @Operation(
        summary = "Get saved properties",
        description = "Get all properties saved by the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Saved properties retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PropertyDTO.class),
                examples = @ExampleObject(
                    value = "[{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\",\"description\":\"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\"city\":\"Ho Chi Minh City\",\"district\":\"District 1\",\"ward\":\"Ben Nghe Ward\",\"street\":\"Nguyen Hue Street\",\"address\":\"123 Nguyen Hue Street, Ben Nghe Ward, District 1, HCMC\",\"pricePerMonth\":5000000,\"area\":50.5,\"bedrooms\":2,\"bathrooms\":1,\"floors\":1,\"type\":\"APARTMENT\",\"status\":\"AVAILABLE\",\"furnitureStatus\":\"FULLY_FURNISHED\",\"direction\":\"EAST\",\"moveInTime\":\"IMMEDIATELY\",\"electricityPrice\":\"PROVIDER_RATE\",\"waterPrice\":\"PROVIDER_RATE\",\"internetPrice\":\"PROVIDER_RATE\",\"amenities\":[\"CAMERA\",\"SECURITY_GUARD\",\"FIRE_SUPPRESSION_SYSTEM\"],\"images\":[{\"id\":1,\"imageUrl\":\"https://example.com/image1.jpg\"},{\"id\":2,\"imageUrl\":\"https://example.com/image2.jpg\"}],\"savedCount\":5,\"createdAt\":\"2024-03-20 10:30:00\",\"updatedAt\":\"2024-03-20 10:30:00\"}]"
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<?> getSavedProperties(@AuthenticationPrincipal User currentUser) {
        List<PropertyDTO> properties = savedPropertyService.getSavedProperties(currentUser);
        return ResponseEntity.ok(properties);

    }
}
