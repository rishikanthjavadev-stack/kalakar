package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.User;
import com.kalakar.kalakar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired private ApplicationContext applicationContext;

    private PasswordEncoder getPasswordEncoder() {
        return applicationContext.getBean(PasswordEncoder.class);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().replace("ROLE_", ""))
                .build();
    }

    public void registerUser(String fullName, String email, String password) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(getPasswordEncoder().encode(password));
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public String createResetToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        return token;
    }

    public boolean isValidResetToken(String token) {
        Optional<User> user = userRepository.findByResetToken(token);
        return user.isPresent() &&
                user.get().getResetTokenExpiry() != null &&
                user.get().getResetTokenExpiry().isAfter(LocalDateTime.now());
    }

    public boolean resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token).orElse(null);
        if (user == null || !isValidResetToken(token)) return false;

        user.setPassword(getPasswordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return true;
    }
}