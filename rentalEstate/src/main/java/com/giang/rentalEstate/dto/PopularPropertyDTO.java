package com.giang.rentalEstate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularPropertyDTO {
    private PropertyDTO property;
    private long savedCount;
} 