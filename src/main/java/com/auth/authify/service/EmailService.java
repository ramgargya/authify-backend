package com.auth.authify.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;


    public void sendWelcomeEmail(String toEmail, String name) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 Welcome to Authify");

            // Prepare context for Thymeleaf
            Context context = new Context();
            context.setVariable("name", name);

            // Render HTML using Thymeleaf
            String htmlContent = templateEngine.process("welcome-email", context);

            helper.setText(htmlContent, true); // true = is HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }


    public void sendResetOtpEmail(String toEmail, String otp) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🔒 Password Reset Request - Authify");

            // Prepare Thymeleaf context
            Context context = new Context();
            context.setVariable("otp", otp);

            // Render HTML from template
            String htmlContent = templateEngine.process("reset-otp-email", context);

            helper.setText(htmlContent, true); // Enable HTML
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset OTP email", e);
        }
    }


    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("🔒 OTP Verification - Authify");
        message.setText("Hello,\n\nYour OTP for verification is: " + otp +
                "\n\nPlease use this OTP to complete your verification." +
                "\n\nIf you did not request this, please ignore this email." +
                "\n\nBest regards,\nThe Authify Team");

        mailSender.send(message);
    }
}
