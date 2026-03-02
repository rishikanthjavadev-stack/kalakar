package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderItem;
import com.kalakar.kalakar.model.Product;
import com.kalakar.kalakar.repository.ProductRepository;
import com.kalakar.kalakar.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;

    // ── Checkout Page ──
    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam(required = false) String productId,
                               @RequestParam(required = false) String qty,
                               Model model, Principal principal) {
        if (productId != null) {
            Product product = productRepository.findById(Long.parseLong(productId)).orElse(null);
            model.addAttribute("product", product);
            model.addAttribute("qty", qty != null ? Integer.parseInt(qty) : 1);
        }
        if (principal != null) {
            model.addAttribute("email", principal.getName());
        }
        return "checkout";
    }

    // ── Place Order ──
    @PostMapping("/orders/place")
    public String placeOrder(@RequestParam String customerName,
                             @RequestParam String customerEmail,
                             @RequestParam String address,
                             @RequestParam String city,
                             @RequestParam String state,
                             @RequestParam String zip,
                             @RequestParam Long productId,
                             @RequestParam int quantity,
                             Model model) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            model.addAttribute("error", "Product not found.");
            return "checkout";
        }

        OrderItem item = new OrderItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getImageUrl());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);

        List<OrderItem> items = new ArrayList<>();
        items.add(item);

        Order order = orderService.createOrder(
            customerName, customerEmail,
            address, city, state, zip, items);

        return "redirect:/orders/confirmation/" + order.getId();
    }

    // ── Order Confirmation ──
    @GetMapping("/orders/confirmation/{id}")
    public String orderConfirmation(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    // ── My Orders ──
    @GetMapping("/orders")
    public String myOrders(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        List<Order> orders = orderService.getOrdersByEmail(principal.getName());
        model.addAttribute("orders", orders);
        return "my-orders";
    }
}
