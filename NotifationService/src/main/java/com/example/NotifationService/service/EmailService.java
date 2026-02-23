package com.example.NotifationService.service;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String username) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Platform ");
        message.setText(
                "Hello " + username + ",\n\n" +
                        "Welcome to our platform! Your account has been successfully created.\n\n" +
                        "We’re excited to have you onboard.\n\n" +
                        "Best Regards,\n" +
                        "Your Company Team"
        );

        mailSender.send(message);
    }
}
