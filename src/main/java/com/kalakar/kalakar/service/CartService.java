package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.Cart;
import com.kalakar.kalakar.model.CartItem;
import com.kalakar.kalakar.model.Product;
import com.kalakar.kalakar.repository.CartRepository;
import com.kalakar.kalakar.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;

    public Cart getOrCreateCart(String email) {
        return cartRepository.findByUserEmail(email)
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUserEmail(email);
                cart.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(cart);
            });
    }

    @Transactional
    public Cart addToCart(String email, Long productId, int quantity) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return cart;

        Optional<CartItem> existing = cart.getItems().stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setProductCategory(product.getCategory());
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(String email, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(email);
        if (quantity <= 0) {
            cart.getItems().removeIf(i -> i.getId().equals(itemId));
        } else {
            cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
        }
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public int getCartCount(String email) {
        return cartRepository.findByUserEmail(email)
            .map(Cart::getTotalItems)
            .orElse(0);
    }
}
