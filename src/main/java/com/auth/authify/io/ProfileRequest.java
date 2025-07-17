package com.auth.authify.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "Name should not be empty")
    private String name;
    @Email(message = "Enter a valid email")
    @NotNull(message = "Email should not be empty")
    private String email;
    @Size(min = 6, message = "Password must be atleast 6 characters")
    private String password;
}
