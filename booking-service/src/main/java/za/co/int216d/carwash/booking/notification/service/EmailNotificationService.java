package za.co.int216d.carwash.booking.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email notification service
 * Sends emails for membership events
 */
@Service
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.fromAddress = System.getenv().getOrDefault("MAIL_FROM_ADDRESS", "noreply@int216d.co.za");
        this.fromName = System.getenv().getOrDefault("MAIL_FROM_NAME", "INT216D Smart Car Wash");
    }

    /**
     * Send subscription confirmation email
     */
    public void sendSubscriptionEmail(String toEmail, String clientName, String planName, Double monthlyPrice) {
        String subject = "Welcome to " + planName + " Membership!";
        String text = buildSubscriptionEmailBody(clientName, planName, monthlyPrice);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership renewal email
     */
    public void sendRenewalEmail(String toEmail, String clientName, String planName, LocalDateTime expiryDate) {
        String subject = "Your " + planName + " Membership Has Been Renewed";
        String text = buildRenewalEmailBody(clientName, planName, expiryDate);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership expiry warning email (N days before expiry)
     */
    public void sendExpiryWarningEmail(String toEmail, String clientName, Integer daysUntilExpiry) {
        String subject = "Your Membership Expires in " + daysUntilExpiry + " Days";
        String text = buildExpiryWarningEmailBody(clientName, daysUntilExpiry);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership expiry email
     */
    public void sendExpiryEmail(String toEmail, String clientName, String planName) {
        String subject = "Your " + planName + " Membership Has Expired";
        String text = buildExpiryEmailBody(clientName, planName);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send membership cancelled email
     */
    public void sendCancellationEmail(String toEmail, String clientName, String planName) {
        String subject = "Your " + planName + " Membership Has Been Cancelled";
        String text = buildCancellationEmailBody(clientName, planName);
        sendEmail(toEmail, subject, text);
    }

    /**
     * Send generic email
     */
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to {} with subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String buildSubscriptionEmailBody(String clientName, String planName, Double price) {
        return "Hi " + clientName + ",\n\n" +
            "Welcome to " + planName + " membership! Your subscription is now active.\n\n" +
            "Plan: " + planName + "\n" +
            "Monthly Price: R" + String.format("%.2f", price) + "\n\n" +
            "You can now enjoy all the benefits of your membership.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildRenewalEmailBody(String clientName, String planName, java.time.LocalDateTime expiryDate) {
        return "Hi " + clientName + ",\n\n" +
            "Your " + planName + " membership has been renewed successfully!\n\n" +
            "New Expiry Date: " + expiryDate + "\n\n" +
            "Thank you for continuing with us.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildExpiryWarningEmailBody(String clientName, Integer daysUntilExpiry) {
        return "Hi " + clientName + ",\n\n" +
            "Your membership will expire in " + daysUntilExpiry + " days.\n\n" +
            "To continue enjoying our services, please renew your membership.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildExpiryEmailBody(String clientName, String planName) {
        return "Hi " + clientName + ",\n\n" +
            "Your " + planName + " membership has expired.\n\n" +
            "To continue using our services, please subscribe to a membership plan.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }

    private String buildCancellationEmailBody(String clientName, String planName) {
        return "Hi " + clientName + ",\n\n" +
            "Your " + planName + " membership has been cancelled.\n\n" +
            "If you have any questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "INT216D Smart Car Wash";
    }
}
