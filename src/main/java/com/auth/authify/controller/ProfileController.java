package com.auth.authify.controller;

import com.auth.authify.io.ProfileRequest;
import com.auth.authify.io.ProfileResponse;
import com.auth.authify.service.EmailService;
import com.auth.authify.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    // construction based dependency injection
    private final ProfileService profileService;
    private final EmailService emailService;

    // create profile
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.createProfile(request);
        //TODO: send welcome email
        emailService.sendWelcomeEmail(response.getEmail(), response.getName());

        return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication.name") String email) {
        return profileService.getProfile(email);    }

}
