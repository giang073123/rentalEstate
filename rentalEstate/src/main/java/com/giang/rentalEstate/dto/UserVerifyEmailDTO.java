package com.giang.rentalEstate.dto;

import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserVerifyEmailDTO {
    private String email;
    private String verificationCode;
}
