package com.kalakar.kalakar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.embedding.url}")
    private String embeddingUrl;

    @Value("${openai.embedding.model}")
    private String embeddingModel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ─────────────────────────────────────────────
    // Get embedding vector from OpenAI
    // Input:  "Blue Wool Winter Jacket warm comfortable"
    // Output: [0.23, -0.87, 0.45, ...] 1536 numbers
    // ─────────────────────────────────────────────
    public double[] getEmbedding(String text) {
        try {
            // Build request body
            Map<String, Object> requestBody = Map.of(
                "model", embeddingModel,
                "input", text
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // Call OpenAI API
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(embeddingUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
            );

            // Parse response
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode embeddingArray = root
                .path("data").get(0).path("embedding");

            // Convert JSON array → double[]
            double[] embedding = new double[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding[i] = embeddingArray.get(i).asDouble();
            }

            return embedding;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get embedding: "
                + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Calculate cosine similarity between two vectors
    // Returns: 0.0 (completely different) to 1.0 (identical)
    // ─────────────────────────────────────────────
    public double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA.length != vectorB.length) return 0.0;

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct  += vectorA[i] * vectorB[i];
            magnitudeA  += vectorA[i] * vectorA[i];
            magnitudeB  += vectorB[i] * vectorB[i];
        }

        if (magnitudeA == 0.0 || magnitudeB == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    // ─────────────────────────────────────────────
    // Convert double[] to JSON string for DB storage
    // ─────────────────────────────────────────────
    public String embeddingToJson(double[] embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize embedding");
        }
    }

    // ─────────────────────────────────────────────
    // Convert JSON string from DB back to double[]
    // ─────────────────────────────────────────────
    public double[] jsonToEmbedding(String json) {
        try {
            return objectMapper.readValue(json, double[].class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize embedding");
        }
    }

    // ─────────────────────────────────────────────
    // Build text to embed from product fields
    // The richer this text, the better the search
    // ─────────────────────────────────────────────
    public String buildProductEmbeddingText(
            String name, String description,
            String category, double price) {

        return String.format(
            "Product: %s. Description: %s. Category: %s. Price: $%.2f",
            name, description, category, price
        );
    }
}
