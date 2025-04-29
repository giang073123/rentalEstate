package com.giang.rentalEstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User reset password information")
public class UserResetPwdDTO {
    @NotBlank(message = "New password is required")
    @Schema(description = "New password of user", example = "password123")
    private String newPassword;
    @NotBlank(message = "Password must be retyped")
    @Schema(description = "Retyped password of user", example = "password123")
    private String retypePassword;
}
