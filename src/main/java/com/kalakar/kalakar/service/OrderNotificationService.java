package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OrderNotificationService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private AIEmailService aiEmailService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:3036}")
    private String baseUrl;

    // ── Welcome Email on Register ──
    public void sendWelcomeEmail(String customerEmail, String customerName) {
        try {
            String aiContent = aiEmailService.generateWelcomeEmail(customerName);
            String html = loadTemplate("welcome.html")
                .replace("{{customerName}}", customerName)
                .replace("{{aiContent}}", aiContent)
                .replace("{{storeUrl}}", baseUrl + "/");
            sendEmail(customerEmail,
                "Welcome to Kalakar, " + customerName + "! 🎉", html);
            System.out.println("✅ Welcome email sent to: " + customerEmail);
        } catch (Exception e) {
            System.err.println("❌ Welcome email failed: " + e.getMessage());
        }
    }

    // ── Order Status Update Email ──
    public void sendStatusUpdateEmail(Order order, OrderStatus newStatus) {
        try {
            String productName = order.getItems() != null &&
                !order.getItems().isEmpty()
                ? order.getItems().get(0).getProductName()
                : "your order";

            String aiContent = aiEmailService.generateEmailContent(
                order.getCustomerName(), productName,
                newStatus.name(), order.getTrackingNumber());

            String subject = getSubject(newStatus, order.getId());
            String html = buildEmailHtml(order, newStatus, aiContent);

            sendEmail(order.getCustomerEmail(), subject, html);
            System.out.println("✅ Status email sent to "
                + order.getCustomerEmail() + " → " + newStatus);
        } catch (Exception e) {
            System.err.println("❌ Status email failed: " + e.getMessage());
        }
    }

    // ── Build Email HTML from template ──
    private String buildEmailHtml(Order order, OrderStatus status,
                                   String aiContent) {
        String trackUrl = baseUrl + "/track";
        String storeUrl = baseUrl + "/";
        String template = loadTemplate(getTemplateName(status));

        return template
            .replace("{{customerName}}", safe(order.getCustomerName()))
            .replace("{{orderId}}", String.valueOf(order.getId()))
            .replace("{{totalAmount}}", String.valueOf(order.getTotalAmount()))
            .replace("{{aiContent}}", safe(aiContent))
            .replace("{{trackUrl}}", trackUrl)
            .replace("{{storeUrl}}", storeUrl)
            .replace("{{trackingNumber}}", safe(order.getTrackingNumber()))
            .replace("{{courierName}}", safe(order.getCourierName()));
    }

    // ── Template name by status ──
    private String getTemplateName(OrderStatus status) {
        return switch (status) {
            case CONFIRMED        -> "order-confirmed.html";
            case PACKED, SHIPPED  -> "order-shipped.html";
            case DELIVERED        -> "order-delivered.html";
            case CANCELLED        -> "order-cancelled.html";
            default               -> "order-confirmed.html";
        };
    }

    // ── Email subject by status ──
    private String getSubject(OrderStatus status, Long orderId) {
        return switch (status) {
            case CONFIRMED        -> "✅ Order #KAL-" + orderId + " Confirmed!";
            case PACKED           -> "📦 Order #KAL-" + orderId + " is being packed";
            case SHIPPED          -> "🚚 Order #KAL-" + orderId + " has shipped!";
            case OUT_FOR_DELIVERY -> "📍 Order #KAL-" + orderId + " is out for delivery!";
            case DELIVERED        -> "🎉 Order #KAL-" + orderId + " delivered!";
            case CANCELLED        -> "Order #KAL-" + orderId + " has been cancelled";
            default               -> "Update on Order #KAL-" + orderId;
        };
    }

    // ── Load HTML template from resources ──
    private String loadTemplate(String filename) {
        try {
            var resource = getClass().getClassLoader()
                .getResourceAsStream("templates/emails/" + filename);
            if (resource != null) return new String(resource.readAllBytes());
        } catch (Exception e) {
            System.err.println("Template load failed: " + e.getMessage());
        }
        return "<p>{{aiContent}}</p>";
    }

    // ── Send email via JavaMail ──
    private void sendEmail(String to, String subject,
                            String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
            message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    // ── Null safe string ──
    private String safe(String value) {
        return value != null ? value : "";
    }
}
