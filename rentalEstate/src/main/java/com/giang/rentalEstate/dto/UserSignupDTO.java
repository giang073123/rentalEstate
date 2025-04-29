package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data //toString
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User signup information")
public class UserSignupDTO {
    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "[a-zA-Z][a-zA-Z ]+", message = "Name only contains letters")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;
    @NotBlank(message = "Username is required")
    @Schema(description = "Username of the user", example = "user1")
    private String username;
    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number of the user", example = "1234567890")
    private String phone;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the user", example = "test@example.com")
    private String email;
    @NotBlank(message = "Password is required")
    @Schema(description = "Password of the user", example = "password123")
    private String password;
    @NotBlank(message = "Password must be retyped")
    @Schema(description = "Retype password of the user", example = "password123")
    private String retypePassword;
    @NotNull(message = "Role is required")
    @Schema(description = "Role of the user", example = "2")
    private Long roleid;


}
