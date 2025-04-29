package com.giang.rentalEstate.dto;

import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequestDTO {
    private String username;
}
