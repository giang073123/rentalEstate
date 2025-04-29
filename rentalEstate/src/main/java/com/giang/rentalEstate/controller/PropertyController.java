package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.dto.PropertyCreateOrUpdateDTO;
import com.giang.rentalEstate.dto.PropertyDTO;
import com.giang.rentalEstate.dto.PropertyFilterDTO;
import com.giang.rentalEstate.enums.PropertyStatus;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.giang.rentalEstate.service.PropertyService;
import com.giang.rentalEstate.service.impl.GoogleDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Controller handling property-related operations including creation, retrieval, updating, and deletion of properties
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Property Controller", description = "APIs for property management")
public class PropertyController {
    private final PropertyService propertyService;
    private final GoogleDriveService googleDriveService;
//    private final PropertySearchService propertySearchService;
    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    /**
     * Create a new property with images
     * @param propertyCreateOrUpdateDTO DTO contains information of property    
     * @param images List of images
     * @param user Current user from SecurityContext
     * @param result Validation result
     * @return ResponseEntity containing created property or errors
     */
    @Operation(
        summary = "Create a new property",
        description = "Create a new property with images"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Property created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Property.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"title\": \"Căn hộ cho thuê tại Quận 1\",\n" +
                        "  \"description\": \"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\n" +
                        "  \"address\": \"123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM\",\n" +
                        "  \"pricePerMonth\": 5000000,\n" +
                        "  \"area\": 50.5,\n" +
                        "  \"bedrooms\": 2,\n" +
                        "  \"bathrooms\": 1,\n" +
                        "  \"floors\": 1,\n" +
                        "  \"type\": \"APARTMENT\",\n" +
                        "  \"status\": \"PENDING_REVIEW\",\n" +
                        "  \"furnitureStatus\": \"FULLY_FURNISHED\",\n" +
                        "  \"direction\": \"EAST\",\n" +
                        "  \"moveInTime\": \"IMMEDIATELY\",\n" +
                        "  \"electricityPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"waterPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"internetPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"amenities\": [\"CAMERA\", \"SECURITY_GUARD\", \"FIRE_SUPPRESSION_SYSTEM\"],\n" +
                        "  \"images\": [\n" +
                        "    {\n" +
                        "      \"id\": 1,\n" +
                        "      \"imageUrl\": \"https://example.com/image1.jpg\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": 2,\n" +
                        "      \"imageUrl\": \"https://example.com/image2.jpg\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "  \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        value = "[\n" +
                            "  \"Title is required\",\n" +
                            "  \"Description is required\",\n" +
                            "  \"City is required\"\n" +
                            "]"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Internal Server Error\",\n" +
                        "  \"message\": \"Lỗi khi upload ảnh: Không thể kết nối đến Google Drive\",\n" +
                        "  \"path\": \"/api/property\",\n" +
                        "  \"status\": 500\n" +
                        "}"
                )
            )
        )
    })

    @PostMapping("/api/property")
    public ResponseEntity<?> createProperty(
            @RequestPart("property") @Valid PropertyCreateOrUpdateDTO propertyCreateOrUpdateDTO,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal User user, 
            BindingResult result) {
        // Add logging
        logger.info("Received request to create property");
        logger.info("Authenticated user: {}", user);
        logger.info("Property data: {}", propertyCreateOrUpdateDTO);
        List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList());
            if(!errorMessages.isEmpty()){
                return ResponseEntity.badRequest().body(errorMessages);
            }
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    logger.info("Đang upload ảnh: {}", image.getOriginalFilename());
                    String imageUrl = googleDriveService.uploadFile(image);
                    imageUrls.add(imageUrl);
                    logger.info("Upload thành công, URL: {}", imageUrl);
                } catch (Exception e) {
                    logger.error("Lỗi khi upload ảnh: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Lỗi khi upload ảnh: " + e.getMessage());
                }
            }

            Property createdProperty = propertyService.createProperty(propertyCreateOrUpdateDTO, user, imageUrls);
            logger.info("Đã tạo property thành công với ID: {}", createdProperty.getId());

            return ResponseEntity.ok(createdProperty);

    }
    /**
     * Get all properties owned by the current user
     * 
     * @param user Current authenticated user
     * @return ResponseEntity containing list of user's properties
     */
    @Operation(
        summary = "Get properties of current owner",
        description = "Get all properties owned by the current user. If user is admin, returns all properties"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Retrieve properties successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PropertyDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"title\": \"Căn hộ cho thuê tại Quận 1\",\n" +
                        "    \"description\": \"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\n" +
                        "    \"address\": \"123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM\",\n" +
                        "    \"pricePerMonth\": 5000000,\n" +
                        "    \"area\": 50.5,\n" +
                        "    \"bedrooms\": 2,\n" +
                        "    \"bathrooms\": 1,\n" +
                        "    \"floors\": 1,\n" +
                        "    \"type\": \"APARTMENT\",\n" +
                        "    \"status\": \"AVAILABLE\",\n" +
                        "    \"furnitureStatus\": \"FULLY_FURNISHED\",\n" +
                        "    \"direction\": \"EAST\",\n" +
                        "    \"moveInTime\": \"IMMEDIATELY\",\n" +
                        "    \"electricityPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"waterPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"internetPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"amenities\": [\"CAMERA\", \"SECURITY_GUARD\", \"FIRE_SUPPRESSION_SYSTEM\"],\n" +
                        "    \"images\": [\n" +
                        "      {\n" +
                        "        \"id\": 1,\n" +
                        "        \"imageUrl\": \"https://example.com/image1.jpg\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": 2,\n" +
                        "        \"imageUrl\": \"https://example.com/image2.jpg\"\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"savedCount\": 5,\n" +
                        "    \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 2,\n" +
                        "    \"title\": \"Nhà trọ cho thuê\",\n" +
                        "    \"description\": \"Nhà trọ giá rẻ, gần chợ\",\n" +
                        "    \"address\": \"456 Đường XYZ, Quận 2, TP.HCM\",\n" +
                        "    \"pricePerMonth\": 3000000,\n" +
                        "    \"area\": 30,\n" +
                        "    \"bedrooms\": 1,\n" +
                        "    \"bathrooms\": 1,\n" +
                        "    \"floors\": 1,\n" +
                        "    \"type\": \"HOUSE\",\n" +
                        "    \"status\": \"RENTED\",\n" +
                        "    \"furnitureStatus\": \"PARTIALLY_FURNISHED\",\n" +
                        "    \"direction\": \"WEST\",\n" +
                        "    \"moveInTime\": \"NEXT_MONTH\",\n" +
                        "    \"electricityPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"waterPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"internetPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"amenities\": [\"WIFI\", \"PARKING\"],\n" +
                        "    \"images\": [\n" +
                        "      {\n" +
                        "        \"id\": 3,\n" +
                        "        \"imageUrl\": \"https://example.com/image3.jpg\"\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"savedCount\": 2,\n" +
                        "    \"createdAt\": \"2024-03-19 15:45:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 09:15:00\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No properties found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Bạn không có BĐS nào\",\n" +
                        "  \"path\": \"/api/property/me\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )
            )
        )
    })
    @GetMapping("/api/property/me")
    public ResponseEntity<?> getApprovedPropertiesOfOwner(@AuthenticationPrincipal User user){
        List<PropertyDTO> properties = propertyService.getPropertiesOfOwner(user);
        return ResponseEntity.ok(properties);
    }
    /**
     * Get property details based on property id
     * 
     * @param id Property id
     * @param user Current user
     * @return ResponseEntity containing property details
     */ 
    @Operation(
        summary = "Get property details by ID",
        description = "Get detailed information of a property by its ID. Access is restricted based on user role and property status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Retrieve property successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PropertyDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"title\": \"Căn hộ cho thuê tại Quận 1\",\n" +
                        "  \"description\": \"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\n" +
                        "  \"city\": \"Hồ Chí Minh\",\n" +
                        "  \"district\": \"Quận 1\",\n" +
                        "  \"ward\": \"Phường Bến Nghé\",\n" +
                        "  \"street\": \"Đường Nguyễn Huệ\",\n" +
                        "  \"address\": \"123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM\",\n" +
                        "  \"pricePerMonth\": 5000000,\n" +
                        "  \"currentTenants\": 1,\n" +
                        "  \"bedrooms\": 2,\n" +
                        "  \"bathrooms\": 1,\n" +
                        "  \"floors\": 1,\n" +
                        "  \"type\": \"APARTMENT\",\n" +
                        "  \"status\": \"AVAILABLE\",\n" +
                        "  \"area\": 50.5,\n" +
                        "  \"furnitureStatus\": \"FULLY_FURNISHED\",\n" +
                        "  \"direction\": \"EAST\",\n" +
                        "  \"moveInTime\": \"IMMEDIATELY\",\n" +
                        "  \"electricityPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"waterPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"internetPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"amenities\": [\"CAMERA\", \"SECURITY_GUARD\", \"FIRE_SUPPRESSION_SYSTEM\"],\n" +
                        "  \"imageUrls\": [\n" +
                        "    \"https://example.com/image1.jpg\",\n" +
                        "    \"https://example.com/image2.jpg\"\n" +
                        "  ],\n" +
                        "  \"owner\": {\n" +
                        "    \"id\": 1,\n" +
                        "    \"fullName\": \"Nguyen Van A\",\n" +
                        "    \"email\": \"nguyenvana@example.com\",\n" +
                        "    \"phone\": \"0123456789\"\n" +
                        "  },\n" +
                        "  \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "  \"reviewedAt\": \"2024-03-20 11:00:00\",\n" +
                        "  \"rejectReason\": null,\n" +
                        "  \"rentalRequests\": [\n" +
                        "    {\n" +
                        "      \"id\": 1,\n" +
                        "      \"status\": \"PENDING\",\n" +
                        "      \"customer\": {\n" +
                        "        \"id\": 2,\n" +
                        "        \"fullName\": \"Tran Van B\"\n" +
                        "      },\n" +
                        "      \"createdAt\": \"2024-03-20 12:00:00\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"savedCount\": 5\n" +
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
                        "  \"path\": \"/api/property/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Customer access denied",
                        value = "{\n" +
                            "  \"error\": \"Access Denied Error\",\n" +
                            "  \"message\": \"Bạn không có quyền xem BĐS này\",\n" +
                            "  \"path\": \"/api/property/1\",\n" +
                            "  \"status\": 403\n" +
                            "}"
                    ),
                    @ExampleObject(
                        name = "Owner access denied",
                        value = "{\n" +
                            "  \"error\": \"Access Denied Error\",\n" +
                            "  \"message\": \"Bạn không có quyền xem BĐS này\",\n" +
                            "  \"path\": \"/api/property/1\",\n" +
                            "  \"status\": 403\n" +
                            "}"
                    )
                }
            )
        )
    })
    @GetMapping("/api/property/{id}")
    public ResponseEntity<?> getPropertyFromId(
            @Schema(description = "Property ID", example = "1") @PathVariable Long id, 
            @AuthenticationPrincipal User user){
        PropertyDTO property = propertyService.getPropertyFromId(id, user);
        return ResponseEntity.ok(property);

    }
    /**
     * Delete property based on property id
     * 
     * @param id Property id
     * @param user Current user
     * @return ResponseEntity containing result message
     */ 
    @Operation(
        summary = "Delete a property",
        description = "Delete a property by its ID. Only the owner of the property can delete it"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Property deleted successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Đã xóa bất động sản thành công\""
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
                        "  \"path\": \"/api/property/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Access Denied Error\",\n" +
                        "  \"message\": \"Bạn không có quyền xóa BĐS này\",\n" +
                        "  \"path\": \"/api/property/1\",\n" +
                        "  \"status\": 403\n" +
                        "}"
                )
            )
        )
    })
    @DeleteMapping("/api/property/{id}")
    public ResponseEntity<?> deletePropertyFromId(
            @Schema(description = "Property ID", example = "1") @PathVariable Long id, 
            @AuthenticationPrincipal User user) {
        propertyService.deletePropertyFromId(id, user);
        return ResponseEntity.ok("Đã xóa bất động sản thành công");
    }
    /**
     * Update property based on property id
     * 
     * @param propertyCreateOrUpdateDTO DTO contains information of property
     * @param images List of images
     * @param keepImageIdsJson List of image ids to keep    
     * @param id Property id
     * @param user Current user
     * @param result Validation result
     * @return ResponseEntity containing updated property or errors
     */ 
    @Operation(
        summary = "Update a property",
        description = "Update a property by its ID. Only the owner can update their property. Updating an approved property will set it back to pending review"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Property updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Property.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"title\": \"Căn hộ cho thuê tại Quận 1\",\n" +
                        "  \"description\": \"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\n" +
                        "  \"city\": \"Hồ Chí Minh\",\n" +
                        "  \"district\": \"Quận 1\",\n" +
                        "  \"ward\": \"Phường Bến Nghé\",\n" +
                        "  \"street\": \"Đường Nguyễn Huệ\",\n" +
                        "  \"address\": \"123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM\",\n" +
                        "  \"pricePerMonth\": 5000000,\n" +
                        "  \"area\": 50.5,\n" +
                        "  \"bedrooms\": 2,\n" +
                        "  \"bathrooms\": 1,\n" +
                        "  \"floors\": 1,\n" +
                        "  \"type\": \"APARTMENT\",\n" +
                        "  \"status\": \"PENDING_REVIEW\",\n" +
                        "  \"furnitureStatus\": \"FULLY_FURNISHED\",\n" +
                        "  \"direction\": \"EAST\",\n" +
                        "  \"moveInTime\": \"IMMEDIATELY\",\n" +
                        "  \"electricityPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"waterPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"internetPrice\": \"PROVIDER_RATE\",\n" +
                        "  \"amenities\": [\"CAMERA\", \"SECURITY_GUARD\", \"FIRE_SUPPRESSION_SYSTEM\"],\n" +
                        "  \"images\": [\n" +
                        "    {\n" +
                        "      \"id\": 1,\n" +
                        "      \"imageUrl\": \"https://example.com/image1.jpg\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"id\": 2,\n" +
                        "      \"imageUrl\": \"https://example.com/image2.jpg\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "  \"updatedAt\": \"2024-03-20 11:15:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        value = "[\n" +
                            "  \"Title is required\",\n" +
                            "  \"Description is required\",\n" +
                            "  \"City is required\"\n" +
                            "]"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Image Upload Error",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        value = "{\n" +
                            "  \"error\": \"Internal Server Error\",\n" +
                            "  \"message\": \"Lỗi khi upload ảnh: Không thể kết nối đến Google Drive\",\n" +
                            "  \"path\": \"/api/property/1\",\n" +
                            "  \"status\": 500\n" +
                            "}"
                    )
                }
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
                        "  \"path\": \"/api/property/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Access Denied Error\",\n" +
                        "  \"message\": \"Bạn không phải chủ của BĐS này\",\n" +
                        "  \"path\": \"/api/property/1\",\n" +
                        "  \"status\": 403\n" +
                        "}"
                )
            )
        )
    })
    @PutMapping("/api/property/{id}")
    public ResponseEntity<?> updateProperty(
            @RequestPart("property") @Valid PropertyCreateOrUpdateDTO propertyCreateOrUpdateDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "keepImageIds", required = false) String keepImageIdsJson,
            @Schema(description = "Property ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal User user,
            BindingResult result) {
        try {
            List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList());
            if (!errorMessages.isEmpty()) {
                return ResponseEntity.badRequest().body(errorMessages);
            }

            List<String> newImageUrls = new ArrayList<>();
            // Chỉ xử lý upload ảnh mới nếu có ảnh được gửi lên
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    String imageUrl = googleDriveService.uploadFile(image);
                    newImageUrls.add(imageUrl);
                }
            }

            Property updatedProperty = propertyService.updateProperty(propertyCreateOrUpdateDTO, id, user, newImageUrls, keepImageIdsJson);
            return ResponseEntity.ok(updatedProperty);
        } catch (IOException | GeneralSecurityException e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    /**
     * Update property status based on property id
     * 
     * @param id Property id
     * @param rejectReason Reject reason
     * @param status New status
     * @param user Current user
     * @return ResponseEntity containing result message
     */
    @Operation(
        summary = "Update property status",
        description = "Update the status of a property. Only admin can update property status. Property must be in PENDING_REVIEW status to be updated"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Property status updated successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "\"Cập nhật trạng thái thành công\""
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
                        "  \"path\": \"/api/property/1/status\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Property already reviewed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Property Already Reviewed Error\",\n" +
                        "  \"message\": \"Bất động sản này đã được duyệt bởi Nguyen Van A vào lúc 2024-03-20 10:30:00\",\n" +
                        "  \"path\": \"/api/property/1/status\",\n" +
                        "  \"status\": 409\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Property is being reviewed by another admin",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Property Locked Error\",\n" +
                        "  \"message\": \"Property này đang được admin khác chỉnh sửa. Vui lòng thử lại sau.\",\n" +
                        "  \"path\": \"/api/property/1/status\",\n" +
                        "  \"status\": 423\n" +
                        "}"
                )
            )
        )
    })
    @PutMapping("/api/property/{id}/status")
    public ResponseEntity<?> updatePropertyStatus(
            @Schema(description = "Property ID", example = "1") @PathVariable Long id,
            @Schema(description = "Reject reason", example = "Thông tin không chính xác") @RequestParam(required = false) String rejectReason,
            @Schema(description = "New status", example = "APPROVED") @RequestParam PropertyStatus status,
            @AuthenticationPrincipal User user) {
        propertyService.updatePropertyStatus(id, status, rejectReason, user);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
    /**
     * Filter properties based on filter criteria
     * 
     * @param propertyFilterDTO Filter criteria
     * @return ResponseEntity containing list of filtered properties
     */ 
    @Operation(
        summary = "Filter properties",
        description = "Filter properties based on various criteria such as area, price, location, etc. Only approved properties will be returned"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Properties filtered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PropertyDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"title\": \"Căn hộ cho thuê tại Quận 1\",\n" +
                        "    \"description\": \"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\n" +
                        "    \"city\": \"Hồ Chí Minh\",\n" +
                        "    \"district\": \"Quận 1\",\n" +
                        "    \"ward\": \"Phường Bến Nghé\",\n" +
                        "    \"street\": \"Đường Nguyễn Huệ\",\n" +
                        "    \"address\": \"123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM\",\n" +
                        "    \"pricePerMonth\": 5000000,\n" +
                        "    \"area\": 50.5,\n" +
                        "    \"bedrooms\": 2,\n" +
                        "    \"bathrooms\": 1,\n" +
                        "    \"floors\": 1,\n" +
                        "    \"type\": \"APARTMENT\",\n" +
                        "    \"status\": \"AVAILABLE\",\n" +
                        "    \"furnitureStatus\": \"FULLY_FURNISHED\",\n" +
                        "    \"direction\": \"EAST\",\n" +
                        "    \"moveInTime\": \"IMMEDIATELY\",\n" +
                        "    \"electricityPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"waterPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"internetPrice\": \"PROVIDER_RATE\",\n" +
                        "    \"amenities\": [\"CAMERA\", \"SECURITY_GUARD\", \"FIRE_SUPPRESSION_SYSTEM\"],\n" +
                        "    \"images\": [\n" +
                        "      {\n" +
                        "        \"id\": 1,\n" +
                        "        \"imageUrl\": \"https://example.com/image1.jpg\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": 2,\n" +
                        "        \"imageUrl\": \"https://example.com/image2.jpg\"\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"savedCount\": 5,\n" +
                        "    \"createdAt\": \"2024-03-20 10:30:00\",\n" +
                        "    \"updatedAt\": \"2024-03-20 10:30:00\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        )
    })
    @PostMapping("/api/property/filter")
    public ResponseEntity<?> filterProperties(@RequestBody PropertyFilterDTO propertyFilterDTO) {
        List<PropertyDTO> properties = propertyService.filterProperties(propertyFilterDTO);
        return ResponseEntity.ok(properties);
    }
    /**
     * Search properties based on keyword
     * 
     * @param keyword Keyword
     * @return ResponseEntity containing list of properties
     */     
    @Operation(
        summary = "Search properties",
        description = "Search properties by keyword in title, description, address, etc. Only approved properties will be returned"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Properties found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PropertyDTO.class),
                examples = @ExampleObject(
                    value = "[{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\",\"description\":\"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\"city\":\"Ho Chi Minh City\",\"district\":\"District 1\",\"ward\":\"Ben Nghe Ward\",\"street\":\"Nguyen Hue Street\",\"address\":\"123 Nguyen Hue Street, Ben Nghe Ward, District 1, HCMC\",\"pricePerMonth\":5000000,\"area\":50.5,\"bedrooms\":2,\"bathrooms\":1,\"floors\":1,\"type\":\"APARTMENT\",\"status\":\"AVAILABLE\",\"furnitureStatus\":\"FULLY_FURNISHED\",\"direction\":\"EAST\",\"moveInTime\":\"IMMEDIATELY\",\"electricityPrice\":\"PROVIDER_RATE\",\"waterPrice\":\"PROVIDER_RATE\",\"internetPrice\":\"PROVIDER_RATE\",\"amenities\":[\"CAMERA\",\"SECURITY_GUARD\",\"FIRE_SUPPRESSION_SYSTEM\"],\"images\":[{\"id\":1,\"imageUrl\":\"https://example.com/image1.jpg\"},{\"id\":2,\"imageUrl\":\"https://example.com/image2.jpg\"}],\"savedCount\":5,\"createdAt\":\"2024-03-20 10:30:00\",\"updatedAt\":\"2024-03-20 10:30:00\"}]"
                )
            )
        )
    })
    @GetMapping("/api/search")
    public ResponseEntity<List<PropertyDTO>> searchProperties(
        @Schema(
            description = "Search keyword",
            example = "chung cư quận 1"
        ) @RequestParam String keyword) {
        return ResponseEntity.ok(propertyService.searchProperties(keyword));
    }

    @Operation(
        summary = "Get all approved properties",
        description = "Get all properties that have been approved by admin. The response will be filtered based on user role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Properties retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PropertyDTO.class),
                examples = @ExampleObject(
                    value = "[{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\",\"description\":\"Căn hộ đầy đủ tiện nghi, gần trung tâm\",\"city\":\"Ho Chi Minh City\",\"district\":\"District 1\",\"ward\":\"Ben Nghe Ward\",\"street\":\"Nguyen Hue Street\",\"address\":\"123 Nguyen Hue Street, Ben Nghe Ward, District 1, HCMC\",\"pricePerMonth\":5000000,\"area\":50.5,\"bedrooms\":2,\"bathrooms\":1,\"floors\":1,\"type\":\"APARTMENT\",\"status\":\"AVAILABLE\",\"furnitureStatus\":\"FULLY_FURNISHED\",\"direction\":\"EAST\",\"moveInTime\":\"IMMEDIATELY\",\"electricityPrice\":\"PROVIDER_RATE\",\"waterPrice\":\"PROVIDER_RATE\",\"internetPrice\":\"PROVIDER_RATE\",\"amenities\":[\"CAMERA\",\"SECURITY_GUARD\",\"FIRE_SUPPRESSION_SYSTEM\"],\"images\":[{\"id\":1,\"imageUrl\":\"https://example.com/image1.jpg\"},{\"id\":2,\"imageUrl\":\"https://example.com/image2.jpg\"}],\"savedCount\":5,\"createdAt\":\"2024-03-20 10:30:00\",\"updatedAt\":\"2024-03-20 10:30:00\"}]"
                )
            )
        )
    })
    @GetMapping("/api/property")
    public ResponseEntity<?> getApprovedProperties(@AuthenticationPrincipal User user){
        List<PropertyDTO> properties = propertyService.getAllProperties(user);
        return ResponseEntity.ok(properties);
    }
}
