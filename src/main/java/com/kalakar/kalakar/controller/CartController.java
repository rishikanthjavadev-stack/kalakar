package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.model.Cart;
import com.kalakar.kalakar.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class CartController {

    @Autowired private CartService cartService;

    // View cart
    @GetMapping("/cart")
    public String viewCart(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        Cart cart = cartService.getOrCreateCart(principal.getName());
        model.addAttribute("cart", cart);
        return "cart";
    }

    // Add to cart
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(defaultValue = "/") String redirect,
                            Principal principal) {
        if (principal == null) return "redirect:/login";
        cartService.addToCart(principal.getName(), productId, quantity);
        return "redirect:" + redirect;
    }

    // Update quantity
    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam Long itemId,
                                 @RequestParam int quantity,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";
        cartService.updateQuantity(principal.getName(), itemId, quantity);
        return "redirect:/cart";
    }

    // Remove item
    @PostMapping("/cart/remove")
    public String removeItem(@RequestParam Long itemId,
                             Principal principal) {
        if (principal == null) return "redirect:/login";
        cartService.removeItem(principal.getName(), itemId);
        return "redirect:/cart";
    }

    // Clear cart
    @PostMapping("/cart/clear")
    public String clearCart(Principal principal) {
        if (principal == null) return "redirect:/login";
        cartService.clearCart(principal.getName());
        return "redirect:/cart";
    }
}
