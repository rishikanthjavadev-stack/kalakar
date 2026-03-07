package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.Product;
import com.kalakar.kalakar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RAGSearchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private AIEmailService aiEmailService; // reuse your Claude caller

    // ─────────────────────────────────────────────
    // Called when a product is saved or updated
    // Generates and stores the embedding
    // ─────────────────────────────────────────────
    public void generateAndStoreEmbedding(Product product) {
        try {
            // Build rich text description for embedding
            String textToEmbed = embeddingService.buildProductEmbeddingText(
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice().doubleValue()
            );

            // Get embedding vector from OpenAI
            double[] embedding = embeddingService.getEmbedding(textToEmbed);

            // Store as JSON in MySQL
            product.setEmbedding(embeddingService.embeddingToJson(embedding));
            product.setEmbeddingText(textToEmbed);

            productRepository.save(product);

        } catch (Exception e) {
            // Log but don't fail the product save
            System.err.println("Warning: embedding failed for product "
                + product.getId() + ": " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Main RAG search method
    // 1. Embed the query
    // 2. Find top 5 similar products
    // 3. Ask Claude to explain results
    // ─────────────────────────────────────────────
    public RAGSearchResult search(String userQuery) {

        // STEP 1: Get all products that have embeddings
        List<Product> products = productRepository
            .findByEmbeddingIsNotNull();

        if (products.isEmpty()) {
            return new RAGSearchResult(
                List.of(),
                "No products with embeddings found. " +
                "Please regenerate embeddings."
            );
        }

        // STEP 2: Embed the user query
        double[] queryEmbedding = embeddingService.getEmbedding(userQuery);

        // STEP 3: Score each product by cosine similarity
        List<ScoredProduct> scored = products.stream()
            .filter(p -> p.getEmbedding() != null)
            .map(p -> {
                double[] productEmbedding = embeddingService
                    .jsonToEmbedding(p.getEmbedding());
                double score = embeddingService
                    .cosineSimilarity(queryEmbedding, productEmbedding);
                return new ScoredProduct(p, score);
            })
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(5) // Top 5 only
            .collect(Collectors.toList());

        List<Product> topProducts = scored.stream()
            .map(sp -> sp.product)
            .collect(Collectors.toList());

        // STEP 4: Ask Claude to generate natural language response
        String claudeResponse = generateClaudeResponse(
            userQuery, topProducts
        );

        return new RAGSearchResult(topProducts, claudeResponse);
    }

    // ─────────────────────────────────────────────
    // Build prompt + call Claude
    // ─────────────────────────────────────────────
    private String generateClaudeResponse(
            String query, List<Product> products) {

        // Build product context for Claude
        StringBuilder context = new StringBuilder();
        context.append("Available products:\n\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            context.append(String.format(
                "%d. %s - $%.2f\n   %s\n\n",
                i + 1, p.getName(),
                p.getPrice().doubleValue(),
                p.getDescription()
            ));
        }

        // Build the prompt
        String prompt = String.format("""
            You are a helpful shopping assistant for Kalakar,
            a clothing store.

            A customer searched for: "%s"

            Based on our product catalog, here are the most
            relevant items:

            %s

            Please provide a helpful, friendly response (2-3 sentences)
            explaining which products best match their search and why.
            Mention specific product names and prices.
            Keep it concise and conversational.
            """, query, context.toString());

        // Call Claude (reuse your existing pattern)
        try {
            return callClaude(prompt);
        } catch (Exception e) {
            return "Here are the products that best match your search:";
        }
    }

    private String callClaude(String prompt) throws Exception {
        // Reuse your existing HttpClient pattern from AIEmailService
        var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        var messages = List.of(Map.of("role", "user", "content", prompt));
        var requestBody = Map.of(
            "model", "claude-opus-4-6",
            "max_tokens", 300,
            "messages", messages
        );

        var httpClient = java.net.http.HttpClient.newHttpClient();
        var request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", System.getProperty("anthropic.api.key",
                getAnthropicKey()))
            .header("anthropic-version", "2023-06-01")
            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                objectMapper.writeValueAsString(requestBody)))
            .build();

        var response = httpClient.send(request,
            java.net.http.HttpResponse.BodyHandlers.ofString());

        var root = objectMapper.readTree(response.body());
        return root.path("content").get(0).path("text").asText();
    }

    @org.springframework.beans.factory.annotation.Value(
        "${anthropic.api.key:}")
    private String anthropicApiKey;

    private String getAnthropicKey() { return anthropicApiKey; }

    // ─────────────────────────────────────────────
    // Inner classes
    // ─────────────────────────────────────────────
    public static class RAGSearchResult {
        public final List<Product> products;
        public final String aiResponse;

        public RAGSearchResult(List<Product> products, String aiResponse) {
            this.products   = products;
            this.aiResponse = aiResponse;
        }
    }

    private static class ScoredProduct {
        final Product product;
        final double score;

        ScoredProduct(Product product, double score) {
            this.product = product;
            this.score   = score;
        }
    }
}
