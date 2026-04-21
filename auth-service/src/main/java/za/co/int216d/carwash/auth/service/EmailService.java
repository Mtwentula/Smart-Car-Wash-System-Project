package za.co.int216d.carwash.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import za.co.int216d.carwash.auth.config.AuthAppProperties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AuthAppProperties props;

    public void sendOtp(String toEmail, String otp, int ttlMinutes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(props.getMail().getFrom());
        msg.setTo(toEmail);
        msg.setSubject("INT216D Car Wash — Verify your email");
        msg.setText("""
                Welcome to INT216D Car Wash!

                Your verification code is: %s

                This code expires in %d minutes.

                If you did not register an account, you can safely ignore this message.
                """.formatted(otp, ttlMinutes));
        safeSend(msg, toEmail);
    }

    public void sendWelcome(String toEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(props.getMail().getFrom());
        msg.setTo(toEmail);
        msg.setSubject("Welcome to INT216D Car Wash");
        msg.setText("""
                Your account is now verified.

                You can log in and book your first wash whenever you're ready.
                """);
        safeSend(msg, toEmail);
    }

    private void safeSend(SimpleMailMessage msg, String toEmail) {
        try {
            mailSender.send(msg);
        } catch (Exception ex) {
            log.warn("Failed to send mail to {}: {}", toEmail, ex.getMessage());
        }
    }
}
