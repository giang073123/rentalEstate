package com.giang.rentalEstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User login information")
public class UserLoginDTO {
    @NotBlank(message = "Username is required")
    @Schema(description = "Username of the user", example = "user1")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password of the user", example = "password123")
    private String password;
}
