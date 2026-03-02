package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.repository.ProductRepository;
import com.kalakar.kalakar.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired private ProductRepository productRepository;
    @Autowired private CartService cartService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String logout,
                       Model model,
                       Principal principal) {
        model.addAttribute("products", productRepository.findAll());
        if (logout != null) {
            model.addAttribute("toast", "You have been logged out. See you soon! 👋");
        }
        if (principal != null) {
            model.addAttribute("cartCount", cartService.getCartCount(principal.getName()));
        } else {
            model.addAttribute("cartCount", 0);
        }
        return "index";
    }
}
