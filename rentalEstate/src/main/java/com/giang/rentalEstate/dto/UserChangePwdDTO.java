package com.giang.rentalEstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User change password information")
public class UserChangePwdDTO {
    @NotBlank(message = "Old password is required")
    @Schema(description = "Old password of the user", example = "password123")
    private String oldPassword;
    @NotBlank(message = "New password is required")
    @Schema(description = "New password of the user", example = "password1234")
    private String newPassword;
    @NotBlank(message = "Password must be retyped")
    @Schema(description = "Retype password of the user", example = "password1234")
    private String retypePassword;
}
