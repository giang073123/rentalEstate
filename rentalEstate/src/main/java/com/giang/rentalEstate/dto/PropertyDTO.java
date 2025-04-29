package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.enums.*;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDTO {
    private Long id;
    private String title;
    private String description;
    private String city;
    private String district;
    private String ward;
    private String street;
    private String address;
    private Double pricePerMonth;
    private int currentTenants;
    private int bedrooms;
    private int bathrooms;
    private int floors;
    private PropertyType type;
    private PropertyStatus status;
    private double area;
    private FurnitureStatus furnitureStatus;
    private Direction direction;
    private MoveInTime moveInTime;
    private PricingOption electricityPrice;
    private PricingOption waterPrice;
    private PricingOption internetPrice;
    private List<Amenity> amenities;
    private List<String> imageUrls;
    private User owner;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String rejectReason;
    private List<RentalRequest> rentalRequests;
    private int savedCount;
}
