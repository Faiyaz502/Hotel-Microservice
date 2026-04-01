package com.example.NotifationService.service;


import com.example.NotifationService.Client.UserClient;
import com.example.NotifationService.payload.BookingConfirmationEvent;
import com.example.NotifationService.payload.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;
    private final UserClient userClient;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

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

    /// Booking Confirmation

    public void sendBookingConfirmationEmail(BookingConfirmationEvent event) {
        try {
            // 1. Fetch User Details via Feign Client
            User user = userClient.getUserDetails(event.getUserId());

            if (user == null || user.getEmail() == null) {
                log.error("Cannot send email: User details not found for ID {}", event.getUserId());
                return;
            }

            // 2. Prepare the Email Message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Booking Confirmed! - " + event.getBookingId());

            String emailContent = String.format(
                    "Hello %s,\n\n" +
                            "Great news! Your booking at Hotel %s has been successfully confirmed.\n\n" +
                            "--- Booking Details ---\n" +
                            "Booking ID: %s\n" +
                            "Status: %s\n" +
                            "------------------------\n\n" +
                            "We look forward to hosting you. If you have any questions, feel free to contact us.\n\n" +
                            "Faiyaz Travels,\n" +
                            "The Hotel Support Team",
                    user.getName(),
                    event.getHotelId(),
                    event.getBookingId(),
                    event.getStatus()
            );

            message.setText(emailContent);

            // 3. Send the Email
            mailSender.send(message);
            log.info("Confirmation email sent to {} for booking {}", user.getEmail(), event.getBookingId());

        } catch (Exception e) {
            log.error("Failed to process booking confirmation email for booking {}", event.getBookingId(), e);
        }
    }




}
