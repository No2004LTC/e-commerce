package ecommerce.example.ecommerce.infrastructure.email;

import org.springframework.stereotype.Service;

/**
 * Email Service - Adapter for sending emails
 */
@Service
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        // Implementation for sending emails
        // Can integrate with Mailgun, SendGrid, SMTP, etc.
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}
