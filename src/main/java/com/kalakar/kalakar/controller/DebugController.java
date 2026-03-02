package com.kalakar.kalakar.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @GetMapping("/debug/me")
    public String whoAmI() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "NOT AUTHENTICATED";
        return "User: " + auth.getName() +
               " | Authorities: " + auth.getAuthorities() +
               " | Authenticated: " + auth.isAuthenticated();
    }
}
