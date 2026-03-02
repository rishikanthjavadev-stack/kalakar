package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.service.AIEmailService;
import com.kalakar.kalakar.service.OrderMonitoringAgent;
import com.kalakar.kalakar.service.OrderNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @Autowired private AIEmailService aiEmailService;
    @Autowired private OrderMonitoringAgent monitoringAgent;
    @Autowired private OrderNotificationService notificationService;

    @GetMapping("/debug/me")
    public String whoAmI() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "NOT AUTHENTICATED";
        return "User: " + auth.getName() +
               " | Authorities: " + auth.getAuthorities() +
               " | Authenticated: " + auth.isAuthenticated();
    }

    @GetMapping("/debug/test-ai")
    public String testAI() {
        try {
            String content = aiEmailService.generateEmailContent(
                "Risikanth", "Blue Banarasi Saree", "SHIPPED", "TRK123456");
            return "<h3>AI Response:</h3><p>" + content + "</p>";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/debug/test-welcome")
    public String testWelcome() {
        try {
            String content = aiEmailService.generateWelcomeEmail("Risikanth");
            return "<h3>Welcome Email AI Content:</h3><p>" + content + "</p>";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/debug/trigger-agent")
    public String triggerAgent() {
        try {
            monitoringAgent.checkStuckPendingOrders();
            monitoringAgent.checkDelayedShipments();
            monitoringAgent.sendFeedbackRequests();
            return "Agent triggered! Check terminal logs.";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/debug/test-email")
    public String testEmail() {
        try {
            notificationService.sendWelcomeEmail(
                "rishikanthjavadev@gmail.com", "Risikanth");
            return "✅ Welcome email sent! Check your inbox.";
        } catch (Exception e) {
            return "❌ Email failed: " + e.getMessage();
        }
    }
}
