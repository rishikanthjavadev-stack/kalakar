package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.dto.ProductCreateDTO;
import com.kalakar.kalakar.dto.ProductDTO;
import com.kalakar.kalakar.mapper.OrderMapper;
import com.kalakar.kalakar.mapper.ProductMapper;
import com.kalakar.kalakar.model.Product;
import com.kalakar.kalakar.repository.ProductRepository;
import com.kalakar.kalakar.service.ImageService;
import com.kalakar.kalakar.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ImageService imageService;
    @Autowired private OrderService orderService;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;

    @GetMapping("/products")
    public String adminProducts(Model model) {
        model.addAttribute("products",
            productMapper.toDTOList(productRepository.findAll()));
        return "admin-products";
    }

    @GetMapping("/products/search")
    public String searchProducts(@RequestParam String q, Model model) {
        model.addAttribute("products",
            productMapper.toDTOList(
                productRepository.findByNameContainingIgnoreCase(q)));
        return "admin-products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new ProductCreateDTO());
        return "admin-add-product";
    }

    @PostMapping("/products/add")
    public String addProduct(
            @ModelAttribute ProductCreateDTO dto,
            @RequestParam(required = false) MultipartFile image,
            Model model) {
        try {
            Product product = productMapper.toEntity(dto);
            if (image != null && !image.isEmpty()) {
                product.setImageUrl(imageService.saveImage(image));
            }
            if (product.getBadge() != null && product.getBadge().isEmpty()) {
                product.setBadge(null);
            }
            productRepository.save(product);
            return "redirect:/admin/products?success=true";
        } catch (IOException e) {
            model.addAttribute("error", "Image upload failed: " + e.getMessage());
            return "admin-add-product";
        }
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("product", productMapper.toDTO(product));
        return "admin-edit-product";
    }

    @PostMapping("/products/edit/{id}")
    public String editProduct(
            @PathVariable Long id,
            @ModelAttribute ProductCreateDTO dto,
            @RequestParam(required = false) MultipartFile image,
            Model model) {
        try {
            Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
            productMapper.updateEntityFromDTO(dto, product);
            if (image != null && !image.isEmpty()) {
                if (product.getImageUrl() != null) {
                    imageService.deleteImage(product.getImageUrl());
                }
                product.setImageUrl(imageService.saveImage(image));
            }
            if (product.getBadge() != null && product.getBadge().isEmpty()) {
                product.setBadge(null);
            }
            productRepository.save(product);
            return "redirect:/admin/products?success=true";
        } catch (IOException e) {
            model.addAttribute("error", "Image upload failed: " + e.getMessage());
            return "admin-edit-product";
        }
    }

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            Product product = productRepository.findById(id).orElse(null);
            if (product != null) {
                if (product.getImageUrl() != null) {
                    imageService.deleteImage(product.getImageUrl());
                }
                productRepository.delete(product);
            }
            return "redirect:/admin/products?deleted=true";
        } catch (Exception e) {
            return "redirect:/admin/products?error=true";
        }
    }

    @GetMapping("/orders")
    public String adminOrders(Model model) {
        try {
            model.addAttribute("orders",
                orderMapper.toDTOList(orderService.getAllOrders()));
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
            model.addAttribute("orders", new ArrayList<>());
        }
        return "admin-orders";
    }

    @PostMapping("/orders/status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status) {
        try {
            orderService.updateStatus(id, status);
        } catch (Exception e) {
            System.err.println("Error updating order: " + e.getMessage());
        }
        return "redirect:/admin/orders?updated=true";
    }

    @GetMapping("/orders/{id}")
    public String adminOrderDetail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("order",
                orderMapper.toDTO(orderService.getOrderById(id)));
        } catch (Exception e) {
            System.err.println("Error loading order: " + e.getMessage());
        }
        return "admin-order-detail";
    }
}
