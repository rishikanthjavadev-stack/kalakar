package com.kalakar.kalakar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public String handleError(Exception ex, Model model) {
        System.err.println("=== KALAKAR ERROR ===");
        System.err.println("Message: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("=====================");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}
