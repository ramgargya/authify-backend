package com.auth.authify.service;

import com.auth.authify.entity.UserEntity;
import com.auth.authify.io.ProfileRequest;
import com.auth.authify.io.ProfileResponse;
import com.auth.authify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
//@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        //convert ProfileRequest to UserEntity
        UserEntity newProfile = convertToUserEntity(request);
        // Check if the email already exists
        if(!userRepository.existsByEmail(request.getEmail())) {
            // Save user to the database
            newProfile = userRepository.save(newProfile);
            // Convert UserEntity to ProfileResponse
            return convertToProfileResponse(newProfile);
        }
        // If email already exists, throw an exception
        throw  new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // generating 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // calculated expiry time
        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);

        //update the profile
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpireAt(expiryTime);

        //save into the database
        userRepository.save(existingEntity);

        try {
            // TODO: send reset otp email
            emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Unable to send reset otp");
        }


    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if(existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid otp");
        }
        if(existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("Otp expired");
        }

        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if(existingUser.isAccountVerified()) {
            return;
        }

        // generating 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        // calculated expiry time, 24 Hours
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        //update the profile
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);

        //save into the database
        emailService.sendOtpEmail(existingUser.getEmail(), otp);
        userRepository.save(existingUser);

    }

    @Override
    public void verifyOtp(String email, String otp) {
        // Fetch the user by email, throw exception if not found
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if the OTP is null or does not match the provided OTP
        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new RuntimeException("Invalid otp");
        }

        // Check if the OTP has expired
        if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("Otp expired");
        }

        // Mark the account as verified and clear the OTP and its expiry time
        existingUser.setAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);

        // Save the updated user entity back to the database
        userRepository.save(existingUser);
    }


    //convert ProfileRequest to UserEntity
    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }


    //convert UserEntity to ProfileResponse
    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.isAccountVerified())
                .build();
    }

}
