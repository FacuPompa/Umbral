package com.umbral.domain.service;

import com.umbral.domain.entity.User;
import com.umbral.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getHandle())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
