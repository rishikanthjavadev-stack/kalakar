package com.kalakar.kalakar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AIEmailService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateEmailContent(String customerName,
                                        String productName,
                                        String status,
                                        String trackingNumber) {
        try {
            String prompt = buildPrompt(customerName, productName,
                                         status, trackingNumber);
            String requestBody = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("model", "claude-opus-4-5");
                    put("max_tokens", 300);
                    put("messages", new java.util.ArrayList<>() {{
                        add(new java.util.HashMap<>() {{
                            put("role", "user");
                            put("content", prompt);
                        }});
                    }});
                }}
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            System.err.println("AI email generation failed: " + e.getMessage());
            return getDefaultContent(status, customerName, productName);
        }
    }

    private String buildPrompt(String customerName, String productName,
                                 String status, String trackingNumber) {
        return String.format("""
            You are a warm, friendly customer service assistant for KALAKAR,
            a premium Indian ethnic wear brand. Write a SHORT, personalized
            email body (2-3 sentences max) for this order update.
            
            Customer: %s
            Product: %s
            Status: %s
            Tracking Number: %s
            
            Brand voice: warm, elegant, celebrates Indian fashion culture.
            Do NOT include greeting or sign-off — just the main message body.
            Keep it under 50 words. Be genuine and excited.
            """,
            customerName, productName, status,
            trackingNumber != null ? trackingNumber : "Not yet assigned"
        );
    }

    private String getDefaultContent(String status, String customerName,
                                      String productName) {
        return switch (status) {
            case "CONFIRMED" ->
                "We've received your order and our team is preparing it with care. " +
                "Your beautiful piece will be on its way soon!";
            case "PACKED" ->
                "Your order has been carefully packed and is ready for dispatch. " +
                "Expect it to ship very soon!";
            case "SHIPPED" ->
                "Your order is now on its way to you! " +
                "Use the tracking number above to follow its journey.";
            case "OUT_FOR_DELIVERY" ->
                "Exciting news — your order is out for delivery today! " +
                "Please ensure someone is available to receive it.";
            case "DELIVERED" ->
                "Your order has been delivered! We hope you love your new piece. " +
                "Thank you for choosing Kalakar — wear your story!";
            case "CANCELLED" ->
                "Your order has been cancelled as requested. " +
                "We hope to serve you again soon with our beautiful collection.";
            default -> "Thank you for shopping with Kalakar!";
        };
    }
    public String generateWelcomeEmail(String customerName) {
        try {
            String prompt = String.format("""
                You are a warm customer service assistant for KALAKAR,
                a premium Indian ethnic wear brand.
                Write a SHORT, genuine welcome message (2-3 sentences max)
                for a new customer named %s who just registered.
                Brand voice: warm, elegant, celebrates Indian fashion culture.
                Do NOT include greeting or sign-off. Under 50 words.
                """, customerName);

            String requestBody = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("model", "claude-opus-4-5");
                    put("max_tokens", 150);
                    put("messages", new java.util.ArrayList<>() {{
                        add(new java.util.HashMap<>() {{
                            put("role", "user");
                            put("content", prompt);
                        }});
                    }});
                }}
            );

            var request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            var response = httpClient.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());
            var root = objectMapper.readTree(response.body());
            return root.path("content").get(0).path("text").asText();

        } catch (Exception e) {
            return "Welcome to Kalakar! We\'re thrilled to have you join our " +
                   "community of Indian fashion lovers. Explore our collection " +
                   "and wear your story!";
        }
    }

}
