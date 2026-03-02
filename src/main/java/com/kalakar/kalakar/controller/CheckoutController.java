package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.model.Cart;
import com.kalakar.kalakar.model.Order;
import com.kalakar.kalakar.model.OrderItem;
import com.kalakar.kalakar.model.CartItem;
import com.kalakar.kalakar.service.CartService;
import com.kalakar.kalakar.service.OrderService;
import com.kalakar.kalakar.service.OrderNotificationService;
import com.kalakar.kalakar.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private OrderNotificationService notificationService;

    // Show COD checkout page
    @GetMapping("/cod")
    public String codCheckoutPage(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        Cart cart = cartService.getOrCreateCart(principal.getName());
        if (cart.getItems().isEmpty()) return "redirect:/cart";
        model.addAttribute("cart", cart);
        model.addAttribute("email", principal.getName());
        return "checkout-cod";
    }

    // Place COD order
    @PostMapping("/cod/place")
    public String placeCodOrder(
            @RequestParam String customerName,
            @RequestParam String customerPhone,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String zip,
            Principal principal,
            Model model) {

        if (principal == null) return "redirect:/login";

        try {
            Cart cart = cartService.getOrCreateCart(principal.getName());
            if (cart.getItems().isEmpty()) return "redirect:/cart";

            // Convert cart items to order items
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cart.getItems()) {
                OrderItem item = new OrderItem();
                item.setProductId(cartItem.getProductId());
                item.setProductName(cartItem.getProductName());
                item.setProductImage(cartItem.getProductImage());
                item.setPrice(cartItem.getPrice());
                item.setQuantity(cartItem.getQuantity());
                orderItems.add(item);
            }

            // Create order
            Order order = orderService.createOrder(
                customerName, principal.getName(),
                address, city, state, zip, orderItems);

            // Set phone
            order = orderService.updatePhone(order.getId(), customerPhone);

            // Clear cart
            cartService.clearCart(principal.getName());

            // Send order placed confirmation email
            notificationService.sendStatusUpdateEmail(order, OrderStatus.PENDING);

            return "redirect:/orders/confirmation/" + order.getId();

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to place order: " + e.getMessage());
            return "checkout-cod";
        }
    }
}
