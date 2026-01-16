package com.stockflow.modules.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    // TODO: Será implementado corretamente no Sprint 04 quando criarmos UserRepository
    // Por enquanto, retorna um user hardcoded para testes

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        // Hardcoded user para testes iniciais
        if ("admin@stockflow.com".equals(email)) {
            return User.withUsername("admin@stockflow.com")
                .password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy") // BCrypt hash of "admin123"
                .authorities(Collections.emptyList())
                .build();
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }
}
