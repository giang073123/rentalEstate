package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.enums.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyUpdateDTO {
    private String title;
    @NotBlank(message = "Description is required")
    private String description;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "District is required")
    private String district;
    @NotBlank(message = "Ward is required")
    private String ward;
    private Double pricePerMonth;
    private int bedrooms;
    private int bathrooms;
    private int floors;
    //    @NotBlank(message = "Property type is required")
    private PropertyType type;
    //    @NotBlank(message = "Area is required")
    private double area;
    private FurnitureStatus furnitureStatus;
    private Direction direction;
    private MoveInTime moveInTime;
    private PricingOption electricityPrice;
    private PricingOption waterPrice;
    private PricingOption internetPrice;
    private List<Amenity> amenities;
}

