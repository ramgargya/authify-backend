package com.auth.authify.io;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "OTP is required")
    private String otp;
    @NotBlank(message = "New password is required")
    private String newPassword;

    // Add any other fields you need for the reset password request
}
