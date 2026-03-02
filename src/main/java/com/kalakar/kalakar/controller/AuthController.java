package com.kalakar.kalakar.controller;

import com.kalakar.kalakar.service.EmailService;
import com.kalakar.kalakar.service.OrderNotificationService;
import com.kalakar.kalakar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private EmailService emailService;
    @Autowired private OrderNotificationService notificationService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String reset,
                            Model model) {
        if (error != null)      model.addAttribute("error",   "Invalid email or password.");
        if (logout != null)     model.addAttribute("logout",  "You have been logged out.");
        if (registered != null) model.addAttribute("success", "Account created! Please sign in.");
        if (reset != null)      model.addAttribute("success", "Password reset! Please sign in.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String registerSubmit(@RequestParam String fullName,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            return "register";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }
        userService.registerUser(fullName, email, password);
        // Send AI welcome email
        try {
            notificationService.sendWelcomeEmail(email, fullName);
        } catch (Exception e) {
            System.err.println("Welcome email failed: " + e.getMessage());
        }
        return "redirect:/login?registered=true";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email,
                                        Model model) {
        if (!userService.emailExists(email)) {
            model.addAttribute("success",
                "If that email is registered, a reset link has been sent.");
            return "forgot-password";
        }
        try {
            String token = userService.createResetToken(email);
            emailService.sendPasswordResetEmail(email, token);
            model.addAttribute("success",
                "Reset link sent! Check your inbox at " + email);
        } catch (Exception e) {
            model.addAttribute("error",
                "Could not send email. Please try again later.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        if (!userService.isValidResetToken(token)) {
            model.addAttribute("error",
                "This reset link is invalid or has expired.");
            return "forgot-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        boolean success = userService.resetPassword(token, password);
        if (!success) {
            model.addAttribute("error",
                "Reset link expired. Please request a new one.");
            return "forgot-password";
        }
        return "redirect:/login?reset=true";
    }

    // Handle GET /logout gracefully - show proper page
    @GetMapping("/logout")
    public String logoutPage() {
        return "logout";
    }

    @GetMapping("/access-denied")
    public String accessDenied() { return "access-denied"; }
}
