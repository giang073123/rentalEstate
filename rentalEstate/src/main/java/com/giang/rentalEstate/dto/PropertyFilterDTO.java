package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.enums.*;
import com.giang.rentalEstate.model.User;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO containing property filtering criteria")
public class PropertyFilterDTO {
    @Schema(
        description = "Area range in square meters",
        example = "{\"min\": 30.0, \"max\": 100.0}"
    )
    private Map<Double, Double> areaRange = new HashMap<>();

    @Schema(
        description = "Price range in VND per month",
        example = "{\"min\": 3000000.0, \"max\": 10000000.0}"
    )
    private Map<Double, Double> priceRange = new HashMap<>();

    @Schema(
        description = "Number of bedrooms",
        example = "[1, 2, 3]"
    )
    private List<Integer> bedrooms = new ArrayList<>();

    @Schema(
        description = "List of cities",
        example = "[\"Ho Chi Minh City\", \"Hanoi\"]"
    )
    private List<String> city = new ArrayList<>();

    @Schema(
        description = "List of districts",
        example = "[\"District 1\", \"District 2\"]"
    )
    private List<String> district = new ArrayList<>();

    @Schema(
        description = "List of wards",
        example = "[\"Ben Nghe Ward\", \"Da Kao Ward\"]"
    )
    private List<String> ward = new ArrayList<>();

    @Schema(
        description = "Property direction",
        example = "[\"EAST\", \"WEST\", \"NORTH\", \"SOUTH\"]"
    )
    private List<String> direction = new ArrayList<>();

    @Schema(
        description = "Property type",
        example = "[\"APARTMENT\", \"VILLA\"]"
    )
    private List<String> type = new ArrayList<>();

    @Schema(
        description = "Electricity price option",
        example = "PROVIDER_RATE"
    )
    private PricingOption electricityPrice;

    @Schema(
        description = "Internet price option",
        example = "PROVIDER_RATE"
    )
    private PricingOption internetPrice;

    @Schema(
        description = "Water price option",
        example = "PROVIDER_RATE"
    )
    private PricingOption waterPrice;

    @Schema(
        description = "Move-in time",
        example = "IMMEDIATELY"
    )
    private MoveInTime moveInTime;
}
