package com.giang.rentalEstate.dto;

import com.giang.rentalEstate.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data //toString
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String fullName;
    private String username;
    private String phone;
    private String email;
    private Role role;
    private boolean enabled;
}
