package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderStatus;
import com.kalakar.kalakar.repository.OrderRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderMonitoringAgent {

    @Autowired private OrderRepository orderRepository;
    @Autowired private AIEmailService aiEmailService;
    @Autowired private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:3036}")
    private String baseUrl;

    // ── Every hour: check orders stuck in PENDING > 24hrs ──
    @Scheduled(fixedRate = 3600000)
    public void checkStuckPendingOrders() {
        System.out.println("🤖 Agent: Checking stuck pending orders...");
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Order> pendingOrders = orderRepository
            .findByStatus(OrderStatus.PENDING);

        for (Order order : pendingOrders) {
            if (order.getOrderDate() != null &&
                order.getOrderDate().isBefore(cutoff)) {
                System.out.println("⚠️ Order #KAL-" + order.getId()
                    + " stuck in PENDING 24hrs");
                String productName = getProductName(order);
                String aiContent = aiEmailService.generateEmailContent(
                    order.getCustomerName(), productName,
                    "PENDING_DELAY", null);
                sendEmail(
                    order.getCustomerEmail(),
                    "Update on your Kalakar order #KAL-" + order.getId(),
                    buildSimpleEmail(order.getCustomerName(),
                        "We're processing your order 🙏",
                        aiContent, order.getId())
                );
            }
        }
    }

    // ── Every day 9am: check shipped orders not delivered in 5 days ──
    @Scheduled(cron = "0 0 9 * * *")
    public void checkDelayedShipments() {
        System.out.println("🤖 Agent: Checking delayed shipments...");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(5);
        List<Order> shippedOrders = orderRepository
            .findByStatus(OrderStatus.SHIPPED);

        for (Order order : shippedOrders) {
            if (order.getShippedAt() != null &&
                order.getShippedAt().isBefore(cutoff)) {
                System.out.println("⚠️ Order #KAL-" + order.getId()
                    + " delayed shipment");
                sendEmail(
                    order.getCustomerEmail(),
                    "Checking in on your order #KAL-" + order.getId(),
                    buildDelayEmail(order)
                );
            }
        }
    }

    // ── Every day 6pm: send feedback after delivery ──
    @Scheduled(cron = "0 0 18 * * *")
    public void sendFeedbackRequests() {
        System.out.println("🤖 Agent: Checking feedback requests...");
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusHours(48);

        List<Order> deliveredOrders = orderRepository
            .findByStatus(OrderStatus.DELIVERED);

        for (Order order : deliveredOrders) {
            if (order.getDeliveredAt() != null &&
                order.getDeliveredAt().isAfter(twoDaysAgo) &&
                order.getDeliveredAt().isBefore(yesterday)) {
                System.out.println("📧 Sending feedback email for #KAL-"
                    + order.getId());
                sendEmail(
                    order.getCustomerEmail(),
                    "How was your Kalakar experience? 🌟",
                    buildFeedbackEmail(order)
                );
            }
        }
    }

    private String buildSimpleEmail(String name, String heading,
                                     String content, Long orderId) {
        return """
            <div style="font-family:'Helvetica Neue',sans-serif;max-width:560px;
                        margin:0 auto;background:white;padding:3rem;">
              <h1 style="font-family:Georgia,serif;color:#0F0C09;">
                KAL<span style="color:#E8621A;">A</span>KAR</h1>
              <hr style="border:none;border-top:2px solid #E8621A;margin:1rem 0 2rem;"/>
              <h2 style="font-family:Georgia,serif;color:#0F0C09;">%s</h2>
              <p style="color:#9C9080;line-height:1.8;">Hi %s,</p>
              <p style="color:#9C9080;line-height:1.8;">%s</p>
              <p style="color:#9C9080;">Order ID: <strong>#KAL-%d</strong></p>
              <a href="%s/track" style="display:inline-block;padding:0.8rem 2rem;
                 background:#E8621A;color:white;text-decoration:none;
                 font-size:0.82rem;letter-spacing:0.1em;text-transform:uppercase;">
                Track Order</a>
              <hr style="border:none;border-top:1px solid #EDE7D9;margin:2rem 0 1rem;"/>
              <p style="color:#ccc;font-size:0.72rem;">© 2025 Kalakar</p>
            </div>
            """.formatted(heading, name, content, orderId, baseUrl);
    }

    private String buildDelayEmail(Order order) {
        return """
            <div style="font-family:'Helvetica Neue',sans-serif;max-width:560px;
                        margin:0 auto;background:white;padding:3rem;">
              <h1 style="font-family:Georgia,serif;color:#0F0C09;">
                KAL<span style="color:#E8621A;">A</span>KAR</h1>
              <hr style="border:none;border-top:2px solid #E8621A;margin:1rem 0 2rem;"/>
              <h2 style="font-family:Georgia,serif;color:#0F0C09;">
                Checking in on your delivery 📦</h2>
              <p style="color:#9C9080;">Hi %s,</p>
              <p style="color:#9C9080;line-height:1.8;">
                Your order <strong>#KAL-%d</strong> was shipped a few days ago.
                We wanted to make sure everything is on track.</p>
              %s
              <a href="%s/track" style="display:inline-block;padding:0.8rem 2rem;
                 background:#E8621A;color:white;text-decoration:none;
                 font-size:0.82rem;letter-spacing:0.1em;text-transform:uppercase;">
                Track Order</a>
              <hr style="border:none;border-top:1px solid #EDE7D9;margin:2rem 0 1rem;"/>
              <p style="color:#ccc;font-size:0.72rem;">© 2025 Kalakar</p>
            </div>
            """.formatted(
                order.getCustomerName(),
                order.getId(),
                order.getTrackingNumber() != null
                    ? "<p style='color:#9C9080;'>Tracking: <strong>"
                      + order.getTrackingNumber() + "</strong> via "
                      + order.getCourierName() + "</p>"
                    : "",
                baseUrl
            );
    }

    private String buildFeedbackEmail(Order order) {
        return """
            <div style="font-family:'Helvetica Neue',sans-serif;max-width:560px;
                        margin:0 auto;background:white;padding:3rem;">
              <h1 style="font-family:Georgia,serif;color:#0F0C09;">
                KAL<span style="color:#E8621A;">A</span>KAR</h1>
              <hr style="border:none;border-top:2px solid #E8621A;margin:1rem 0 2rem;"/>
              <h2 style="font-family:Georgia,serif;color:#0F0C09;">
                How was your experience? 🌟</h2>
              <p style="color:#9C9080;">Hi %s,</p>
              <p style="color:#9C9080;line-height:1.8;">
                Your order <strong>#KAL-%d</strong> was delivered!
                We hope you love your new piece from Kalakar.</p>
              <p style="color:#9C9080;line-height:1.8;">
                Your feedback means the world to us and helps us
                serve you better.</p>
              <a href="%s" style="display:inline-block;padding:0.8rem 2rem;
                 background:#E8621A;color:white;text-decoration:none;
                 font-size:0.82rem;letter-spacing:0.1em;text-transform:uppercase;
                 margin-right:1rem;">Shop Again</a>
              <hr style="border:none;border-top:1px solid #EDE7D9;margin:2rem 0 1rem;"/>
              <p style="color:#ccc;font-size:0.72rem;">© 2025 Kalakar</p>
            </div>
            """.formatted(order.getCustomerName(), order.getId(), baseUrl);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
        }
    }

    private String getProductName(Order order) {
        return order.getItems() != null && !order.getItems().isEmpty()
            ? order.getItems().get(0).getProductName()
            : "your order";
    }
}
