package com.umbral.domain.service;

import com.umbral.domain.entity.User;
import com.umbral.domain.exception.ResourceNotFoundException;
import com.umbral.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private static final Long DEMO_USER_ID = 1L;
    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return userRepository.findById(DEMO_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario de demostracion"
                ));
    }
}
