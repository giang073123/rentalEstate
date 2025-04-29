package com.giang.rentalEstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User forgot password information")
public class UserForgotPwdDTO {
    @Email
    @NotBlank(message = "Email is required")
    @Schema(description = "Email of the user", example = "test@example.com")
    private String email;
}
