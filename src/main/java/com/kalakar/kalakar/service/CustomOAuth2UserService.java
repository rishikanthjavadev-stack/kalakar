package com.kalakar.kalakar.service;

import com.kalakar.kalakar.model.User;
import com.kalakar.kalakar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);

        String email      = oAuth2User.getAttribute("email");
        String fullName   = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        String imageUrl   = oAuth2User.getAttribute("picture");
        String provider   = request.getClientRegistration().getRegistrationId();

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setImageUrl(imageUrl);
            userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setEmail(email);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setImageUrl(imageUrl);
            newUser.setRole("ROLE_USER");
            newUser.setPassword("OAUTH_NO_PASSWORD");
            userRepository.save(newUser);
        }

        return oAuth2User;
    }
}
