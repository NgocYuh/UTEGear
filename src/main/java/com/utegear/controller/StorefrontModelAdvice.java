package com.utegear.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = StorefrontViewController.class)
public class StorefrontModelAdvice {

    @ModelAttribute("isAuthenticated")
    boolean isAuthenticated() {
        return false;
    }

    @ModelAttribute("currentUser")
    Object currentUser() {
        return null;
    }

    @ModelAttribute("cartCount")
    int cartCount() {
        return 0;
    }

    @ModelAttribute("wishlistCount")
    int wishlistCount() {
        return 0;
    }

    @ModelAttribute("keyword")
    String keyword() {
        return null;
    }
}
