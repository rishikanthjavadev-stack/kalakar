package com.kalakar.kalakar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String token) throws MessagingException {
        String resetLink = baseUrl + "/reset-password?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Reset your Kalakar password");

        String html = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: 'Helvetica Neue', sans-serif; background: #F2EDE4; padding: 2rem;">
              <div style="max-width: 520px; margin: 0 auto; background: white; padding: 3rem;">
                <h1 style="font-family: Georgia, serif; font-size: 2rem; color: #0F0C09; margin-bottom: 0.5rem;">
                  KALA<span style="color:#E8621A">K</span>AR
                </h1>
                <hr style="border: none; border-top: 2px solid #E8621A; margin: 1rem 0 2rem;"/>
                <h2 style="font-family: Georgia, serif; color: #0F0C09;">Reset Your Password</h2>
                <p style="color: #9C9080; line-height: 1.6;">
                  We received a request to reset your password. Click the button below to choose a new one.
                  This link expires in <strong>1 hour</strong>.
                </p>
                <a href="%s"
                   style="display:inline-block; margin: 2rem 0; padding: 1rem 2.5rem;
                          background: #E8621A; color: white; text-decoration: none;
                          font-size: 0.85rem; letter-spacing: 0.15em; text-transform: uppercase;">
                  Reset My Password
                </a>
                <p style="color: #bbb; font-size: 0.8rem;">
                  If you didn't request this, you can safely ignore this email.<br/>
                  This link will expire in 1 hour.
                </p>
                <hr style="border: none; border-top: 1px solid #EDE7D9; margin-top: 2rem;"/>
                <p style="color: #ccc; font-size: 0.72rem;">© 2025 Kalakar — Wear Your Art</p>
              </div>
            </body>
            </html>
            """.formatted(resetLink);

        helper.setText(html, true);
        mailSender.send(message);
    }
}
