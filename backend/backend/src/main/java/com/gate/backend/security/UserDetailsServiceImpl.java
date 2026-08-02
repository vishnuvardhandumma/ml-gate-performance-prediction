package com.gate.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gate.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String ADMIN_EMAIL = "admin@gatepredictor.com";
    private static final String ADMIN_PASSWORD = "{noop}Admin@123";

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            return org.springframework.security.core.userdetails.User
                    .withUsername(ADMIN_EMAIL)
                    .password(ADMIN_PASSWORD)
                    .roles("ADMIN")
                    .build();
        }

        return userRepository.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email));
    }
}