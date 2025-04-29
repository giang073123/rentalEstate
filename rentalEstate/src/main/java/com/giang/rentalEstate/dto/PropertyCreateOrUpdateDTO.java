package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Create a new property or update existing property")
public class PropertyCreateOrUpdateDTO {
    @Schema(description = "Title of the property", example = "Căn hộ cho thuê tại Quận 1")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Detailed description of the property", example = "Căn hộ đầy đủ tiện nghi, gần trung tâm")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "City where the property is located", example = "Hồ Chí Minh")
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "District where the property is located", example = "Quận 1")
    @NotBlank(message = "District is required")
    private String district;

    @Schema(description = "Ward where the property is located", example = "Phường Bến Nghé")
    @NotBlank(message = "Ward is required")
    private String ward;

    @Schema(description = "Street where the property is located", example = "Đường Nguyễn Huệ")
    private String street;

    @Schema(description = "Full address of the property", example = "123 Đường Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM")
    private String address;

    @Schema(description = "Monthly rental price in VND", example = "5000000")
    @NotNull(message = "Price is required")
    private Double pricePerMonth;

    @Schema(description = "Number of bedrooms", example = "2")
    private int bedrooms;

    @Schema(description = "Number of bathrooms", example = "1")
    private int bathrooms;

    @Schema(description = "Number of floors", example = "1")
    private int floors;

    @Schema(description = "Type of property", example = "APARTMENT")
    @NotNull(message = "Property type is required")
    private PropertyType type;

    @Schema(description = "Area of the property in square meters", example = "50.5")
    @NotNull(message = "Area is required")
    private double area;

    @Schema(description = "Furniture status of the property", example = "FULLY_FURNISHED")
    private FurnitureStatus furnitureStatus;

    @Schema(description = "Direction the property faces", example = "EAST")
    private Direction direction;

    @Schema(description = "Available move-in time", example = "IMMEDIATELY")
    private MoveInTime moveInTime;

    @Schema(description = "Electricity price option", example = "PROVIDER_RATE")
    private PricingOption electricityPrice;

    @Schema(description = "Water price option", example = "PROVIDER_RATE")
    private PricingOption waterPrice;

    @Schema(description = "Internet price option", example = "PROVIDER_RATE")
    private PricingOption internetPrice;

    @Schema(description = "List of amenities available in the property", example = "[\"CAMERA\", \"SECURITY_GUARD\", \"FIRE_SUPPRESSION_SYSTEM\"]")
    private List<Amenity> amenities;

    @Schema(description = "List of image URLs for the property", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> imageUrls;
}
