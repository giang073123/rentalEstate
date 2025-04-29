package com.giang.rentalEstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data //toString
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User update information")
public class UserUpdateDTO {
    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "[a-zA-Z][a-zA-Z ]+", message = "Name only contains letters")
    @Schema(description = "Full name of user", example = "John Doe")
    private String fullName;
    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "user1")
    private String username;
    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number of user", example = "1234567890")
    private String phone;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of user", example = "user1@example.com")
    private String email;
    @NotNull(message = "Role is required")
    @Schema(description = "Role ID of user", example = "1")
    private Long roleid;
}
