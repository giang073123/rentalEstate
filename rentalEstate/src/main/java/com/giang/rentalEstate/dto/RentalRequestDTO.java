package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.enums.RentalRequestStatus;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RentalRequestDTO {
    private Long id;
    private LocalDateTime createdAt;
    private RentalRequestStatus status;
    private User customer;
    private Property property;
}
