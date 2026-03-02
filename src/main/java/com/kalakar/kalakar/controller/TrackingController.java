package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.dto.OrderDTO;
import com.kalakar.kalakar.mapper.OrderMapper;
import com.kalakar.kalakar.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class TrackingController {

    @Autowired private OrderService orderService;
    @Autowired private OrderMapper orderMapper;

    // Public tracking page - track by order ID + email
    @GetMapping("/track")
    public String trackPage() {
        return "track-order";
    }

    @PostMapping("/track")
    public String trackOrder(@RequestParam Long orderId,
                             @RequestParam String email,
                             Model model) {
        try {
            OrderDTO order = orderMapper.toDTO(
                orderService.getOrderByIdAndEmail(orderId, email));
            model.addAttribute("order", order);
        } catch (Exception e) {
            model.addAttribute("error",
                "No order found with that ID and email.");
        }
        return "track-order";
    }

    // Authenticated tracking - from My Orders page
    @GetMapping("/orders/track/{id}")
    public String trackMyOrder(@PathVariable Long id,
                               Principal principal,
                               Model model) {
        if (principal == null) return "redirect:/login";
        try {
            OrderDTO order = orderMapper.toDTO(
                orderService.getOrderByIdAndEmail(id, principal.getName()));
            model.addAttribute("order", order);
        } catch (Exception e) {
            model.addAttribute("error", "Order not found.");
        }
        return "track-order";
    }
}
