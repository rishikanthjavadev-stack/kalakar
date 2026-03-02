package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.model.Cart;
import com.kalakar.kalakar.model.Product;
import com.kalakar.kalakar.repository.ProductRepository;
import com.kalakar.kalakar.service.CartService;
import com.kalakar.kalakar.service.StripeService;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    @Autowired private StripeService stripeService;
    @Autowired private CartService cartService;
    @Autowired private ProductRepository productRepository;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    // ── Checkout from Cart ──
    @PostMapping("/payment/checkout-cart")
    public String checkoutFromCart(Principal principal) {
        if (principal == null) return "redirect:/login";
        try {
            Cart cart = cartService.getOrCreateCart(principal.getName());
            if (cart.getItems().isEmpty()) return "redirect:/cart";

            List<String> names = cart.getItems().stream()
                .map(i -> i.getProductName())
                .collect(Collectors.toList());

            List<Long> prices = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(
                    java.math.BigDecimal.valueOf(100)).longValue())
                .collect(Collectors.toList());

            List<Long> quantities = cart.getItems().stream()
                .map(i -> (long) i.getQuantity())
                .collect(Collectors.toList());

            String url = stripeService.createCartCheckoutSession(
                names, prices, quantities);
            return "redirect:" + url;

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cart?error=payment_failed";
        }
    }

    // ── Buy Now single product ──
    @PostMapping("/payment/buy-now")
    public String buyNow(@RequestParam Long productId,
                         @RequestParam(defaultValue = "1") int quantity,
                         Principal principal) {
        if (principal == null) return "redirect:/login";
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return "redirect:/";

            long priceInCents = product.getPrice()
                .multiply(java.math.BigDecimal.valueOf(100)).longValue();

            String url = stripeService.createSingleCheckoutSession(
                product.getName(), priceInCents, quantity);
            return "redirect:" + url;

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=payment_failed";
        }
    }

    // ── Payment Success ──
    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam String session_id,
                                  Principal principal,
                                  Model model) {
        try {
            Session session = stripeService.getSession(session_id);
            model.addAttribute("sessionId", session_id);
            model.addAttribute("amountPaid",
                session.getAmountTotal() / 100.0);
            model.addAttribute("customerEmail",
                session.getCustomerDetails() != null
                    ? session.getCustomerDetails().getEmail() : "");

            // Clear cart after successful payment
            if (principal != null) {
                cartService.clearCart(principal.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "payment-success";
    }
}
