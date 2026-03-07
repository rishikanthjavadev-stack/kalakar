package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.model.Product;
import com.kalakar.kalakar.repository.ProductRepository;
import com.kalakar.kalakar.service.RAGSearchService;
import com.kalakar.kalakar.service.RAGSearchService.RAGSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/search")
public class RAGSearchController {

    @Autowired
    private RAGSearchService ragSearchService;

    @Autowired
    private ProductRepository productRepository;

    // Show search page
    @GetMapping
    public String searchPage() {
        return "rag-search";
    }

    // Handle search query
    @GetMapping("/results")
    public String search(
            @RequestParam String query,
            Model model) {

        RAGSearchResult result = ragSearchService.search(query);
        model.addAttribute("query", query);
        model.addAttribute("products", result.products);
        model.addAttribute("aiResponse", result.aiResponse);
        return "rag-search";
    }

    // ─────────────────────────────────────────────
    // Generate embeddings for ALL existing products
    // Visit: GET /search/admin/regenerate-embeddings
    // ─────────────────────────────────────────────
    @GetMapping("/admin/regenerate-embeddings")
    @ResponseBody
    public String regenerateEmbeddings() {
        List<Product> allProducts = productRepository.findAll();
        int success = 0;
        int failed  = 0;

        for (Product product : allProducts) {
            try {
                ragSearchService.generateAndStoreEmbedding(product);
                success++;
                System.out.println("✅ Embedded: " + product.getName());
            } catch (Exception e) {
                failed++;
                System.err.println("❌ Failed: " + product.getName()
                    + " → " + e.getMessage());
            }
        }

        return String.format(
            "Done! ✅ %d products embedded successfully. ❌ %d failed.",
            success, failed
        );
    }
}
